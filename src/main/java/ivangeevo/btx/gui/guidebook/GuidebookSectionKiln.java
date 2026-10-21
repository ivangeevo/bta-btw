package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.kiln.RecipeEntryKiln;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class GuidebookSectionKiln extends GuidebookSectionBTX<RecipeEntryKiln> {
	public GuidebookSectionKiln(String translationKey, ItemStack tabIcon, int bgColor, int fgColor) {
		super(translationKey, tabIcon, bgColor, fgColor);
	}

	@Override
	protected List<RecipeEntryKiln> getRecipes() {
		return BTXRecipes.KILN == null ? new ArrayList<>() : new ArrayList<>(BTXRecipes.KILN.getAllRecipes());
	}

	@Override
	protected boolean matchesQuery(RecipeEntryKiln recipe, SearchQuery query) {
		return recipe.matchesQueryIgnoreExceptions(query);
	}

	@Override
	protected int getRecipesPerPage() {
		return RecipePageKiln.RECIPES_PER_PAGE;
	}

	@Override
	protected GuidebookPage createPage(List<RecipeEntryKiln> recipes) {
		return new RecipePageKiln(this, recipes);
	}
}