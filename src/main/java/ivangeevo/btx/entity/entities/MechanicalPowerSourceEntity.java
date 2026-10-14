package ivangeevo.btx.entity.entities;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicAxle;
import ivangeevo.btx.block.blocks.BlockLogicGearBox;
import ivangeevo.btx.util.utils.MechPowerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.ICollidable;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.gamemode.Gamemodes;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBd;

public abstract class MechanicalPowerSourceEntity extends Entity implements ICollidable {

	private static final int DATA_ROTATION_SPEED_ID = 22;

	private static final int[][] FACING_OFFSETS = {
		{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}
	};

	private static final int[][] AXLE_FACINGS_FOR_ALIGNMENT = {{0, 1}, {2, 3}, {4, 5}};

	// degrees per tick, change per tick. 0.02 goes from calm (-0.12) to storm (-2.0) in about 5 seconds
	private static final float SPEED_EASE_STEP = 0.02F;

	public float rotation;
	public int currentDamage;
	public int timeSinceHit;
	public int rockDirection;

	protected boolean providingPower;
	protected int fullUpdateTickCount;

	protected float displayedSpeed;
	private boolean displayedSpeedInitialized;

	public MechanicalPowerSourceEntity(@NotNull World world) {
		super(world);

		// fire immune to prevent this entity from catching fire as it is so large
		// that the fire effect would just look fucked
		fireImmune = true;

		providingPower = false;
		currentDamage = 0;
		timeSinceHit = 0;
		rockDirection = 1;
		rotation = 0F;
		fullUpdateTickCount = 0;
		blocksBuilding = true;

		setSize(this.getWidth(), this.getHeight());

		heightOffset = bbHeight / 2.0f;
	}

	public MechanicalPowerSourceEntity(World world, double x, double y, double z) {
		this(world);
		setPos(x, y, z);
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_ROTATION_SPEED_ID, 0, Integer.class);
	}

	@Override
	protected boolean makeStepSound() {
		return false;
	}

	@Override
	public boolean isPickable() {
		return !removed;
	}

	@Override
	public float getPickRadius() {
		return 0.0F; // 0.4 half-depth + 0 = 0.4 < 0.5, so the axle face is always hit first
	}

	@Override
	public boolean hasCollision() {
		return !removed;
	}

	@Override
	public void move(double xd, double yd, double zd) {
		// this might be called by external sources (like the pistons), so we have to override it
		// destroy the device if it is moved by an external source
		if(!removed) {
			destroyWithDrop();
		}
	}

	@Override
	public boolean hurt(Entity attacker, int baseDamage, DamageType type) {
		if (removed) {
			return true;
		}
		// Note: the server and client can have differing damage values as the server will destroy the ent when it takes too much, and the rest is just visual

		currentDamage += baseDamage * 5;
		rockDirection = -rockDirection;
		timeSinceHit = 10;

		if (!world.isClientSide) {
			if (attacker instanceof Player player && player.getGamemode() == Gamemodes.CREATIVE) {
				// destroy on first hit in creative
				destroyWithDrop();
			} else {
				markHurt();
				if (currentDamage > getMaxDamage()) {
					destroyWithDrop();
				}
			}
		}

		return true;
	}

	@Override
	public void tick() {
		// intentionally doesn't call super method
		if (removed) {
			return;
		}

		if (!world.isClientSide) {
			fullUpdateTickCount--;

			if (fullUpdateTickCount <= 0) {
				fullUpdateTickCount = getTicksPerFullUpdate();
				onFullUpdateServer();
			}

			updateRotationAndDamageState();
		} else {
			float prevRotation = rotation;

			updateRotationAndDamageState();

			int newOctant = (int)(rotation / 45F);
			int oldOctant = (int)(prevRotation / 45F);

			if (oldOctant != newOctant) {
				onClientRotationOctantChange();
			}
		}
	}

	public abstract float getWidth();

	public abstract float getHeight();

	public abstract float getDepth();

	//public abstract void initBoundingBox();

	public abstract AABBd getDeviceBounds();

	public abstract int getMaxDamage();

	public abstract int getTicksPerFullUpdate();

	public abstract void destroyWithDrop();

	public abstract boolean validateAreaAroundDevice();

	protected abstract boolean validateConnectedAxles();

	public abstract float computeRotation();

	public abstract void transferPowerStateToConnectedAxles();

	private void updateRotationAndDamageState() {
		// update rotation

		//rotation += getRotationSpeed();

		float targetSpeed = getRotationSpeed(); // synced target
		if (!displayedSpeedInitialized) {
			displayedSpeed = targetSpeed; // snap on the first tick so a loaded or newly joined mill doesn't ramp up from 0
			displayedSpeedInitialized = true;
		} else {
			displayedSpeed = approach(displayedSpeed, targetSpeed, SPEED_EASE_STEP);
		}

		rotation += displayedSpeed;

		if (rotation > 360F) {
			rotation -= 360;
		} else if (rotation < -360F) {
			rotation += 360F;
		}

		// update damage state

		if (timeSinceHit > 0) {
			timeSinceHit--;
		}

		if (currentDamage > 0) {
			currentDamage--;
		}
	}

	public float getDisplayedRotationSpeed() {
		return displayedSpeed;
	}

	private static float approach(float current, float target, float step) {
		if (current < target) return Math.min(current + step, target);
		return Math.max(current - step, target);
	}

	protected void onClientRotationOctantChange() {}

	public boolean isClearOfBlockingEntities() {
		return world.checkIfAABBIsClear(getDeviceBounds());
	}

	public float getRotationSpeed() {
		return entityData.getInt(DATA_ROTATION_SPEED_ID) / 100F;
	}

	public void setRotationSpeed(float rotation) {
		entityData.set(DATA_ROTATION_SPEED_ID, (int) (rotation * 100F));
	}

	public int getRotationSpeedScaled() {
		return entityData.getInt(DATA_ROTATION_SPEED_ID);
	}

	public void setRotationSpeedScaled(int rotationSpeedScaled) {
		entityData.set(DATA_ROTATION_SPEED_ID, rotationSpeedScaled);
	}

	protected void onFullUpdateServer() {
		// validate the entity is still occupying a valid location

		if (!validateAreaAroundDevice() || !validateConnectedAxles()) {
			destroyWithDrop();
			return;
		}

		// update the rotation;

		setRotationSpeed(computeRotation());

		float currentSpeed = getRotationSpeed();

		boolean newPoweredState = currentSpeed > 0.01F || currentSpeed < -0.01F;

		if (providingPower != newPoweredState ) {
			providingPower = newPoweredState;
			transferPowerStateToConnectedAxles();
		}
	}

	protected void turnOffClosestInputGearbox(int axisAlignment, TilePosc center) {
		TilePos pos = new TilePos();

		for (int facing : AXLE_FACINGS_FOR_ALIGNMENT[axisAlignment]) {
			pos.set(center.x(), center.y(), center.z());

			Block<?> block;
			int steps = 0;
			do {
				// addFacingAsOffset reads the source before calling dest.set, so in-place is safe
				BlockLogicAxle.addFacingAsOffset(pos, facing, pos);
				block = world.getBlockType(pos);
				steps++;
			} while (steps < 4
				&& MechPowerUtils.isBlockAxle(block)
				&& block.getLogic() instanceof BlockLogicAxle axle
				&& axle.getAxisAlignment(world, pos) == axisAlignment
				&& axle.getPowerLevel(world, pos) > 0);

			if (block == BTXBlocks.GEARBOX
				&& block.getLogic() instanceof BlockLogicGearBox gearBox
				&& gearBox.isGearBoxOn(world, pos)
				&& gearBox.canInputAxlePowerToFacing(world, pos,
				Direction.fromId(BlockLogicAxle.getOppositeFacing(facing)))) {
				gearBox.setGearBoxOn(world, pos, false);
			}
		}
	}
	private static int oppositeFacing(int facing) {
		return facing ^ 1;   // 0<->1, 2<->3, 4<->5
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void lerpTo(double x, double y, double z, float yRot, float xRot, int i) {
		// empty override to prevent network entity teleport packets from fucking up position and bounding box
	}

	@Override
	public float getShadowHeightOffs() {
		return 0.0F;
	}

	@Override
	public void animateHurt() {
		rockDirection = -rockDirection;
		timeSinceHit = 10;
		currentDamage += currentDamage * 5;
	}
}
