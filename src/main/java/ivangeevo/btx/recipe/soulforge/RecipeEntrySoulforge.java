package ivangeevo.btx.recipe.soulforge;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.HasJsonAdapter;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.adapter.RecipeJsonAdapter;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerCrafting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeEntrySoulforge extends RecipeEntrySoulforgeBase<List<String>, ItemStack> implements HasJsonAdapter {
	private final Map<Character, RecipeSymbol> symbols;
	private final int width;
	private final int height;

	public RecipeEntrySoulforge(List<String> pattern, Map<Character, RecipeSymbol> symbols, ItemStack output) {
		super(pattern, output);
		if (pattern.isEmpty() || pattern.size() > GRID_SIZE) {
			throw new IllegalArgumentException("Soulforge pattern must have 1-" + GRID_SIZE + " rows");
		}
		int maxWidth = 0;
		for (String row : pattern) {
			if (row.length() > GRID_SIZE) {
				throw new IllegalArgumentException("Soulforge pattern rows must be at most " + GRID_SIZE + " wide");
			}
			maxWidth = Math.max(maxWidth, row.length());
		}
		this.symbols = symbols;
		this.width = maxWidth;
		this.height = pattern.size();
	}

	public RecipeEntrySoulforge() {
		super();
		this.symbols = new HashMap<>();
		this.width = 0;
		this.height = 0;
	}

	@Override
	public boolean matches(ContainerCrafting grid) {
		if (grid.getContainerSize() != GRID_SIZE * GRID_SIZE || this.width == 0) {
			return false;
		}

		for (int offsetX = 0; offsetX <= GRID_SIZE - this.width; offsetX++) {
			for (int offsetY = 0; offsetY <= GRID_SIZE - this.height; offsetY++) {
				if (matchesAt(grid, offsetX, offsetY, false) || matchesAt(grid, offsetX, offsetY, true)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean matchesAt(ContainerCrafting grid, int offsetX, int offsetY, boolean mirrored) {
		for (int x = 0; x < GRID_SIZE; x++) {
			for (int y = 0; y < GRID_SIZE; y++) {
				int recipeX = x - offsetX;
				int recipeY = y - offsetY;
				RecipeSymbol symbol = null;

				if (recipeX >= 0 && recipeY >= 0 && recipeX < this.width && recipeY < this.height) {
					symbol = getSymbol(mirrored ? this.width - 1 - recipeX : recipeX, recipeY);
				}

				ItemStack stack = grid.getItemStackAt(x, y);
				if (symbol == null) {
					if (stack != null) {
						return false;
					}
				} else if (stack == null || !symbol.matches(stack)) {
					return false;
				}
			}
		}

		return true;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public @Nullable RecipeSymbol getSymbol(int x, int y) {
		if (y < 0 || y >= this.height || x < 0) {
			return null;
		}
		String row = getInput().get(y);
		if (x >= row.length()) {
			return null;
		}
		char key = row.charAt(x);

		return key == ' ' ? null : this.symbols.get(key);
	}

	@Override
	public @Nullable ItemStack getCraftingResult(ContainerCrafting grid) {
		return getOutput().copy();
	}

	@Override
	public RecipeJsonAdapter<?> getAdapter() {
		return new JsonAdapter();
	}

	public static class JsonAdapter implements RecipeJsonAdapter<RecipeEntrySoulforge> {
		@Override
		public RecipeEntrySoulforge deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();

			List<String> pattern = new ArrayList<>();

			for (JsonElement row : object.getAsJsonArray("pattern")) {
				pattern.add(row.getAsString());
			}

			Map<Character, RecipeSymbol> symbols = new HashMap<>();

			for (JsonElement element : object.getAsJsonArray("symbols")) {
				JsonObject symbolObject = element.getAsJsonObject();
				char key = symbolObject.get("symbol").getAsString().charAt(0);
				symbols.put(key, context.deserialize(symbolObject, RecipeSymbol.class));
			}

			ItemStack result = context.deserialize(object.get("result"), ItemStack.class);

			return new RecipeEntrySoulforge(pattern, symbols, result);
		}

		@Override
		public JsonElement serialize(@NotNull RecipeEntrySoulforge recipe, Type typeOfSrc, @NotNull JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			object.addProperty("type", Registries.RECIPE_TYPES.getKey(recipe.getClass()));
			object.addProperty("name", recipe.toString());

			JsonArray pattern = new JsonArray();

			for (String row : recipe.getInput()) {
				pattern.add(row);
			}

			object.add("pattern", pattern);

			JsonArray symbols = new JsonArray();
			for (Map.Entry<Character, RecipeSymbol> entry : recipe.symbols.entrySet()) {
				JsonObject symbolObject = context.serialize(entry.getValue(), RecipeSymbol.class).getAsJsonObject();
				symbolObject.addProperty("symbol", String.valueOf(entry.getKey()));
				symbols.add(symbolObject);
			}

			object.add("symbols", symbols);
			object.add("result", context.serialize(recipe.getOutput(), ItemStack.class));

			return object;
		}
	}
}
