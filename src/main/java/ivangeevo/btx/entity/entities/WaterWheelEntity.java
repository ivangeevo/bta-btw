package ivangeevo.btx.entity.entities;

import com.mojang.nbt.tags.CompoundTag;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.util.WaterFlowHelper;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

public class WaterWheelEntity extends MechanicalPowerSourceEntityHorizonal{

	public static final float HEIGHT = 4.8F;
	public static final float WIDTH = 4.8F;
	public static final float DEPTH = 0.8F;

	public static final int MAX_DAMAGE = 160;

	public static final float ROTATION_PER_TICK = 0.25F;

	public static final int TICKS_PER_FULL_UPDATE = 20;

	private static final int FLOW_DEPTH = 2;
	private static final int SIDE_OFFSET = 2;
	private static final int WHEEL_RADIUS = 2;

	public WaterWheelEntity(@NotNull World world) {
		super(world);
	}

	public WaterWheelEntity(@NotNull World world, double x, double y, double z, boolean xAligned) {
		super(world, x, y, z, xAligned);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag tag) {
		tag.putBoolean("bWaterWheelIAligned", alignedToX);
		tag.putFloat("fRotation", rotation);
		tag.putBoolean("bProvidingPower", providingPower);
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag tag) {
		alignedToX = tag.getBoolean("bWaterWheelIAligned");
		rotation = tag.getFloat("fRotation");
		providingPower = tag.getBoolean("bProvidingPower");
	}

	@Override
	public float getWidth() {
		return WIDTH;
	}

	@Override
	public float getHeight() {
		return HEIGHT;
	}

	@Override
	public float getDepth() {
		return DEPTH;
	}

	@Override
	public int getMaxDamage() {
		return MAX_DAMAGE;
	}

	@Override
	public int getTicksPerFullUpdate() {
		return TICKS_PER_FULL_UPDATE;
	}

	@Override
	public void destroyWithDrop() {
		if (!world.isClientSide && !removed) {
			this.dropItem(BTXItems.WATERWHEEL.id, 1, 0.0F);
			this.remove();
		}
	}

	@Override
	public boolean validateAreaAroundDevice() {
		int centerX = MathHelper.floor(this.x);
		int centerY = MathHelper.floor(this.y);
		int centerZ = MathHelper.floor(this.z);

		return waterWheelValidateAreaAroundBlock(world, centerX, centerY, centerZ, alignedToX);
	}

	@Override
	public float computeRotation() {
		int centerX = MathHelper.floor(this.x);
		int centerY = MathHelper.floor(this.y);
		int centerZ = MathHelper.floor(this.z);

		float rotationAmount = 0.0F;

		int flowY = centerY - FLOW_DEPTH;

		// check for rotation below

		TilePos flowPosBelow = new TilePos(centerX, flowY, centerZ);
		Block<?> flowBlock = world.getBlockType(flowPosBelow);

		if (flowBlock == Blocks.FLUID_WATER_FLOWING || flowBlock == Blocks.FLUID_WATER_STILL) {
			Vector3d flowVector = WaterFlowHelper.getFlowVector(world, flowPosBelow);

			if (alignedToX) {
				if (flowVector.z > 0.33F) {
					rotationAmount = -ROTATION_PER_TICK;
				} else if (flowVector.z < -0.33F) {
					rotationAmount = ROTATION_PER_TICK;
				}
			} else {
				if (flowVector.x > 0.33F) {
					rotationAmount = ROTATION_PER_TICK;
				} else if (flowVector.x < -0.33F) {
					rotationAmount = -ROTATION_PER_TICK;
				}
			}
		}

		// check for rotation on the sides

		int xOffset;
		int zOffset;

		if (alignedToX) {
			xOffset = 0;
			zOffset = SIDE_OFFSET;
		} else {
			xOffset = SIDE_OFFSET;
			zOffset = 0;
		}

		TilePos flowPosSideA = new TilePos(centerX + xOffset, centerY, centerZ - zOffset);
		flowBlock = world.getBlockType(flowPosSideA);

		if (flowBlock == Blocks.FLUID_WATER_FLOWING || flowBlock == Blocks.FLUID_WATER_STILL) {
			rotationAmount -= ROTATION_PER_TICK;
		}

		TilePos flowPosSideB = new TilePos(centerX - xOffset, centerY, centerZ + zOffset);
		flowBlock = world.getBlockType(flowPosSideB);

		if (flowBlock == Blocks.FLUID_WATER_FLOWING || flowBlock == Blocks.FLUID_WATER_STILL) {
			rotationAmount += ROTATION_PER_TICK;
		}

		rotationAmount = Math.max(-ROTATION_PER_TICK, Math.min(ROTATION_PER_TICK, rotationAmount));

		return rotationAmount;
	}

	public static boolean waterWheelValidateAreaAroundBlock(World world, int x, int y, int z, boolean alignedToX) {
		int xOffset;
		int zOffset;

		if (alignedToX) {
			xOffset = 0;
			zOffset = 1;
		} else {
			xOffset = 1;
			zOffset = 0;
		}

		for (int heightOffset = -WHEEL_RADIUS; heightOffset <= WHEEL_RADIUS; heightOffset++) {
			for (int widthOffset = -WHEEL_RADIUS; widthOffset <= WHEEL_RADIUS; widthOffset++) {
				if (heightOffset != 0 || widthOffset != 0) {
					int tempX = x + (xOffset * widthOffset);
					int tempY = y + heightOffset;
					int tempZ = z + (zOffset * widthOffset);

					if (!isValidBlockForWaterWheelToOccupy(world, tempX, tempY, tempZ)) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public static boolean isValidBlockForWaterWheelToOccupy(World world, int x, int y, int z) {
		TilePos tilePos = new TilePos(x, y, z);

		if (!world.isAirBlock(tilePos)) {
			Block<?> block = world.getBlockType(tilePos);
			return block == Blocks.FLUID_WATER_FLOWING || block == Blocks.FLUID_WATER_STILL;
		}

		return true;
	}

}
