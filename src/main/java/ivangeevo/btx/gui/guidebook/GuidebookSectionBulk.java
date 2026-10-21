package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.recipe.cooking.RecipeEntryBulk;
import ivangeevo.btx.recipe.cooking.RecipeGroupBulk;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class GuidebookSectionBulk extends GuidebookSectionBTX<RecipeEntryBulk> {
	private final Supplier<RecipeGroupBulk> normalGroup;
	private final Supplier<RecipeGroupBulk> stokedGroup;
	private final Set<RecipeEntryBulk> stokedRecipes = Collections.newSetFromMap(new IdentityHashMap<>());

	public GuidebookSectionBulk(String translationKey, ItemStack tabIcon, int bgColor, int fgColor, Supplier<RecipeGroupBulk> normalGroup, Supplier<RecipeGroupBulk> stokedGroup) {
		super(translationKey, tabIcon, bgColor, fgColor);
		this.normalGroup = normalGroup;
		this.stokedGroup = stokedGroup;
	}

	@Override
	protected List<RecipeEntryBulk> getRecipes() {
		List<RecipeEntryBulk> recipes = new ArrayList<>();
		RecipeGroupBulk normal = this.normalGroup.get();
		if (normal != null) {
			recipes.addAll(normal.getAllRecipes());
		}

		this.stokedRecipes.clear();
		RecipeGroupBulk stoked = this.stokedGroup.get();
		if (stoked != null) {
			List<RecipeEntryBulk> stokedList = stoked.getAllRecipes();
			this.stokedRecipes.addAll(stokedList);
			recipes.addAll(stokedList);
		}
		return recipes;
	}

	@Override
	protected boolean matchesQuery(RecipeEntryBulk recipe, SearchQuery query) {
		boolean matches = switch (query.mode) {
			case ALL -> matchesRecipe(recipe, query) || matchesUsage(recipe, query);
			case RECIPE -> matchesRecipe(recipe, query);
			case USAGE -> matchesUsage(recipe, query);
		};
		return matches && matchesScope(recipe, query);
	}

	private static boolean matchesRecipe(RecipeEntryBulk recipe, SearchQuery query) {
		for (ItemStack output : recipe.getOutput()) {
			if (matchesStack(output, query)) {
				return true;
			}
		}
		return false;
	}

	private static boolean matchesUsage(RecipeEntryBulk recipe, SearchQuery query) {
		for (RecipeSymbol symbol : recipe.getInput()) {
			for (ItemStack stack : symbol.resolve()) {
				if (matchesStack(stack, query)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected int getRecipesPerPage() {
		return RecipePageBulk.RECIPES_PER_PAGE;
	}

	@Override
	protected GuidebookPage createPage(List<RecipeEntryBulk> recipes) {
		return new RecipePageBulk(this, recipes, this.stokedRecipes::contains);
	}
}