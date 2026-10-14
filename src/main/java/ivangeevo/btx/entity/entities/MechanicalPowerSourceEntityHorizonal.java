package ivangeevo.btx.entity.entities;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicAxle;
import ivangeevo.btx.util.utils.MechPowerUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

public abstract class MechanicalPowerSourceEntityHorizonal extends MechanicalPowerSourceEntity {

	public boolean alignedToX;

	public MechanicalPowerSourceEntityHorizonal(@NotNull World world) {
		super(world);
		alignedToX = true;
		setBounds();
	}

	public MechanicalPowerSourceEntityHorizonal(@NotNull World world, double x, double y, double z, boolean xAligned) {
		super(world, x, y, z);
		alignedToX = xAligned;
		setBounds();
	}

	@Override
	public void remove() {
		if (providingPower && !world.isClientSide) {
			TilePos center = getCenterPos();

			if (isPoweredAxle(center)) {
				// temporarily replace the powered axle with a plain one to prevent power-transfer
				// problems if we're powering in both directions

				int axisAlignment = getAxleAxisAlignment(center);

				turnOffClosestInputGearbox(axisAlignment, center);
				replaceCenterAxle(center, false, axisAlignment);
			}
		}

		super.remove();
	}

	/*@Override
	public void setBounds() {
		double halfWidth = getWidth() * 0.5;
		double halfDepth = getDepth() * 0.5;

		double halfX = alignedToX ? halfDepth : halfWidth;
		double halfZ = alignedToX ? halfWidth : halfDepth;

		double bottom = y - heightOffset + ySlideOffset;
		bb.setMin(x - halfX, bottom, z - halfZ);
		bb.setMax(x + halfX, bottom + getHeight(), z + halfZ);
	}*/

	@Override
	public void setBounds() {
		double hx = (alignedToX ? getDepth() : getWidth()) / 2.0;
		double hz = (alignedToX ? getWidth() : getDepth()) / 2.0;
		double minY = y - heightOffset + ySlideOffset;
		bb.setMin(x - hx, minY, z - hz);
		bb.setMax(x + hx, minY + getHeight(), z + hz);
	}

	@Override
	public @Nullable AABBdc getCollisionAABB() {
		return this.bb;
	}

	@Override
	protected boolean validateConnectedAxles() {
		// verify we still have an axle at our center

		TilePos center = getCenterPos();

		if (!isAnyAxle(center)) {
			return false;
		}

		// make sure the center block is still properly oriented
		int axisAlignment = getAxleAxisAlignment(center);

		if (axisAlignment == 0 || (axisAlignment == 1 && alignedToX) || (axisAlignment == 2 & !alignedToX)) {
			return false;
		}

		boolean centerIsPowered = isPoweredAxle(center);

		if (!providingPower) {
			if (centerIsPowered) {
				replaceCenterAxle(center, false, axisAlignment);
			} else if (getAxlePowerLevel(center) > 0) {
				// we have an unpowered device on a powered axle
				return false;
			}
		} else if (!centerIsPowered) {
			replaceCenterAxle(center, true, axisAlignment);
		}

		return true;
	}

	@Override
	public void transferPowerStateToConnectedAxles() {
		TilePos center = getCenterPos();

		if (!isAnyAxle(center)) {
			return;
		}

		int axisAlignment = getAxleAxisAlignment(center);
		boolean isCenterPowered = isPoweredAxle(center);

		if (providingPower && !isCenterPowered) {
			replaceCenterAxle(center, true, axisAlignment);
		} else if (!providingPower && isCenterPowered) {
			replaceCenterAxle(center, false, axisAlignment);
		}
	}

	@Override
	public AABBd getDeviceBounds() {
		// centered on the entity position (the axle block center)
		double halfWidth = getWidth() * 0.5F;
		double halfHeight = getHeight() * 0.5F;
		double halfDepth = getDepth() * 0.5F;

		double halfX = alignedToX ? halfDepth : halfWidth;
		double halfZ = alignedToX ? halfWidth : halfDepth;

		return new AABBd(
			x - halfX, y - halfHeight, z - halfZ,
			x + halfX, y + halfHeight, z + halfZ
		);
	}

	protected TilePos getCenterPos() {
		return new TilePos(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
	}

	private boolean isAnyAxle(TilePos tilePos) {
		return MechPowerUtils.isBlockAxle(world.getBlockType(tilePos));
	}

	private boolean isPoweredAxle(TilePos tilePos) {
		return world.getBlockType(tilePos) == BTXBlocks.AXLE_POWER_SOURCE;
	}

	private BlockLogicAxle getAxleLogic(TilePos tilePos) {
		return world.getBlockType(tilePos).getLogic() instanceof BlockLogicAxle axle ? axle : null;
	}

	private int getAxleAxisAlignment(TilePos tilePos) {
		BlockLogicAxle axle = getAxleLogic(tilePos);
		return axle != null ? axle.getAxisAlignment(world, tilePos) : 0;
	}

	private int getAxlePowerLevel(TilePos tilePos) {
		BlockLogicAxle axle = getAxleLogic(tilePos);
		return axle != null ? axle.getPowerLevel(world, tilePos) : 0;
	}

	// Power bits stored on the power-source axle
	private static final int POWER_SOURCE_LEVEL = 3;

	private void replaceCenterAxle(TilePos pos, boolean powered, int axisAlignment) {
		Block<?> targetBlock = powered ? BTXBlocks.AXLE_POWER_SOURCE : BTXBlocks.AXLE;
		int data = (axisAlignment << 2) | (powered ? POWER_SOURCE_LEVEL : 0);
		world.setBlockTypeData(pos, targetBlock, data);
		world.notifyBlockChange(pos, targetBlock);
	}
}
