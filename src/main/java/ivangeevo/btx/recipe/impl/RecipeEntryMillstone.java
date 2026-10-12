package ivangeevo.btx.recipe.impl;

import com.google.gson.*;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.*;
import net.minecraft.core.data.registry.recipe.adapter.RecipeJsonAdapter;
import net.minecraft.core.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class RecipeEntryMillstone extends RecipeEntryBTX implements HasJsonAdapter {

	public RecipeEntryMillstone(RecipeSymbol input, ItemStack output) {
		super(input, output);
	}

	public RecipeEntryMillstone() {
		super();
	}

	@Override
	public RecipeJsonAdapter<?> getAdapter() {
		return new JsonAdapter();
	}

	public static class JsonAdapter implements RecipeJsonAdapter<RecipeEntryMillstone> {

		@Override
		public RecipeEntryMillstone deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException
		{
			JsonObject object = json.getAsJsonObject();

			RecipeSymbol input = context.deserialize(object.get("input"), RecipeSymbol.class);
			ItemStack output = context.deserialize(object.get("output"), ItemStack.class);

			return new RecipeEntryMillstone(input, output);
		}

		@Override
		public JsonElement serialize(@NotNull RecipeEntryMillstone recipe, Type typeOfSrc, @NotNull JsonSerializationContext context)
		{
			JsonObject object = new JsonObject();

			object.addProperty("type", Registries.RECIPE_TYPES.getKey(recipe.getClass()));
			object.addProperty("name", recipe.toString());
			object.add("input", context.serialize(recipe.getInput(), RecipeSymbol.class));
			object.add("output", context.serialize(recipe.getOutput(), ItemStack.class));

			return object;
		}
	}

}
