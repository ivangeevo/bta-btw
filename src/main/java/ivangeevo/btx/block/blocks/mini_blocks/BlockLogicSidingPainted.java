package ivangeevo.btx.block.blocks.mini_blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.util.rendering.PlacementBounds;
import ivangeevo.btx.util.utils.FacingUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.IPainted;
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

public class BlockLogicSidingPainted extends BlockLogicWoodPiece implements IPainted, BlockLogic.MatcherDataEquivalency, PlacementBounds {
	protected static final double SIDING_HEIGHT = 0.5D;
	public static final int MASK_COLOR = 0xF0;
	public static final int COLOR_SHIFT = 4;

	protected BlockLogicSidingPainted(@NotNull Block<?> block) {
		super(block);
	}

	public static BlockLogicSupplier<BlockLogicSidingPainted> properties() {
		return block -> new BlockLogicSidingPainted(
			block
				.setBlockItem(b -> new ItemBlockPainted<>(b, true))
				.withSound(BlockSounds.WOOD)
				.withHardness(2.0F)
				.withBlastResistance(5.0F)
				.withTags(BlockTags.MINEABLE_BY_AXE)
		);
	}

	@Override
	public int getPlacedData(@Nullable Player player, @NotNull ItemStack itemStack, @NotNull World world,
							 @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		int color = colorIndex(itemStack.getMetadata());
		return setFacing(color << COLOR_SHIFT, side.id);
	}

	@Override
	public ItemStack[] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		return new ItemStack[]{new ItemStack(this, 1, data & MASK_COLOR)}; // color bits only, no facing
	}

	@Override
	public @NotNull AABBdc getBoundsFromState(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		AABBd box = new AABBd(0D, 0D, 0D, 1D, SIDING_HEIGHT, 1D);
		return FacingUtils.tiltToFacingAlongY(box, getFacing(source.getBlockData(tilePos)));
	}

	@Override
	public boolean canRotateOnTurntable(WorldSource source, TilePosc tilePos) {
		return getFacing(source.getBlockData(tilePos)) != 0; // a top-mounted slab can't be turned
	}

	@Override
	public boolean canTransmitRotationVertically(WorldSource source, TilePosc tilePos) {
		return getFacing(source.getBlockData(tilePos)) > 1; // sideways sidings span the full height
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
		return meta & MASK_COLOR; // keep facing, drop color
	}

	@Override
	public void removeDye(@NotNull World world, @NotNull TilePosc tilePos) {
		int facing = getFacing(world.getBlockData(tilePos));
		world.setBlockTypeDataRaw(tilePos, BTXBlocks.SIDING_OAK, facing);
		world.notifyBlockChange(tilePos, BTXBlocks.SIDING_OAK);
	}

	@Override
	public void setColor(@NotNull World world, @NotNull TilePosc tilePos, @NotNull DyeColor color) {
		IPainted.super.setColor(world, tilePos, color);
	}

	@Override
	public AABBdc placementBounds(int data) {
		return FacingUtils.tiltToFacingAlongY(new AABBd(0, 0, 0, 1, SIDING_HEIGHT, 1), getFacing(data));
	}

	public static int getFacing(int data) {
		return data & 7;
	}

	public static int setFacing(int data, int facing) {
		return (data & ~7) | (facing & 7);
	}

	public static int colorIndex(int data) {
		return (data >> COLOR_SHIFT) & 15;
	}

}
