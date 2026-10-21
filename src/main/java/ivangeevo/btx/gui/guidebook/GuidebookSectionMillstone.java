package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.impl.RecipeEntryMillstone;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class GuidebookSectionMillstone extends GuidebookSectionBTX<RecipeEntryMillstone> {
	public GuidebookSectionMillstone(String translationKey, ItemStack tabIcon, int bgColor, int fgColor) {
		super(translationKey, tabIcon, bgColor, fgColor);
	}

	@Override
	protected List<RecipeEntryMillstone> getRecipes() {
		return BTXRecipes.MILLSTONE == null ? new ArrayList<>() : new ArrayList<>(BTXRecipes.MILLSTONE.getAllRecipes());
	}

	@Override
	protected boolean matchesQuery(RecipeEntryMillstone recipe, SearchQuery query) {
		return recipe.matchesQueryIgnoreExceptions(query);
	}

	@Override
	protected int getRecipesPerPage() {
		return RecipePageMillstone.RECIPES_PER_PAGE;
	}

	@Override
	protected GuidebookPage createPage(List<RecipeEntryMillstone> recipes) {
		return new RecipePageMillstone(this, recipes);
	}
}