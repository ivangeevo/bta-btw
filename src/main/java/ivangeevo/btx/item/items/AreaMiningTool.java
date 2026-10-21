package ivangeevo.btx.item.items;

import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.item.ItemStack;

public interface AreaMiningTool {
	float getBaseStrVsBlock(ItemStack stack, Block<?> block);
	boolean canHarvestBlock(ItemStack stack, Mob mob, Block<?> block);
}
