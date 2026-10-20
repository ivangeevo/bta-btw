package ivangeevo.btx.recipe.impl;

import net.minecraft.core.data.registry.recipe.RecipeGroup;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

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

	public List<ItemStack> findOutputs(ItemStack input) {
		RecipeEntryMillstone recipe = this.findRecipe(input);
		if (recipe == null) return null;

		List<ItemStack> outputs = new ArrayList<>();
		for (ItemStack output : recipe.getOutputs()) {
			outputs.add(output.copy());
		}
		return outputs;
	}
}
