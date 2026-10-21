package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.recipe.BTXItemGroups;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.soulforge.RecipeEntrySoulforge;
import ivangeevo.btx.recipe.soulforge.RecipeEntrySoulforgeBase;
import ivangeevo.btx.recipe.soulforge.RecipeEntrySoulforgeRepair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.core.data.registry.recipe.RecipeEntryBase;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class GuidebookSectionSoulforge extends GuidebookSectionBTX<RecipeEntryBase<?, ?, ?>> {
	private final Map<RecipeEntrySoulforgeRepair, List<RecipeEntrySoulforgeRepairDisplay>> repairDisplays = new IdentityHashMap<>();

	public GuidebookSectionSoulforge(String translationKey, ItemStack tabIcon, int bgColor, int fgColor) {
		super(translationKey, tabIcon, bgColor, fgColor);
	}

	@Override
	protected List<RecipeEntryBase<?, ?, ?>> getRecipes() {
		List<RecipeEntryBase<?, ?, ?>> recipes = new ArrayList<>();
		if (BTXRecipes.SOULFORGE == null) {
			return recipes;
		}
		for (RecipeEntrySoulforgeBase<?, ?> recipe : BTXRecipes.SOULFORGE.getAllRecipes()) {
			if (recipe instanceof RecipeEntrySoulforge shaped) {
				recipes.add(shaped);
			} else if (recipe instanceof RecipeEntrySoulforgeRepair repair) {
				recipes.addAll(this.repairDisplays.computeIfAbsent(repair, source -> List.of(
					new RecipeEntrySoulforgeRepairDisplay(source, BTXItemGroups.SOULFORGED_TOOLS),
					new RecipeEntrySoulforgeRepairDisplay(source, BTXItemGroups.SOULFORGED_ARMOR))));
			}
		}
		return recipes;
	}

	@Override
	protected int getRecipesPerPage() {
		return RecipePageSoulforge.RECIPES_PER_PAGE;
	}

	@Override
	protected GuidebookPage createPage(List<RecipeEntryBase<?, ?, ?>> recipes) {
		return new RecipePageSoulforge(this, recipes);
	}

	@Override
	protected boolean matchesQuery(RecipeEntryBase<?, ?, ?> recipe, SearchQuery query) {
		boolean matches = switch (query.mode) {
			case ALL -> matchesRecipe(recipe, query) || matchesUsage(recipe, query);
			case RECIPE -> matchesRecipe(recipe, query);
			case USAGE -> matchesUsage(recipe, query);
		};
		return matches && matchesScope(recipe, query);
	}

	private static boolean matchesRecipe(RecipeEntryBase<?, ?, ?> recipe, SearchQuery query) {
		if (recipe instanceof RecipeEntrySoulforge shaped) {
			return matchesStack(shaped.getOutput(), query);
		}
		if (recipe instanceof RecipeEntrySoulforgeRepairDisplay repair) {
			return matchesSymbol(repair.getOutput(), query);
		}
		return false;
	}

	private static boolean matchesUsage(RecipeEntryBase<?, ?, ?> recipe, SearchQuery query) {
		if (recipe instanceof RecipeEntrySoulforgeRepairDisplay repair) {
			return matchesSymbol(repair.getInput(), query) || matchesSymbol(repair.getIngot(), query);
		}
		if (!(recipe instanceof RecipeEntrySoulforge shaped)) {
			return false;
		}
		for (int y = 0; y < shaped.getHeight(); y++) {
			for (int x = 0; x < shaped.getWidth(); x++) {
				if (matchesSymbol(shaped.getSymbol(x, y), query)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean matchesSymbol(RecipeSymbol symbol, SearchQuery query) {
		if (symbol == null) {
			return false;
		}
		for (ItemStack stack : symbol.resolve()) {
			if (matchesStack(stack, query)) {
				return true;
			}
		}
		return false;
	}
}