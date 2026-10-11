package ivangeevo.btx.recipe.impl;

import com.google.gson.*;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.adapter.RecipeJsonAdapter;
import net.minecraft.core.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class RecipeBTXMachineJsonAdapter implements RecipeJsonAdapter<RecipeEntryBTXMachine> {

	@Override
	public RecipeEntryBTXMachine deserialize(
		@NotNull JsonElement json,
		Type typeOfT,
		@NotNull JsonDeserializationContext context
	) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();

		RecipeSymbol input = context.deserialize(
			object.get("input"),
			RecipeSymbol.class
		);

		ItemStack output = context.deserialize(
			object.get("output"),
			ItemStack.class
		);

		int time = object.get("time").getAsInt();

		if (time < 0) {
			throw new JsonParseException(
				"BTX machine recipe time cannot be negative: " + time
			);
		}

		return new RecipeEntryBTXMachine(input, output, time);
	}

	@Override
	public JsonElement serialize(
		@NotNull RecipeEntryBTXMachine recipe,
		Type typeOfSrc,
		@NotNull JsonSerializationContext context
	) {
		JsonObject object = new JsonObject();

		object.addProperty("name", recipe.toString());
		object.addProperty(
			"type",
			Registries.RECIPE_TYPES.getKey(recipe.getClass())
		);

		object.add(
			"input",
			context.serialize(recipe.getInput(), RecipeSymbol.class)
		);

		object.add(
			"output",
			context.serialize(recipe.getOutput(), ItemStack.class)
		);

		object.addProperty("time", recipe.getData());

		return object;
	}
}
