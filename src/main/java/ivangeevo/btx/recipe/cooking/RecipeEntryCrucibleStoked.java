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

public class RecipeEntryCrucibleStoked extends RecipeEntryBulk implements HasJsonAdapter {
	public RecipeEntryCrucibleStoked(List<RecipeSymbol> inputs, List<ItemStack> outputs) {
		super(inputs, outputs);
	}

	public RecipeEntryCrucibleStoked() {
		super();
	}

	@Override
	public RecipeJsonAdapter<?> getAdapter() {
		return new JsonAdapter();
	}

	public static class JsonAdapter implements RecipeJsonAdapter<RecipeEntryCrucibleStoked> {
		@Override
		public RecipeEntryCrucibleStoked deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();
			return new RecipeEntryCrucibleStoked(parseInputs(object, context), parseOutputs(object, context));
		}

		@Override
		public JsonElement serialize(@NotNull RecipeEntryCrucibleStoked recipe, Type typeOfSrc, @NotNull JsonSerializationContext context) {
			JsonObject object = new JsonObject();

			object.addProperty("type", Registries.RECIPE_TYPES.getKey(recipe.getClass()));
			object.addProperty("name", recipe.toString());
			object.add("inputs", serializeInputs(recipe.getInput(), context));
			object.add("outputs", serializeOutputs(recipe.getOutput(), context));

			return object;
		}
	}
}
