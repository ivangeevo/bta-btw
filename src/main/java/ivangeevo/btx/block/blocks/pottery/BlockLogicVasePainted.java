package ivangeevo.btx.block.blocks.pottery;

import ivangeevo.btx.block.BTXBlocks;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.IPainted;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.block.ItemBlockPainted;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockLogicVasePainted extends BlockLogicVase implements IPainted {
	public static final int MASK_COLOR = 0xF;

	protected BlockLogicVasePainted(@NotNull Block<?> block) {
		super(block);
	}

	public static BlockLogicSupplier<BlockLogicVasePainted> paintedProperties() {
		return block -> new BlockLogicVasePainted(
			applyCommon(block).setBlockItem(b -> new ItemBlockPainted<>(b, false))
		);
	}

	@Override
	public int getPlacedData(@Nullable Player player, @NotNull ItemStack itemStack, @NotNull World world,
							 @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		return itemStack.getMetadata() & MASK_COLOR;
	}

	@Override
	public @NotNull DyeColor fromMetadata(int meta) {
		return DyeColor.colorFromBlockMeta(meta & MASK_COLOR);
	}

	@Override
	public int toMetadata(@NotNull DyeColor color) {
		return color.blockMeta & MASK_COLOR;
	}

	@Override
	public int stripColorFromMetadata(int meta) {
		return meta & ~MASK_COLOR;
	}

	@Override
	public void setColor(@NotNull World world, @NotNull TilePosc tilePos, @NotNull DyeColor color) {
		// white is the plain vase
		if (color == DyeColor.WHITE) {
			removeDye(world, tilePos);
			return;
		}

		IPainted.super.setColor(world, tilePos, color);
	}

	@Override
	public void removeDye(@NotNull World world, @NotNull TilePosc tilePos) {
		swapKeepingContents(world, tilePos, BTXBlocks.VASE, 0);
	}
}
