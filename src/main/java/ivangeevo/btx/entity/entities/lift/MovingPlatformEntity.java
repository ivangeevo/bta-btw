package ivangeevo.btx.entity.entities.lift;

import com.mojang.nbt.tags.CompoundTag;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.util.utils.MiscUtils;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;

import java.util.List;

public class MovingPlatformEntity extends MovingLiftEntity {
	private static final int MISSING_ANCHOR_GRACE_TICKS = 20;

	private int ticksWithoutAnchor;
	private long driveTick = Long.MIN_VALUE;
	private double driveStartY;
	private double anchorLastKnownX;
	private double anchorLastKnownY;
	private double anchorLastKnownZ;

	public MovingPlatformEntity(World world) {
		super(world, 0.98F, 0.49F);
	}

	public MovingPlatformEntity(World world, double x, double y, double z) {
		this(world, x, y, z, null);
	}

	public MovingPlatformEntity(World world, double x, double y, double z, @Nullable MovingAnchorEntity anchor) {
		this(world);

		if (anchor != null) {
			this.anchorLastKnownX = anchor.x;
			this.anchorLastKnownY = anchor.y;
			this.anchorLastKnownZ = anchor.z;
			this.yd = anchor.getMotionY();
			setCoarseYMotion(this.yd);
		}

		setPos(x, y, z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		tag.putDouble("m_AssociatedAnchorLastKnownXPos", this.anchorLastKnownX);
		tag.putDouble("m_AssociatedAnchorLastKnownYPos", this.anchorLastKnownY);
		tag.putDouble("m_AssociatedAnchorLastKnownZPos", this.anchorLastKnownZ);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		this.anchorLastKnownX = tag.getDouble("m_AssociatedAnchorLastKnownXPos");
		this.anchorLastKnownY = tag.getDouble("m_AssociatedAnchorLastKnownYPos");
		this.anchorLastKnownZ = tag.getDouble("m_AssociatedAnchorLastKnownZPos");
	}

	@Override
	public void tick() {
		if (this.removed) {
			return;
		}

		if (this.world.isClientSide) {
			this.yd = getCoarseYMotion();
			moveInternal(this.yd);
			moveClientTowardsServer();
			pushCollidingEntities(0.5D);
			return;
		}

		long now = this.world.getTotalWorldTime();

		if (this.driveTick == now) {
			this.yo = this.driveStartY;
			return;
		}

		if (this.driveTick == now - 1) {
			return;
		}

		List<MovingAnchorEntity> list = this.world.getEntitiesWithinAABB(MovingAnchorEntity.class, new AABBd(
			this.anchorLastKnownX - 0.25D, this.anchorLastKnownY - 0.25D, this.anchorLastKnownZ - 0.25D,
			this.anchorLastKnownX + 0.25D, this.anchorLastKnownY + 0.25D, this.anchorLastKnownZ + 0.25D));

		for (MovingAnchorEntity anchor : list) {
			if (!anchor.removed) {
				this.ticksWithoutAnchor = 0;
				this.yd = 0.0D;
				setCoarseYMotion(this.yd);
				return;
			}
		}

		if (!isWaitingForAnchor()) {
			convertToBlock(this.yd > 0.0D);
		}
	}

	public boolean isDrivenByAnchorAt(double anchorX, double anchorY, double anchorZ) {
		return !this.removed
			&& Math.abs(this.anchorLastKnownX - anchorX) <= 0.25D
			&& Math.abs(this.anchorLastKnownY - anchorY) <= 0.25D
			&& Math.abs(this.anchorLastKnownZ - anchorZ) <= 0.25D;
	}

	public void driveFromAnchor(MovingAnchorEntity anchor, double deltaY) {
		if (this.removed) {
			return;
		}

		this.driveTick = this.world.getTotalWorldTime();
		this.driveStartY = this.y;
		this.ticksWithoutAnchor = 0;
		this.anchorLastKnownX = anchor.x;
		this.anchorLastKnownY = anchor.y;
		this.anchorLastKnownZ = anchor.z;
		this.yd = deltaY;
		setCoarseYMotion(this.yd);

		int i = MathHelper.floor(this.x);
		int oldCenterY = MathHelper.floor(this.y);
		int k = MathHelper.floor(this.z);

		moveInternal(this.yd);
		driveLiftedBlock(deltaY);
		pushCollidingEntities(0.5D);

		if (this.yd < 0.01D && this.yd > -0.01D) {
			return;
		}

		boolean movingUp = this.yd > 0.0D;
		TilePos edge = new TilePos(i, MathHelper.floor(movingUp ? this.y + 0.49D : this.y - 0.49D), k);

		if (!this.world.canPlaceInsideBlock(edge)) {
			if (LiftUtils.canLiftBreakBlock(this.world, edge)) {
				LiftUtils.breakBlockWithDrops(this.world, edge);
			} else {
				convertToBlock(new TilePos(i, oldCenterY, k), movingUp);
				anchor.forceStopByPlatform();
			}
		}
	}

	private void driveLiftedBlock(double deltaY) {
		double previousY = this.y - deltaY;
		List<BlockLiftedByPlatformEntity> lifted = this.world.getEntitiesWithinAABB(BlockLiftedByPlatformEntity.class, new AABBd(
			this.x - 0.25D, previousY + 0.75D, this.z - 0.25D,
			this.x + 0.25D, previousY + 1.25D, this.z + 0.25D));

		for (BlockLiftedByPlatformEntity block : lifted) {
			block.followPlatform(this);
		}
	}

	public void convertToBlock(boolean movingUp) {
		convertToBlock(new TilePos(MathHelper.floor(this.x), MathHelper.floor(this.y), MathHelper.floor(this.z)), movingUp);
	}

	private boolean isWaitingForAnchor() {
		TilePos anchorPos = new TilePos(MathHelper.floor(this.anchorLastKnownX), MathHelper.floor(this.anchorLastKnownY), MathHelper.floor(this.anchorLastKnownZ));

		if (!this.world.areBlocksLoaded(anchorPos, 2)) {
			return true;
		}

		return this.ticksWithoutAnchor++ < MISSING_ANCHOR_GRACE_TICKS;
	}

	private void convertToBlock(TilePos pos, boolean movingUp) {
		if (this.removed) {
			return;
		}

		if (this.world.canPlaceInsideBlock(pos)) {
			this.world.setBlockTypeNotify(pos, BTXBlocks.PLATFORM);
		} else if (LiftUtils.canLiftBreakBlock(this.world, pos)) {
			LiftUtils.breakBlockWithDrops(this.world, pos);
			this.world.setBlockTypeNotify(pos, BTXBlocks.PLATFORM);
		} else {
			MiscUtils.ejectStackWithRandomOffset(this.world, pos, new ItemStack(BTXBlocks.PLATFORM));
		}

		convertLiftedBlock(pos);
		LiftUtils.positionAllNonPlayerMoveableEntitiesOutsideOfLocation(this.world, pos);
		LiftUtils.positionAllPlayerEntitiesOutsideOfLocation(this.world, movingUp ? pos.down(new TilePos()) : pos.up(new TilePos()));
		LiftUtils.positionAllPlayerEntitiesOutsideOfLocation(this.world, pos);

		remove();
	}

	private void convertLiftedBlock(TilePos platformPos) {
		List<BlockLiftedByPlatformEntity> lifted = this.world.getEntitiesWithinAABB(BlockLiftedByPlatformEntity.class, new AABBd(
			this.x - 0.25D, this.y + 0.75D, this.z - 0.25D,
			this.x + 0.25D, this.y + 1.25D, this.z + 0.25D));

		for (BlockLiftedByPlatformEntity block : lifted) {
			block.convertToBlock(platformPos.up(new TilePos()));
		}
	}

	@Override
	public void destroyWithDrop() {
		if (!this.world.isClientSide) {
			MiscUtils.ejectStackWithRandomOffset(this.world, new TilePos(MathHelper.floor(this.x), MathHelper.floor(this.y), MathHelper.floor(this.z)), new ItemStack(BTXBlocks.PLATFORM));
		}

		remove();
	}
}