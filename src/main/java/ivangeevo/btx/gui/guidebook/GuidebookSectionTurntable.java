package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.turntable.RecipeEntryTurntable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class GuidebookSectionTurntable extends GuidebookSectionBTX<RecipeEntryTurntable> {
	public GuidebookSectionTurntable(String translationKey, ItemStack tabIcon, int bgColor, int fgColor) {
		super(translationKey, tabIcon, bgColor, fgColor);
	}

	@Override
	protected List<RecipeEntryTurntable> getRecipes() {
		return BTXRecipes.TURNTABLE == null ? new ArrayList<>() : new ArrayList<>(BTXRecipes.TURNTABLE.getAllRecipes());
	}

	@Override
	protected boolean matchesQuery(RecipeEntryTurntable recipe, SearchQuery query) {
		return recipe.matchesQueryIgnoreExceptions(query);
	}

	@Override
	protected int getRecipesPerPage() {
		return RecipePageTurntable.RECIPES_PER_PAGE;
	}

	@Override
	protected GuidebookPage createPage(List<RecipeEntryTurntable> recipes) {
		return new RecipePageTurntable(this, recipes);
	}
}