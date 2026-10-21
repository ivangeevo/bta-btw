package ivangeevo.btx.item.items;

import ivangeevo.btx.util.AreaMiningHelper;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.material.ToolMaterial;
import net.minecraft.core.item.tool.ItemToolPickaxe;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemToolAreaPickaxe extends ItemToolPickaxe implements AreaMiningTool {
	public ItemToolAreaPickaxe(@NotNull String name, @NotNull String namespaceId, int id, ToolMaterial material) {
		super(name, namespaceId, id, material);
	}

	@Override
	public float getBaseStrVsBlock(ItemStack stack, Block<?> block) {
		return super.getStrVsBlock(stack, block);
	}

	@Override
	public float getStrVsBlock(@NotNull ItemStack selfStack, @NotNull Block<?> block) {
		return AreaMiningHelper.getStrVsBlock(selfStack, super.getStrVsBlock(selfStack, block));
	}

	@Override
	public @Nullable ItemStack onUse(@NotNull ItemStack selfStack, @NotNull World world, @NotNull Player player) {
		AreaMiningHelper.toggleAreaMode(selfStack, world, player);
		return selfStack;
	}

	@Override
	public boolean onBlockDestroyed(@NotNull ItemStack selfStack, @NotNull World world, @NotNull Mob mob, @NotNull Block<?> removedBlock, @NotNull TilePosc blockPos, @NotNull Side side) {
		boolean result = super.onBlockDestroyed(selfStack, world, mob, removedBlock, blockPos, side);
		if (mob instanceof Player player && selfStack.stackSize > 0) {
			AreaMiningHelper.breakArea(this, selfStack, world, player, removedBlock, blockPos, side);
		}
		return result;
	}
}
