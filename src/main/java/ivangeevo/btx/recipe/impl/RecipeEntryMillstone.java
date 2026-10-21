package ivangeevo.btx.recipe.impl;

import com.google.gson.*;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.*;
import net.minecraft.core.data.registry.recipe.adapter.RecipeJsonAdapter;
import net.minecraft.core.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RecipeEntryMillstone extends RecipeEntryBTX implements HasJsonAdapter {
	private List<ItemStack> outputs = Collections.emptyList();

	public RecipeEntryMillstone(RecipeSymbol input, ItemStack output) {
		this(input, List.of(output));
	}

	public RecipeEntryMillstone(RecipeSymbol input, List<ItemStack> outputs) {
		super(input, outputs.get(0));
		this.outputs = List.copyOf(outputs);
	}

	public RecipeEntryMillstone() {
		super();
	}

	public List<ItemStack> getOutputs() {
		return this.outputs.isEmpty() ? List.of(this.getOutput()) : this.outputs;
	}

	@Override
	public boolean matchesRecipe(@NotNull SearchQuery query) {
		if (query.query.getLeft() == SearchQuery.QueryType.GROUP && !Objects.equals(query.query.getRight(), "")) {
			List<ItemStack> stacks = new RecipeSymbol(query.query.getRight()).resolve();
			return stacks != null && this.getOutputs().stream().anyMatch(stacks::contains);
		}

		return this.getOutputs().stream().anyMatch(output -> this.matchesName(output, query));
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

			if (object.has("outputs")) {
				List<ItemStack> outputs = new ArrayList<>();
				for (JsonElement element : object.getAsJsonArray("outputs")) {
					outputs.add(context.deserialize(element, ItemStack.class));
				}
				return new RecipeEntryMillstone(input, outputs);
			}

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

			List<ItemStack> outputs = recipe.getOutputs();
			if (outputs.size() > 1) {
				JsonArray array = new JsonArray();
				for (ItemStack output : outputs) {
					array.add(context.serialize(output, ItemStack.class));
				}
				object.add("outputs", array);
			} else {
				object.add("output", context.serialize(recipe.getOutput(), ItemStack.class));
			}

			return object;
		}
	}
}
