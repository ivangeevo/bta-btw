package ivangeevo.btx.block.blocks.mini_blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.util.IExactHitPlacement;
import ivangeevo.btx.util.rendering.PlacementBounds;
import net.minecraft.core.block.*;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
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

public class BlockLogicCornerPainted extends BlockLogicWoodPiece
	implements IExactHitPlacement, IPainted, BlockLogic.MatcherDataEquivalency, PlacementBounds
{
	protected static final double CORNER_HEIGHT = 0.5D;
	protected static final double CORNER_WIDTH = 0.5D;
	protected static final double CORNER_WIDTH_OFFSET = 1D - CORNER_WIDTH;
	public static final int COLOR_SHIFT = 4;

	public BlockLogicCornerPainted(@NotNull Block<?> block) {
		super(block);
	}

	public static BlockLogicSupplier<BlockLogicCornerPainted> properties() {
		return block -> new BlockLogicCornerPainted(
			block
				.setBlockItem(b -> new ItemBlockPainted<>(b, true))
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

		int colorBits = itemStack.getMetadata() & ~7; // keep only the color part (bits 4+)

		return makeData(x, y, z) | colorBits;
	}

	@Override
	public @NotNull ItemStack @Nullable [] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		return new ItemStack[]{new ItemStack(this, 1, data & ~7)}; // color bits only, no offset flags
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
	public @NotNull DyeColor fromMetadata(int meta) {
		return DyeColor.colorFromBlockMeta(colorIndex(meta));
	}

	@Override
	public int toMetadata(@NotNull DyeColor color) {
		return color.blockMeta << COLOR_SHIFT;
	}

	@Override
	public int stripColorFromMetadata(int meta) {
		return meta & 7; // keep the offset flags, drop the color
	}

	@Override
	public void removeDye(@NotNull World world, @NotNull TilePosc tilePos) {
		int offsets = world.getBlockData(tilePos) & 7;
		world.setBlockTypeDataRaw(tilePos, BTXBlocks.CORNER_OAK, offsets);
		world.notifyBlockChange(tilePos, BTXBlocks.CORNER_OAK);
	}

	@Override
	public void setColor(@NotNull World world, @NotNull TilePosc tilePos, @NotNull DyeColor color) {
		IPainted.super.setColor(world, tilePos, color);
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

	public static int colorIndex(int data) {
		return (data >> COLOR_SHIFT) & 15;
	}

}
