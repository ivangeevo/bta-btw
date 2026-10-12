package ivangeevo.btx.recipe.impl;

import net.minecraft.core.data.registry.recipe.RecipeGroup;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;

public class RecipeGroupMillstone extends RecipeGroup<RecipeEntryMillstone> {

	public RecipeGroupMillstone(RecipeSymbol machine) {
		super(machine);
	}

	public RecipeEntryMillstone findRecipe(ItemStack input) {
		if (input != null) {
			for (RecipeEntryMillstone recipe : this.getAllRecipes()) {
				if (recipe.matches(input)) return recipe;
			}
		}

		return null;
	}

	public ItemStack findOutput(ItemStack input) {
		RecipeEntryMillstone recipe = this.findRecipe(input);
		return recipe == null ? null : recipe.getOutput().copy();
	}

}
