package ivangeevo.btx.recipe.cooking;

import net.minecraft.core.data.registry.recipe.RecipeGroup;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.Container;

import java.util.List;

public class RecipeGroupBulk extends RecipeGroup<RecipeEntryBulk> {
	public RecipeGroupBulk(RecipeSymbol machine) {
		super(machine);
	}

	public RecipeEntryBulk findRecipe(Container inventory) {
		RecipeEntryBulk best = null;
		for (RecipeEntryBulk recipe : getAllRecipes()) {
			if (recipe.doesInventoryContainIngredients(inventory)
				&& (best == null || recipe.getInput().size() > best.getInput().size())) {
				best = recipe;
			}
		}

		return best;
	}

	public List<ItemStack> getCraftingResult(Container inventory) {
		RecipeEntryBulk recipe = findRecipe(inventory);
		return recipe == null ? null : recipe.getCraftingOutputList();
	}

	public List<ItemStack> consumeIngredientsAndReturnResult(Container inventory) {
		RecipeEntryBulk recipe = findRecipe(inventory);

		if (recipe == null) {
			return null;
		}

		recipe.consumeInventoryIngredients(inventory);

		return recipe.getCraftingOutputList();
	}
}
