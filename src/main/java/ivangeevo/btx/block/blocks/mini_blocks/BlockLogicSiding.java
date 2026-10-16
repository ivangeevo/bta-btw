package ivangeevo.btx.block.blocks.mini_blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicGearBox;
import ivangeevo.btx.util.rendering.PlacementBounds;
import ivangeevo.btx.util.utils.FacingUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.IPaintable;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
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

public class BlockLogicSiding extends BlockLogicWoodPiece implements IPaintable, PlacementBounds {
	protected static final double SIDING_HEIGHT = 0.5D;

	public BlockLogicSiding(@NotNull Block<?> block) {
		super(block);
	}

	public static BlockLogicSupplier<BlockLogicSiding> properties() {
		return block -> new BlockLogicSiding(
			block
				.withSound(BlockSounds.WOOD)
				.withHardness(2.0F)
				.withBlastResistance(5.0F)
				.withTags(BlockTags.MINEABLE_BY_AXE)
		);
	}

	@Override
	public @NotNull AABBdc getBoundsFromState(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		AABBd box = new AABBd(0D, 0D, 0D, 1D, SIDING_HEIGHT, 1D);
		return FacingUtils.tiltToFacingAlongY(box, getFacing(source.getBlockData(tilePos)));
	}

	@Override
	public int getPlacedData(@Nullable Player player, @NotNull ItemStack itemStack, @NotNull World world,
							 @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		return setFacing(0, side.id);
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
	public void setColor(@NotNull World world, @NotNull TilePosc tilePos, @NotNull DyeColor color) {
		int facing = getFacing(world.getBlockData(tilePos));
		world.setBlockTypeDataRaw(tilePos, BTXBlocks.SIDING_OAK_PAINTED, facing);
		world.notifyBlockChange(tilePos, BTXBlocks.SIDING_OAK_PAINTED);
		BTXBlocks.SIDING_OAK_PAINTED.getLogic().setColor(world, tilePos, color);
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
}
