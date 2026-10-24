package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.util.utils.WorldUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicMatcher;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.BlockLogicVeryRotatable;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BlockLogicLens extends BlockLogicVeryRotatable {
	public static final int MAX_RANGE = 128;
	public static final int MASK_LIT = 8;
	private static final int TICK_RATE = 1;
	private static final int MIN_TRIGGER_LIGHT_VALUE = 12;
	private static final int MIN_SENSOR_LIGHT_VALUE = 8;
	private static final int PISTON_IMMOVABLE = 2;

	public BlockLogicLens(@NotNull Block<?> block) {
		super(block, Materials.METAL);
	}

	public static BlockLogicSupplier<BlockLogicLens> properties() {
		return block -> new BlockLogicLens(
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
	public int getPistonPushReaction(@NotNull World world, @NotNull TilePosc tilePos) {
		return PISTON_IMMOVABLE;
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
	public void onRemoved(@NotNull World world, @NotNull TilePosc tilePos, int data) {
		if (!world.isClientSide) {
			BlockLogicDetectorLogic.removeLensBeamFromBlock(world, tilePos, metaToDirection(data), MAX_RANGE);
		}
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		if (!WorldUtils.isUpdatePendingThisTick(world, tilePos, this.block)) {
			scheduleCheck(world, tilePos);
		}
	}

	private void scheduleCheck(World world, TilePosc tilePos) {
		world.scheduleBlockUpdate(tilePos, this.block, tickDelay());
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		if (world.isClientSide) {
			return;
		}

		Direction facing = getDirection(world, tilePos);

		if (!isDirectlyFacingDetector(world, tilePos, facing)) {
			boolean lightOn = checkForInputLight(world, tilePos, facing);

			if (isLit(world.getBlockData(tilePos)) != lightOn) {
				setLit(world, tilePos, lightOn);

				if (lightOn) {
					BlockLogicDetectorLogic.turnBeamOnFromBlock(world, tilePos, facing, MAX_RANGE);
				} else {
					BlockLogicDetectorLogic.turnBeamOffFromBlock(world, tilePos, facing, MAX_RANGE);
				}
			}

			TilePos targetPos = tilePos.add(facing, new TilePos());

			if (world.getBlockType(targetPos) == Blocks.AIR) {
				BlockLogicDetectorLogic.propagateBeamsThroughBlock(world, targetPos);
			}
		} else {
			TilePos sourcePos = tilePos.add(facing.opposite(), new TilePos());
			boolean shouldBeOn = world.getBlockLightValue(sourcePos) >= MIN_SENSOR_LIGHT_VALUE;

			if (isLit(world.getBlockData(tilePos)) != shouldBeOn) {
				setLit(world, tilePos, shouldBeOn);
			}

			scheduleCheck(world, tilePos);
			BlockLogicMatcher.updateNeighboringMatchers(world, tilePos.add(facing, new TilePos()));
		}
	}

	//------------- State -------------//

	public static Direction getDirection(WorldSource source, TilePosc tilePos) {
		return metaToDirection(source.getBlockData(tilePos));
	}

	public static boolean isLit(int data) {
		return (data & MASK_LIT) != 0;
	}

	private void setLit(World world, TilePosc tilePos, boolean lit) {
		int data = world.getBlockData(tilePos);

		if (lit != isLit(data)) {
			world.setBlockDataNotify(tilePos, lit ? data | MASK_LIT : data & ~MASK_LIT);
		}
	}

	private boolean checkForInputLight(World world, TilePosc tilePos, Direction facing) {
		TilePos targetPos = tilePos.add(facing.opposite(), new TilePos());
		Block<?> target = world.getBlockType(targetPos);

		if (target == Blocks.AIR) {
			return false;
		}

		if (target.lightEmission() > MIN_TRIGGER_LIGHT_VALUE) {
			if (target != BTXBlocks.DETECTOR_LOGIC_GLOWING) {
				return true;
			}

			return BlockLogicDetectorLogic.shouldBeProjectingToFacing(world, targetPos, facing);
		}

		return target == this.block && isLit(world.getBlockData(targetPos)) && getDirection(world, targetPos) == facing;
	}

	public static boolean isDirectlyFacingDetector(WorldSource source, TilePosc tilePos, Direction facing) {
		TilePos targetPos = tilePos.add(facing, new TilePos());
		return source.getBlockType(targetPos) == BTXBlocks.DETECTOR_BLOCK
			&& BlockLogicDetector.getDirection(source, targetPos) == facing.opposite();
	}

	public static int getSensedLightLevel(World world, TilePosc tilePos) {
		return world.getBlockLightValue(tilePos.add(getDirection(world, tilePos).opposite(), new TilePos()));
	}
}