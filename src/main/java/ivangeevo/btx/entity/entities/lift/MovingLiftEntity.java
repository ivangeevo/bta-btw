package ivangeevo.btx.entity.entities.lift;

import ivangeevo.btx.entity.entities.MechanicalPowerSourceEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.ICollidable;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

import java.util.List;

public abstract class MovingLiftEntity extends Entity implements ICollidable {
	public static final double MOVEMENT_SPEED = 1.0D / 20.0D;
	private static final int DATA_Y_MOTION_ID = 22;
	private static final double MAX_CLIENT_DRIFT = 0.5D;
	private static final double CLIENT_CORRECTION_RATE = 0.1D;

	private boolean hasServerTargetY;
	private double serverTargetY;

	protected MovingLiftEntity(World world, float height, float heightOffset) {
		super(world);
		fireImmune = true;
		blocksBuilding = true;
		setSize(0.98F, height);
		this.heightOffset = heightOffset;
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_Y_MOTION_ID, 0, Integer.class);
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
	public boolean hasCollision() {
		return !removed;
	}

	@Override
	public AABBdc getCollisionAABB() {
		return this.bb;
	}

	@Override
	public boolean hurt(Entity attacker, int baseDamage, DamageType type) {
		return false;
	}

	@Override
	public void move(double xd, double yd, double zd) {
		if (!removed) {
			destroyWithDrop();
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
		if (Math.abs(x - this.x) > MAX_CLIENT_DRIFT || Math.abs(y - this.y) > MAX_CLIENT_DRIFT || Math.abs(z - this.z) > MAX_CLIENT_DRIFT) {
			setPos(x, y, z);
		}

		this.serverTargetY = y;
		this.hasServerTargetY = true;
	}

	protected void moveClientTowardsServer() {
		if (!this.hasServerTargetY) {
			return;
		}

		this.serverTargetY += this.yd;
		moveInternal((this.serverTargetY - this.y) * CLIENT_CORRECTION_RATE);
	}

	public abstract void destroyWithDrop();

	protected double getCoarseYMotion() {
		return this.entityData.getInt(DATA_Y_MOTION_ID) / 100.0D;
	}

	protected void setCoarseYMotion(double yMotion) {
		this.entityData.set(DATA_Y_MOTION_ID, (int) Math.round(yMotion * 100.0D));
	}

	protected void moveInternal(double deltaY) {
		setPos(this.x, this.y + deltaY, this.z);
	}

	protected void pushCollidingEntities(double topThreshold) {
		if (this.world.isClientSide) {
			markClientRiders(topThreshold);
			return;
		}

		AABBd area = new AABBd(this.bb.minX, this.bb.minY - 0.15D, this.bb.minZ, this.bb.maxX, this.bb.maxY + 0.15D, this.bb.maxZ);
		List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, area);

		for (Entity entity : list) {
			if (entity.removed) {
				continue;
			}

			if (entity instanceof MechanicalPowerSourceEntity device) {
				device.destroyWithDrop();
			} else if (entity instanceof Player || entity.isPushable() || entity instanceof EntityItem) {
				pushEntity(entity, topThreshold);
			}
		}
	}

	private void markClientRiders(double topThreshold) {
		AABBd area = new AABBd(this.bb.minX, this.bb.maxY - topThreshold, this.bb.minZ, this.bb.maxX, this.bb.maxY + 0.3D, this.bb.maxZ);

		for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, area)) {
			if (!entity.removed && !(entity instanceof Player) && (entity.isPushable() || entity instanceof EntityItem)) {
				LiftUtils.markClientRider(entity, this);
			}
		}
	}

	private void pushEntity(Entity entity, double topThreshold) {
		double testZoneMaxY = this.bb.maxY + 0.075D;
		double entityMinY = entity.bb.minY;

		if (entityMinY >= testZoneMaxY) {
			return;
		}

		if (entityMinY > this.bb.maxY - topThreshold) {
			if (!(entity instanceof Player)) {
				double offset = this.bb.maxY + 0.01D - entityMinY;
				entity.setPos(entity.x, entity.y + offset, entity.z);
				LiftUtils.settleOnLift(entity);
				LiftUtils.recordPush(entity, this.world.getTotalWorldTime(), offset);
			}
		} else if (entity instanceof Mob && this.yd < 0.0D) {
			double entityMaxY = entity.bb.maxY;

			if (this.bb.minY < entityMaxY - 0.25D && testZoneMaxY > entityMaxY) {
				entity.hurt(null, 1, DamageType.GENERIC);
			}
		}
	}
}