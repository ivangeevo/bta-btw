package ivangeevo.btx.block.blocks.mini_blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.util.IExactHitPlacement;
import ivangeevo.btx.util.rendering.PlacementBounds;
import ivangeevo.btx.util.utils.FacingUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.IPaintable;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.block.ItemBlockPainted;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

public class BlockLogicCorner extends BlockLogicWoodPiece implements IExactHitPlacement, IPaintable, PlacementBounds {
	protected static final double CORNER_HEIGHT = 0.5D;
	protected static final double CORNER_WIDTH = 0.5D;

	protected static final double CORNER_WIDTH_OFFSET = 1D - CORNER_WIDTH;

	public BlockLogicCorner(@NotNull Block<?> block) {
		super(block);
	}

	public static BlockLogicSupplier<BlockLogicCorner> properties() {
		return block -> new BlockLogicCorner(
			block
				.withSound(BlockSounds.WOOD)
				.withHardness(2.0F)
				.withBlastResistance(5.0F)
				.withTags(BlockTags.MINEABLE_BY_AXE)
		);
	}

	@Override
	public @NotNull AABBdc getBoundsFromState(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return placementBounds(source.getBlockData(tilePos));
	}

	@Override
	public AABBdc placementBounds(int data) {
		AABBd box = new AABBd(0D, 0D, 0D, CORNER_WIDTH, CORNER_HEIGHT, CORNER_WIDTH);
		box.translate(
			isXOffset(data) ? CORNER_WIDTH_OFFSET : 0D,
			isYOffset(data) ? CORNER_WIDTH_OFFSET : 0D,
			isZOffset(data) ? CORNER_WIDTH_OFFSET : 0D
		);
		return box;
	}

	@Override
	public int getPlacedData(@Nullable Player player, @NotNull ItemStack itemStack, @NotNull World world,
							 @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		double[] f = IExactHitPlacement.decode(side, xHit, yHit);

		boolean x = side.offsetX() != 0 ? side.offsetX() < 0 : isClickOffsetOnAxis(f[0]);
		boolean y = side.offsetY() != 0 ? side.offsetY() < 0 : isClickOffsetOnAxis(f[1]);
		boolean z = side.offsetZ() != 0 ? side.offsetZ() < 0 : isClickOffsetOnAxis(f[2]);
		return makeData(x, y, z);
	}

	@Override
	public boolean canRotateOnTurntable(WorldSource source, TilePosc tilePos) {
		return !isYOffset(source.getBlockData(tilePos)); // everything but upper corners because they float
	}

	@Override
	public boolean canTransmitRotationVertically(WorldSource source, TilePosc tilePos) {
		return false;
	}

	@Override
	public boolean canTransmitRotationHorizontally(WorldSource source, TilePosc tilePos) {
		return false;
	}

	@Override
	public void setColor(@NotNull World world, @NotNull TilePosc tilePos, @NotNull DyeColor color) {
		int facing = getOffsets(world.getBlockData(tilePos));
		world.setBlockTypeDataRaw(tilePos, BTXBlocks.CORNER_OAK_PAINTED, facing);
		world.notifyBlockChange(tilePos, BTXBlocks.CORNER_OAK_PAINTED);
		BTXBlocks.CORNER_OAK_PAINTED.getLogic().setColor(world, tilePos, color);
	}

	public static boolean isXOffset(int data) {
		return (data & 4) != 0;
	}

	public static boolean isYOffset(int data) {
		return (data & 2) != 0;
	}

	public static boolean isZOffset(int data) {
		return (data & 1) != 0;
	}

	public static int makeData(boolean x, boolean y, boolean z) {
		return (x ? 4 : 0) | (y ? 2 : 0) | (z ? 1 : 0);
	}

	/** axis = 0 is x, 1 is y, 2 is z. Returns +1 if the corner sits on the positive half of that axis, else -1. */
	public static int getCornerAlignmentOffsetAlongAxis(int cornerData, int axis) {
		return (cornerData & (4 >> axis)) != 0 ? 1 : -1;
	}

	public static int getOffsets(int data) {
		return data & 7;
	}

}
