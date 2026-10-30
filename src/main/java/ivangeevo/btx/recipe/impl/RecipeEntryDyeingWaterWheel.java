package ivangeevo.btx.recipe.impl;

import ivangeevo.btx.item.items.ItemWaterWheel;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.entry.RecipeEntryDyeing;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.DyeColor;

public class RecipeEntryDyeingWaterWheel extends RecipeEntryDyeing {
	public RecipeEntryDyeingWaterWheel() {
		super();
	}

	public RecipeEntryDyeingWaterWheel(RecipeSymbol input, ItemStack output) {
		super(input, output, false, false);
	}

	@Override
	public int getOutputMetaFromInputMeta(int meta) {
		return ItemWaterWheel.metaFromColor(DyeColor.colorFromItemMeta(meta).ordinal());
	}
}