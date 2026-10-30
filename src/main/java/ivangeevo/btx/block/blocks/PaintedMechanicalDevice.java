package ivangeevo.btx.block.blocks;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.IPainted;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public interface PaintedMechanicalDevice extends IPainted {
	int MASK_COLOR = 0xF0;
	int COLOR_SHIFT = 4;

	Block<?> getUnpaintedBlock();

	static int colorIndex(int data) {
		return (data & MASK_COLOR) >> COLOR_SHIFT;
	}

	static void paint(World world, TilePosc tilePos, Block<?> paintedBlock, DyeColor color) {
		int data = (world.getBlockData(tilePos) & ~MASK_COLOR) | (color.blockMeta << COLOR_SHIFT);
		world.setBlockTypeDataRaw(tilePos, paintedBlock, data);
		world.notifyBlockChange(tilePos, paintedBlock);
		world.markBlockDirty(tilePos);
	}

	@Override
	default @NotNull DyeColor fromMetadata(int meta) {
		return DyeColor.colorFromBlockMeta(colorIndex(meta));
	}

	@Override
	default int toMetadata(@NotNull DyeColor color) {
		return color.blockMeta << COLOR_SHIFT;
	}

	@Override
	default int stripColorFromMetadata(int meta) {
		return meta & ~MASK_COLOR;
	}

	@Override
	default void removeDye(@NotNull World world, @NotNull TilePosc tilePos) {
		Block<?> plain = getUnpaintedBlock();
		world.setBlockTypeDataRaw(tilePos, plain, stripColorFromMetadata(world.getBlockData(tilePos)));
		world.notifyBlockChange(tilePos, plain);
		world.markBlockDirty(tilePos);
	}
}