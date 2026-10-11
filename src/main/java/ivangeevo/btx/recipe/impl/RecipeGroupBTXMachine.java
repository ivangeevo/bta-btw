package ivangeevo.btx.recipe.impl;

import net.minecraft.core.data.registry.recipe.RecipeGroup;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;

public class RecipeGroupBTXMachine extends RecipeGroup<RecipeEntryBTXMachine> {

	public RecipeGroupBTXMachine(RecipeSymbol machine) {
		super(machine);
	}

	public RecipeEntryBTXMachine findRecipe(ItemStack input) {
		if (input == null) {
			return null;
		}

		for (RecipeEntryBTXMachine recipe : this.getAllRecipes()) {
			if (recipe.matches(input)) {
				return recipe;
			}
		}

		return null;
	}

	public ItemStack findOutput(ItemStack input) {
		RecipeEntryBTXMachine recipe = this.findRecipe(input);
		return recipe == null ? null : recipe.getOutput().copy();
	}
}
