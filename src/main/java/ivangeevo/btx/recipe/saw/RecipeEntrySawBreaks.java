package ivangeevo.btx.recipe.saw;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import net.minecraft.core.block.Block;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.HasJsonAdapter;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.adapter.RecipeJsonAdapter;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RecipeEntrySawBreaks extends RecipeEntrySawBase<List<RecipeSymbol>, List<ItemStack>> implements HasJsonAdapter {
	private @Nullable Set<Block<?>> blocks = null;

	public RecipeEntrySawBreaks(List<RecipeSymbol> inputs) {
		super(inputs, List.of());
	}

	public RecipeEntrySawBreaks() {
		super();
	}

	@Override
	public boolean matches(Block<?> block, int data, Direction sawFacing) {
		if (this.blocks == null) {
			this.blocks = resolveBlocks(getInput());
		}

		return this.blocks.contains(block);
	}

	@Override
	public RecipeJsonAdapter<?> getAdapter() {
		return new JsonAdapter();
	}

	public static class JsonAdapter implements RecipeJsonAdapter<RecipeEntrySawBreaks> {
		@Override
		public RecipeEntrySawBreaks deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
			List<RecipeSymbol> inputs = new ArrayList<>();
			for (JsonElement element : json.getAsJsonObject().getAsJsonArray("inputs")) {
				inputs.add(context.deserialize(element, RecipeSymbol.class));
			}

			return new RecipeEntrySawBreaks(inputs);
		}

		@Override
		public JsonElement serialize(@NotNull RecipeEntrySawBreaks recipe, Type typeOfSrc, @NotNull JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			object.addProperty("type", Registries.RECIPE_TYPES.getKey(recipe.getClass()));
			object.addProperty("name", recipe.toString());

			JsonArray inputs = new JsonArray();

			for (RecipeSymbol input : recipe.getInput()) {
				inputs.add(context.serialize(input, RecipeSymbol.class));
			}
			object.add("inputs", inputs);

			return object;
		}
	}
}
