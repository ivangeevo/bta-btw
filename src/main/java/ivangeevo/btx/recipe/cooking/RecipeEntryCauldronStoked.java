package ivangeevo.btx.recipe.cooking;

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
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

public class RecipeEntryCauldronStoked extends RecipeEntryBulk implements HasJsonAdapter {
	public RecipeEntryCauldronStoked(List<RecipeSymbol> inputs, List<ItemStack> outputs) {
		super(inputs, outputs);
	}

	public RecipeEntryCauldronStoked() {
		super();
	}

	@Override
	public RecipeJsonAdapter<?> getAdapter() {
		return new JsonAdapter();
	}

	public static class JsonAdapter implements RecipeJsonAdapter<RecipeEntryCauldronStoked> {
		@Override
		public RecipeEntryCauldronStoked deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();
			return new RecipeEntryCauldronStoked(parseInputs(object, context), parseOutputs(object, context));
		}

		@Override
		public JsonElement serialize(@NotNull RecipeEntryCauldronStoked recipe, Type typeOfSrc, @NotNull JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			object.addProperty("type", Registries.RECIPE_TYPES.getKey(recipe.getClass()));
			object.addProperty("name", recipe.toString());
			object.add("inputs", serializeInputs(recipe.getInput(), context));
			object.add("outputs", serializeOutputs(recipe.getOutput(), context));
			return object;
		}
	}
}
