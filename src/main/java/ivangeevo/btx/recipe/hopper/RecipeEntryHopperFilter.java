package ivangeevo.btx.recipe.hopper;

import com.google.gson.*;
import ivangeevo.btx.recipe.impl.RecipeEntryBTX;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.HasJsonAdapter;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.data.registry.recipe.adapter.RecipeJsonAdapter;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.block.ItemBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeEntryHopperFilter extends RecipeEntryBTX implements HasJsonAdapter {
	public static final String MATCH_NON_BLOCK = "non_block";
	public static final String MATCH_NON_BLOCK_STACKABLE = "non_block_stackable";

	private List<RecipeSymbol> accepts = new ArrayList<>();
	private List<RecipeSymbol> rejects = new ArrayList<>();
	private List<String> includes = new ArrayList<>();
	private @Nullable String match;

	public RecipeEntryHopperFilter(RecipeSymbol filter, List<RecipeSymbol> accepts, List<RecipeSymbol> rejects, List<String> includes, @Nullable String match) {
		super(filter, null);
		this.accepts = accepts;
		this.rejects = rejects;
		this.includes = includes;
		this.match = match;
	}

	public RecipeEntryHopperFilter() {
		super();
	}

	public boolean canProcess(@NotNull ItemStack stack) {
		return canProcess(stack, new HashSet<>());
	}

	private boolean canProcess(@NotNull ItemStack stack, Set<RecipeEntryHopperFilter> visited) {
		if (!visited.add(this)) {
			return false;
		}

		for (RecipeSymbol symbol : rejects) {
			if (symbol.matches(stack)) return false;
		}

		for (RecipeSymbol symbol : accepts) {
			if (symbol.matches(stack)) return true;
		}

		if (match != null && matchesRule(stack.getItem())) {
			return true;
		}

		if (parent instanceof RecipeGroupHopperFilter group) {
			for (String name : includes) {
				RecipeEntryHopperFilter included = group.findByName(name);

				if (included != null && included.canProcess(stack, visited)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean matchesRule(Item item) {
		if (item == null || item instanceof ItemBlock) {
			return false;
		}

		return switch (match) {
			case MATCH_NON_BLOCK -> true;
			case MATCH_NON_BLOCK_STACKABLE -> item.getItemStackLimit(null) > 1;
			default -> false;
		};
	}

	@Override
	public boolean matchesRecipe(@NotNull SearchQuery query) {
		return false; // no output to search for
	}

	@Override
	public RecipeJsonAdapter<?> getAdapter() {
		return new JsonAdapter();
	}

	public static class JsonAdapter implements RecipeJsonAdapter<RecipeEntryHopperFilter> {
		@Override
		public RecipeEntryHopperFilter deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();

			RecipeSymbol filter = context.deserialize(object.get("input"), RecipeSymbol.class);
			List<RecipeSymbol> accepts = readSymbols(object, "accepts", context);
			List<RecipeSymbol> rejects = readSymbols(object, "rejects", context);

			List<String> includes = new ArrayList<>();
			if (object.has("includes")) {
				for (JsonElement e : object.getAsJsonArray("includes")) includes.add(e.getAsString());
			}

			String match = object.has("match") ? object.get("match").getAsString() : null;

			return new RecipeEntryHopperFilter(filter, accepts, rejects, includes, match);
		}

		@Override
		public JsonElement serialize(@NotNull RecipeEntryHopperFilter recipe, Type typeOfSrc, @NotNull JsonSerializationContext context) {
			JsonObject object = new JsonObject();

			object.addProperty("type", Registries.RECIPE_TYPES.getKey(recipe.getClass()));
			object.addProperty("name", recipe.toString());
			object.add("input", context.serialize(recipe.getInput(), RecipeSymbol.class));
			writeSymbols(object, "accepts", recipe.accepts, context);
			writeSymbols(object, "rejects", recipe.rejects, context);

			if (!recipe.includes.isEmpty()) {
				JsonArray array = new JsonArray();
				recipe.includes.forEach(array::add);
				object.add("includes", array);
			}

			if (recipe.match != null) object.addProperty("match", recipe.match);

			return object;
		}

		private static List<RecipeSymbol> readSymbols(JsonObject object, String key, JsonDeserializationContext context) {
			List<RecipeSymbol> symbols = new ArrayList<>();

			if (object.has(key)) {
				for (JsonElement e : object.getAsJsonArray(key)) {
					symbols.add(context.deserialize(e, RecipeSymbol.class));
				}
			}

			return symbols;
		}

		private static void writeSymbols(JsonObject object, String key, List<RecipeSymbol> symbols, JsonSerializationContext context) {
			if (symbols.isEmpty()) return;

			JsonArray array = new JsonArray();
			for (RecipeSymbol symbol : symbols) array.add(context.serialize(symbol, RecipeSymbol.class));
			object.add(key, array);
		}
	}
}
