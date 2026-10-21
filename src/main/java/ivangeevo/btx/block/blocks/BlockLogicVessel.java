package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.sound.BTXSoundTypes;
import ivangeevo.btx.util.utils.FacingUtils;
import ivangeevo.btx.util.utils.MechPowerUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import ivangeevo.btx.util.utils.WorldUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

import java.util.Random;

public abstract class BlockLogicVessel extends BlockLogic implements MechanicalDevice {
	private static final int TICK_RATE = 1;
	public static final double COLLISION_BOX_HEIGHT = 1F;

	public BlockLogicVessel(@NotNull Block<?> block, @NotNull Material material) {
		super(block, material);
	}

	@Override
	public int tickDelay() {
		return TICK_RATE;
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		super.onPlacedByWorld(world, tilePos);
		world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
	}

	@Override
	public boolean isSolidRender() {
		return false;
	}

	@Override
	public boolean isCubeShaped() {
		return false;
	}

	@Override
	public @NotNull AABBdc getBoundsFromState(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return new AABBd(0, 0, 0, 1, COLLISION_BOX_HEIGHT, 1);
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		// update mechanical power state
		boolean wasPowered = getMechanicallyPoweredFlag(world, tilePos);
		boolean isPowered = false;
		int poweredFromFacing = 0;

		for (int facing = 2; facing <= 5; facing++) {
			if (MechPowerUtils.isBlockPoweredByAxleToSide(world, tilePos, facing) ||
				MechPowerUtils.isBlockPoweredByHandCrankToSide(world, tilePos, facing)
			) {
				isPowered = true;
				poweredFromFacing = facing;

				breakPowerSourceThatOpposePoweredFacing(world, tilePos, poweredFromFacing);
			}
		}

		if (wasPowered != isPowered) {
			float volume = 2.0F + (rand.nextFloat() * 0.1F);
			float pitch = 0.5F + (rand.nextFloat() * 0.1F);

			SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.VESSEL_TIP, volume, pitch);

			setMechanicallyPoweredFlag(world, tilePos, isPowered);

			if (!isPowered) {
				// unpowered blocks face upwards
				setTiltFacing(world, tilePos, 0);
			} else {
				setFacingBasedOnPoweredFromFacing(world, tilePos, poweredFromFacing);
			}
		}
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		if (!WorldUtils.isUpdatePendingThisTick(world, tilePos, this.block)) {
			world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
		}
	}

	@Override
	public void animationTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand) {
		if (getMechanicallyPoweredFlag(world, tilePos)) return;

		Block<?> blockUnder = world.getBlockType(new TilePos(tilePos.x(), tilePos.y() - 1, tilePos.z()));

		if (blockUnder == Blocks.FIRE || blockUnder == BTXBlocks.STOKED_FIRE) {
			double smokeX = tilePos.x() + rand.nextFloat();
			double smokeY = tilePos.y() + rand.nextFloat() * 0.5F + 1.0F;
			double smokeZ = tilePos.z() + rand.nextFloat();

			world.spawnParticle("whitesmoke", smokeX, smokeY, smokeZ, 0.0, 0.0, 0.0, 0, false);
		}
	}

	public int getFacing(WorldSource source, TilePosc tilePos) {
		int facing = 1;

		if (getMechanicallyPoweredFlag(source, tilePos)) {
			facing = getTiltFacing(source, tilePos);
		}

		return facing;
	}

	public void setFacing(World world, TilePosc tilePos, int facing) {
	}

	public int getFacing(int data) {
		return 0;
	}

	public int setFacing(int data, int facing) {
		return data;
	}

	@Override
	public boolean canOutputMechanicalPower() {
		return false;
	}

	@Override
	public boolean canInputMechanicalPower() {
		return true;
	}

	@Override
	public boolean isInputtingMechanicalPower(World world, TilePosc tilePos) {
		return MechPowerUtils.isBlockPoweredByAxle(world, tilePos, this);
	}

	@Override
	public boolean canInputAxlePowerToFacing(World world, TilePosc tilePos, Direction facing) {
		return facing != Direction.DOWN && facing != Direction.UP;
	}

	@Override
	public boolean isOutputtingMechanicalPower(World world, TilePosc tilePos) {
		return false;
	}

	@Override
	public void overpower(World world, TilePosc tilePos) {}

	public int getTiltFacing(WorldSource source, TilePosc tilePos) {
		return (source.getBlockData(tilePos) & 3) + 2;
	}

	public void setTiltFacing(World world, TilePosc tilePos, int facing) {
		int flatFacing = Math.max(facing -2, 0);

		int data = world.getBlockData(tilePos) & ~3; // filter out the old facing

		data |= flatFacing & 3;

		world.setBlockDataNotify(tilePos, data);
		world.markBlocksDirty(tilePos, tilePos);
	}

	public boolean getMechanicallyPoweredFlag(WorldSource source, TilePosc tilePos) {
		return (source.getBlockData(tilePos) & 4) > 0;
	}

	private void setMechanicallyPoweredFlag(World world, TilePosc tilePos, boolean flag) {
		int data = world.getBlockData(tilePos) & ~4;

		if (flag) {
			data |= 4;
		}

		world.setBlockDataNotify(tilePos, data);
	}

	private void setFacingBasedOnPoweredFromFacing(World world, TilePosc tilePos, int poweredFromFacing) {
		int newFacing = FacingUtils.rotateFacingAroundY(poweredFromFacing, false);
		setTiltFacing(world, tilePos, newFacing);
	}

	private void breakPowerSourceThatOpposePoweredFacing(World world, TilePosc tilePos, int poweredFromFacing) {
		// break other axles that don't "fit" with the one that's primarily powering it
		int oppositePoweredFromFacing = Direction.fromId(poweredFromFacing).opposite().id;
		TilePos tempPos = new TilePos();

		for (int facing = 2; facing <= 5; facing++) {
			if (facing != poweredFromFacing) {
				boolean shouldBreak = false;

				if (facing == oppositePoweredFromFacing) {
					if (MechPowerUtils.isBlockPoweredByAxleToSide(world, tilePos, facing)) {
						shouldBreak = true;
					}
				} else if (MechPowerUtils.doesBlockHaveFacingAxleToSide(world, tilePos, facing)) {
					shouldBreak = true;
				}

				BlockLogicAxle.addFacingAsOffset(tilePos, facing, tempPos);

				if (shouldBreak) {
					((BlockLogicAxle) BTXBlocks.AXLE.getLogic()).breakAxle(world, tempPos);
				}

				// break powered hand cranks
				if (MechPowerUtils.isBlockPoweredByHandCrankToSide(world, tilePos, facing)) {
					((BlockLogicHandCrank) BTXBlocks.HAND_CRANK.getLogic()).breakCrankWithDrop(world, tempPos);
				}
			}
		}
	}

	public boolean isOpenSideBlocked(World world, TilePosc tilePos) {
		int facing = getFacing(world, tilePos);
		TilePos targetPos = new TilePos(tilePos);
		targetPos.add(Direction.fromId(facing));

		return world.isBlockNormalCube(targetPos);
	}
}
