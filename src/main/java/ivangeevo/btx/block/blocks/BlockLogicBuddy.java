package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.blocks.turntable.TurntableInteraction;
import ivangeevo.btx.util.utils.FacingUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import ivangeevo.btx.util.utils.WorldUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicConduit;
import net.minecraft.core.block.BlockLogicMatcher;
import net.minecraft.core.block.BlockLogicRepeater;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.BlockLogicTimer;
import net.minecraft.core.block.BlockLogicTorchRedstone;
import net.minecraft.core.block.BlockLogicVeryRotatable;
import net.minecraft.core.block.BlockLogicWireRedstone;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BlockLogicBuddy extends BlockLogicVeryRotatable implements TurntableInteraction {
	public static final int MASK_ON = 8;
	private static final int TICK_RATE = 5;

	public BlockLogicBuddy(@NotNull Block<?> block) {
		super(block, Materials.STONE);
	}

	public static BlockLogicSupplier<BlockLogicBuddy> properties() {
		return block -> new BlockLogicBuddy(
			block
				.setTicking(true)
				.withSound(BlockSounds.STONE)
				.withHardness(3.5F)
				.withTags(BlockTags.MINEABLE_BY_PICKAXE)
		);
	}

	@Override
	public int tickDelay() {
		return TICK_RATE;
	}

	@Override
	public void onPlacedByMob(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side, @NotNull Mob mob, double xHit, double yHit) {
		world.setBlockDataNotify(tilePos, setDirection(0, mob.getPlacementDirection(side).opposite()));
	}

	@Override
	public void onPlacedOnSide(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		world.setBlockDataNotify(tilePos, setDirection(0, side.direction().opposite()));
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		if (!WorldUtils.isUpdateScheduled(world, tilePos, this.block)) {
			world.scheduleBlockUpdate(tilePos, this.block, 1);
		}
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		if (!isOn(world.getBlockData(tilePos)) && !doesNotTrigger(block) && !WorldUtils.isUpdateScheduled(world, tilePos, this.block)) {
			world.scheduleBlockUpdate(tilePos, this.block, 1);
		}
	}

	@Override
	public void onMoveComplete(@NotNull World world, @NotNull TilePosc tilePos) {
		notifyOutput(world, tilePos, getDirection(world, tilePos));
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		if (world.isClientSide) {
			return;
		}

		boolean on = isOn(world.getBlockData(tilePos));

		if (isRandomTick) {
			if (on && !WorldUtils.isUpdateScheduled(world, tilePos, this.block)) {
				world.scheduleBlockUpdate(tilePos, this.block, TICK_RATE);
			}
			return;
		}

		if (on) {
			setOn(world, tilePos, false);
		} else {
			setOn(world, tilePos, true);
			world.scheduleBlockUpdate(tilePos, this.block, TICK_RATE);
		}
	}

	public static boolean doesNotTrigger(Block<?> block) {
		BlockLogic logic = block.getLogic();

		return logic instanceof BlockLogicWireRedstone
			|| logic instanceof BlockLogicTorchRedstone
			|| logic instanceof BlockLogicRepeater
			|| logic instanceof BlockLogicConduit
			|| logic instanceof BlockLogicMatcher
			|| logic instanceof BlockLogicTimer
			|| logic instanceof BlockLogicBuddy
			|| BlockLogicDetectorLogic.isLogicBlock(block);
	}

	//------------- State -------------//

	public static Direction getDirection(WorldSource source, TilePosc tilePos) {
		return metaToDirection(source.getBlockData(tilePos));
	}

	public static boolean isOn(int data) {
		return (data & MASK_ON) != 0;
	}

	private void setOn(World world, TilePosc tilePos, boolean on) {
		int data = world.getBlockData(tilePos);

		if (on == isOn(data)) {
			return;
		}

		world.setBlockData(tilePos, on ? data | MASK_ON : data & ~MASK_ON);
		world.markBlockNeedsUpdate(tilePos);

		if (on) {
			SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, "random.click", 0.75F, 2.0F);
		}

		notifyOutput(world, tilePos, metaToDirection(data));
	}

	private void notifyOutput(World world, TilePosc tilePos, Direction facing) {
		TilePos outputPos = tilePos.add(facing, new TilePos());
		world.getBlockType(outputPos).getLogic().onNeighborChanged(world, outputPos, this.block);
		world.notifyBlocksOfNeighborChange(outputPos, this.block);
	}

	//------------- Redstone -------------//

	@Override
	public boolean isSignalSource() {
		return true;
	}

	@Override
	public boolean isEmittingSignal(@NotNull WorldSource source, @NotNull TilePosc tilePos, @NotNull Side side) {
		int data = source.getBlockData(tilePos);
		return isOn(data) && side.direction() == metaToDirection(data).opposite();
	}

	@Override
	public boolean isEmittingDirectSignal(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side) {
		return isEmittingSignal(world, tilePos, side);
	}

	//------------- Turntable -------------//

	@Override
	public boolean canRotateOnTurntable(WorldSource source, TilePosc tilePos) {
		return true;
	}

	@Override
	public int rotateOnTurntable(World world, TilePosc pos, boolean reverse, int craftingCounter) {
		int data = world.getBlockData(pos);
		int facing = FacingUtils.facingFromDirection(metaToDirection(data));
		int rotated = FacingUtils.rotateFacingAroundY(facing, reverse);

		if (rotated != facing) {
			world.setBlockDataNotify(pos, setDirection(data, FacingUtils.directionFromFacing(rotated)));
		}
		return craftingCounter;
	}
}