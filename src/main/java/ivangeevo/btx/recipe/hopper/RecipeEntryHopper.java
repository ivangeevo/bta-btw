package ivangeevo.btx.recipe.hopper;

import com.google.gson.*;
import ivangeevo.btx.recipe.impl.RecipeEntryBTX;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.HasJsonAdapter;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.data.registry.recipe.adapter.RecipeJsonAdapter;
import net.minecraft.core.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class RecipeEntryHopper extends RecipeEntryBTX implements HasJsonAdapter {
	private RecipeSymbol filter;
	private @Nullable ItemStack filteredOutput;
	private boolean souls;

	public RecipeEntryHopper(RecipeSymbol input, RecipeSymbol filter, @Nullable ItemStack output, @Nullable ItemStack filteredOutput, boolean souls) {
		super(input, output);
		this.filter = filter;
		this.filteredOutput = filteredOutput;
		this.souls = souls;
	}

	public RecipeEntryHopper() {
		super();
	}

	public boolean matches(ItemStack stack, @Nullable ItemStack filterStack) {
		return filterStack != null && matches(stack) && filter.matches(filterStack);
	}

	public @Nullable ItemStack getHopperOutput() {
		return getOutput();
	}

	public @Nullable ItemStack getFilteredOutput() {
		return filteredOutput;
	}

	public boolean containsSouls() {
		return souls;
	}

	@Override
	public boolean matchesRecipe(@NotNull SearchQuery query) {
		return getOutput() != null && super.matchesRecipe(query);
	}

	@Override
	public RecipeJsonAdapter<?> getAdapter() {
		return new JsonAdapter();
	}

	public static class JsonAdapter implements RecipeJsonAdapter<RecipeEntryHopper> {
		@Override
		public RecipeEntryHopper deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();

			RecipeSymbol input = context.deserialize(object.get("input"), RecipeSymbol.class);
			RecipeSymbol filter = context.deserialize(object.get("filter"), RecipeSymbol.class);
			ItemStack output = object.has("output") ? context.deserialize(object.get("output"), ItemStack.class) : null;
			ItemStack filteredOutput = object.has("filtered_output") ? context.deserialize(object.get("filtered_output"), ItemStack.class) : null;
			boolean souls = object.has("souls") && object.get("souls").getAsBoolean();

			return new RecipeEntryHopper(input, filter, output, filteredOutput, souls);
		}

		@Override
		public JsonElement serialize(@NotNull RecipeEntryHopper recipe, Type typeOfSrc, @NotNull JsonSerializationContext context) {
			JsonObject object = new JsonObject();

			object.addProperty("type", Registries.RECIPE_TYPES.getKey(recipe.getClass()));
			object.addProperty("name", recipe.toString());
			object.add("input", context.serialize(recipe.getInput(), RecipeSymbol.class));
			object.add("filter", context.serialize(recipe.filter, RecipeSymbol.class));
			if (recipe.getOutput() != null) object.add("output", context.serialize(recipe.getOutput(), ItemStack.class));
			if (recipe.filteredOutput != null) object.add("filtered_output", context.serialize(recipe.filteredOutput, ItemStack.class));
			if (recipe.souls) object.addProperty("souls", true);

			return object;
		}
	}
}
