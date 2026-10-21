package ivangeevo.btx.item.items;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.material.ToolMaterial;
import net.minecraft.core.item.tool.ItemToolPickaxe;
import org.jetbrains.annotations.NotNull;

public class ItemToolMattock extends ItemToolPickaxe {
	public ItemToolMattock(@NotNull String name, @NotNull String namespaceId, int id, ToolMaterial material) {
		super(name, namespaceId, id, material);
	}

	@Override
	public float getStrVsBlock(@NotNull ItemStack selfStack, @NotNull Block<?> block) {
		if (block.hasTag(BlockTags.MINEABLE_BY_PICKAXE) || block.hasTag(BlockTags.MINEABLE_BY_SHOVEL)) {
			return this.material.getEfficiency(false);
		}
		return 1.0F;
	}

	@Override
	public boolean canHarvestBlock(@NotNull ItemStack selfStack, @NotNull Mob mob, @NotNull Block<?> block) {
		return super.canHarvestBlock(selfStack, mob, block) || block.hasTag(BlockTags.MINEABLE_BY_SHOVEL);
	}
}
