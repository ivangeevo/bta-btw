package ivangeevo.btx.recipe.kiln;

import com.google.gson.*;
import ivangeevo.btx.recipe.impl.RecipeEntryBTX;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.HasJsonAdapter;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.adapter.RecipeJsonAdapter;
import net.minecraft.core.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** A block the kiln can cook: input = the block above the kiln, output = what it becomes. */
public class RecipeEntryKiln extends RecipeEntryBTX implements HasJsonAdapter {
	private int cookTimeMultiplier = 1;
	private List<ItemStack> extraOutputs = new ArrayList<>();

	public RecipeEntryKiln(RecipeSymbol input, ItemStack output, int cookTimeMultiplier, List<ItemStack> extraOutputs) {
		this(input, output, cookTimeMultiplier);
		this.extraOutputs = extraOutputs;
	}

	public RecipeEntryKiln(RecipeSymbol input, ItemStack output, int cookTimeMultiplier) {
		super(input, output);
		this.cookTimeMultiplier = Math.max(1, cookTimeMultiplier);
	}

	public RecipeEntryKiln() {
		super();
	}

	/** Primary output first, then any extras. */
	public List<ItemStack> getOutputs() {
		List<ItemStack> all = new ArrayList<>();
		all.add(getOutput());
		all.addAll(extraOutputs);
		return all;
	}

	/** BTW: getCookTimeMultiplierInKiLn. Multiplies the tick delay between cook steps. */
	public int getCookTimeMultiplier() {
		return cookTimeMultiplier;
	}

	@Override
	public RecipeJsonAdapter<?> getAdapter() {
		return new JsonAdapter();
	}

	public static class JsonAdapter implements RecipeJsonAdapter<RecipeEntryKiln> {

		@Override
		public RecipeEntryKiln deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();

			RecipeSymbol input = context.deserialize(object.get("input"), RecipeSymbol.class);
			ItemStack output = context.deserialize(object.get("output"), ItemStack.class);
			int multiplier = object.has("cook_time_multiplier") ? object.get("cook_time_multiplier").getAsInt() : 1;

			List<ItemStack> extras = new java.util.ArrayList<>();
			if (object.has("extra_outputs")) {
				for (JsonElement e : object.getAsJsonArray("extra_outputs")) {
					extras.add(context.deserialize(e, ItemStack.class));
				}
			}

			return new RecipeEntryKiln(input, output, multiplier, extras);
		}

		@Override
		public JsonElement serialize(@NotNull RecipeEntryKiln recipe, Type typeOfSrc, @NotNull JsonSerializationContext context) {
			JsonObject object = new JsonObject();

			object.addProperty("type", Registries.RECIPE_TYPES.getKey(recipe.getClass()));
			object.addProperty("name", recipe.toString());
			object.add("input", context.serialize(recipe.getInput(), RecipeSymbol.class));
			object.add("output", context.serialize(recipe.getOutput(), ItemStack.class));
			object.addProperty("cook_time_multiplier", recipe.getCookTimeMultiplier());
			if (!recipe.extraOutputs.isEmpty()) {
				JsonArray extras = new JsonArray();
				for (ItemStack s : recipe.extraOutputs) extras.add(context.serialize(s, ItemStack.class));
				object.add("extra_outputs", extras);
			}

			return object;
		}
	}
}
