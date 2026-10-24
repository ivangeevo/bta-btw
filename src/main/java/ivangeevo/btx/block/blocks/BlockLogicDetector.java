package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.turntable.TurntableInteraction;
import ivangeevo.btx.util.utils.FacingUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import ivangeevo.btx.util.utils.WorldUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicCropsWheat;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.BlockLogicVeryRotatable;
import net.minecraft.core.block.Blocks;
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

public class BlockLogicDetector extends BlockLogicVeryRotatable implements TurntableInteraction {
	public static final int MASK_ON = 8;
	private static final int TICK_RATE = 4;
	private static final int PARTICLE_BRIGHTNESS = 10;

	public BlockLogicDetector(@NotNull Block<?> block) {
		super(block, Materials.STONE);
	}

	public static BlockLogicSupplier<BlockLogicDetector> properties() {
		return block -> new BlockLogicDetector(
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
		scheduleCheck(world, tilePos);
	}

	@Override
	public void onPlacedOnSide(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		world.setBlockDataNotify(tilePos, setDirection(0, side.direction().opposite()));
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		scheduleCheck(world, tilePos);
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		if (!WorldUtils.isUpdatePendingThisTick(world, tilePos, this.block)) {
			world.scheduleBlockUpdate(tilePos, this.block, tickDelay());
		}
	}

	@Override
	public void onMoveComplete(@NotNull World world, @NotNull TilePosc tilePos) {
		scheduleCheck(world, tilePos);
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		if (world.isClientSide) {
			return;
		}

		if (isRandomTick) {
			if (getDirection(world, tilePos) == Direction.UP || checkForDetection(world, tilePos) != isOn(world.getBlockData(tilePos))) {
				scheduleCheck(world, tilePos);
			}
			return;
		}

		boolean placedLogic = placeDetectorLogicIfNecessary(world, tilePos);
		boolean detected = checkForDetection(world, tilePos);
		Direction facing = getDirection(world, tilePos);

		if (facing == Direction.UP) {
			if (!detected && world.isBlockBeingRainedOn(tilePos.add(facing, new TilePos()))) {
				detected = true;
			}

			world.scheduleBlockUpdate(tilePos, this.block, tickDelay());
		}

		if (detected) {
			setOn(world, tilePos, true);
		} else if (isOn(world.getBlockData(tilePos))) {
			if (!placedLogic) {
				setOn(world, tilePos, false);
			} else {
				world.scheduleBlockUpdate(tilePos, this.block, tickDelay());
			}
		}
	}

	private void scheduleCheck(World world, TilePosc tilePos) {
		if (!WorldUtils.isUpdateScheduled(world, tilePos, this.block)) {
			world.scheduleBlockUpdate(tilePos, this.block, tickDelay());
		}
	}

	//------------- Detection -------------//

	private static boolean placeDetectorLogicIfNecessary(World world, TilePosc tilePos) {
		TilePos targetPos = tilePos.add(getDirection(world, tilePos), new TilePos());
		Block<?> target = world.getBlockType(targetPos);

		if (target == Blocks.AIR) {
			world.setBlockTypeData(targetPos, BTXBlocks.DETECTOR_LOGIC, 0);
			BlockLogicDetectorLogic.setFlag(world, targetPos, BlockLogicDetectorLogic.MASK_DETECTOR_LOGIC, true);
			BlockLogicDetectorLogic.fullyValidateBlock(world, targetPos);
			return true;
		}

		if (BlockLogicDetectorLogic.isLogicBlock(target) && !BlockLogicDetectorLogic.isDetectorLogicFlagOn(world, targetPos)) {
			BlockLogicDetectorLogic.setFlag(world, targetPos, BlockLogicDetectorLogic.MASK_DETECTOR_LOGIC, true);

			if (BlockLogicDetectorLogic.hasValidLensSource(world, targetPos, null)) {
				BlockLogicDetectorLogic.setFlag(world, targetPos, BlockLogicDetectorLogic.MASK_INTERSECTION_POINT, true);
			}
		}

		return false;
	}

	private static boolean checkForDetection(World world, TilePosc tilePos) {
		Direction facing = getDirection(world, tilePos);
		TilePos targetPos = tilePos.add(facing, new TilePos());
		Block<?> target = world.getBlockType(targetPos);

		if (target == Blocks.AIR) {
			return false;
		}

		if (BlockLogicDetectorLogic.isLogicBlock(target)) {
			return BlockLogicDetectorLogic.isEntityCollidingFlagOn(world, targetPos)
				|| BlockLogicDetectorLogic.isLitFlagOn(world, targetPos)
				|| isAboveMatureCrop(world, targetPos);
		}

		if (target == BTXBlocks.LENS && BlockLogicLens.getDirection(world, targetPos) == facing.opposite()) {
			return BlockLogicLens.isLit(world.getBlockData(targetPos));
		}

		return true;
	}

	private static boolean isAboveMatureCrop(World world, TilePosc pos) {
		TilePos belowPos = pos.down(new TilePos());
		Block<?> below = world.getBlockType(belowPos);
		int growth = world.getBlockData(belowPos) & 7;

		if (below == Blocks.CROPS_WHEAT) {
			return growth >= BlockLogicCropsWheat.MAX_GROWTH_STATE;
		}

		return below == BTXBlocks.HEMP && growth >= BlockLogicCropsHemp.MAX_GROWTH_STATE;
	}

	//------------- Matcher -------------//

	@Override
	public boolean isEquivalent(@NotNull WorldSource world, @NotNull TilePosc tilePos, @NotNull TilePosc otherPos, @NotNull Block<?> otherBlock) {
		if (otherBlock == this.block && world instanceof World w) {
			int level = getLightSensorLevel(w, tilePos);
			int otherLevel = getLightSensorLevel(w, otherPos);

			if (level >= 0 && otherLevel >= 0) {
				return level == otherLevel;
			}
		}

		return super.isEquivalent(world, tilePos, otherPos, otherBlock);
	}

	private static int getLightSensorLevel(World world, TilePosc tilePos) {
		Direction facing = getDirection(world, tilePos);
		TilePos lensPos = tilePos.add(facing, new TilePos());

		if (world.getBlockType(lensPos) == BTXBlocks.LENS && BlockLogicLens.getDirection(world, lensPos) == facing.opposite()) {
			return BlockLogicLens.getSensedLightLevel(world, lensPos);
		}

		return -1;
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

		world.setBlockDataNotify(tilePos, on ? data | MASK_ON : data & ~MASK_ON);

		if (on) {
			SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, "random.click", 0.75F, 2.0F);
		}
	}

	//------------- Redstone -------------//

	@Override
	public boolean isSignalSource() {
		return true;
	}

	@Override
	public boolean isEmittingSignal(@NotNull WorldSource source, @NotNull TilePosc tilePos, @NotNull Side side) {
		return isOn(source.getBlockData(tilePos));
	}

	@Override
	public boolean isEmittingDirectSignal(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side) {
		return false;
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
			scheduleCheck(world, pos);
		}

		return craftingCounter;
	}

	//------------- Rendering -------------//

	@Override
	public void animationTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand) {
		if (!isOn(world.getBlockData(tilePos))) {
			return;
		}

		Direction facing = getDirection(world, tilePos);
		double offset = rand.nextBoolean() ? 0.33D : 0.66D;
		double x = tilePos.x() + 0.5D + facing.offsetX() * 0.6D;
		double y = tilePos.y() + 0.75D + facing.offsetY() * 0.6D;
		double z = tilePos.z() + 0.5D + facing.offsetZ() * 0.6D;

		if (facing.offsetY() != 0) {
			x = tilePos.x() + offset;
			y = tilePos.y() + 0.5D + facing.offsetY() * 0.6D;
			z = tilePos.z() + 0.25D;
		} else if (facing.offsetX() != 0) {
			z = tilePos.z() + offset;
		} else {
			x = tilePos.x() + offset;
		}

		x += (rand.nextFloat() - 0.5F) * 0.1F;
		y += (rand.nextFloat() - 0.5F) * 0.1F;
		z += (rand.nextFloat() - 0.5F) * 0.1F;

		world.spawnParticle("reddust", x, y, z, 0.0D, 0.0D, 0.0D, PARTICLE_BRIGHTNESS, false);
	}
}
