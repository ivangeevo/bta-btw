package ivangeevo.btx.recipe.impl;

import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.entry.RecipeEntryCraftingShapeless;
import net.minecraft.core.item.ItemStack;

import java.util.List;

public class RecipeEntryCraftingShapelessConsuming extends RecipeEntryCraftingShapeless {
	public RecipeEntryCraftingShapelessConsuming(List<RecipeSymbol> inputs, ItemStack output) {
		super(inputs, output);
	}

	public RecipeEntryCraftingShapelessConsuming() {
		super();
	}

	@Override
	public boolean consumesContainer() {
		return true;
	}
}