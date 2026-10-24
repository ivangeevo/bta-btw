package ivangeevo.btx.entity.entities.lift;

import com.mojang.nbt.tags.CompoundTag;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicAnchor;
import ivangeevo.btx.block.entity.TileEntityPulley;
import ivangeevo.btx.util.utils.MiscUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;

import java.util.ArrayList;
import java.util.List;

public class MovingAnchorEntity extends MovingLiftEntity {
	private static final int CHUNK_CHECK_RANGE = 35;
	private static final double LINKED_ANCHOR_RANGE = 5.0D;

	private final TilePos pulleyPos = new TilePos();
	private int pulleyRopeStateCounter = -1;
	private int oldBottomY;
	private boolean resumed = true;

	public MovingAnchorEntity(World world) {
		super(world, (float) BlockLogicAnchor.BASE_HEIGHT, 0.5F);
	}

	public MovingAnchorEntity(World world, double x, double y, double z) {
		this(world);
		setPos(x, y, z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
		this.oldBottomY = MathHelper.floor(this.y - this.heightOffset);
	}

	public MovingAnchorEntity(World world, double x, double y, double z, TilePosc pulleyPos, int direction) {
		this(world, x, y, z);
		this.pulleyPos.set(pulleyPos.x(), pulleyPos.y(), pulleyPos.z());
		this.yd = direction > 0 ? MOVEMENT_SPEED : -MOVEMENT_SPEED;
		setCoarseYMotion(this.yd);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		tag.putInt("associatedPulleyPosI", this.pulleyPos.x());
		tag.putInt("associatedPulleyPosJ", this.pulleyPos.y());
		tag.putInt("associatedPulleyPosK", this.pulleyPos.z());
		tag.putInt("m_iAssociatedPulleyRopeStateCounter", this.pulleyRopeStateCounter);
		tag.putInt("m_iOldBottomJ", this.oldBottomY);
		tag.putDouble("btxMotionY", this.yd);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		this.pulleyPos.set(tag.getInteger("associatedPulleyPosI"), tag.getInteger("associatedPulleyPosJ"), tag.getInteger("associatedPulleyPosK"));

		if (tag.containsKey("m_iAssociatedPulleyRopeStateCounter")) {
			this.pulleyRopeStateCounter = tag.getInteger("m_iAssociatedPulleyRopeStateCounter");
		}

		this.oldBottomY = tag.containsKey("m_iOldBottomJ")
			? tag.getInteger("m_iOldBottomJ")
			: MathHelper.floor(this.y - this.heightOffset);

		if (tag.containsKey("btxMotionY")) {
			this.yd = tag.getDouble("btxMotionY");
		}

		this.resumed = false;
	}

	@Override
	public void tick() {
		if (this.removed) {
			return;
		}

		if (this.world.isClientSide) {
			this.yd = getCoarseYMotion();
		}

		if (!this.world.isClientSide && !this.resumed) {
			if (!canResumeWithLinkedAnchors()) {
				setCoarseYMotion(0.0D);
				return;
			}

			this.resumed = true;
		}

		int i = MathHelper.floor(this.x);
		int k = MathHelper.floor(this.z);

		if (!this.world.areBlocksLoaded(i - CHUNK_CHECK_RANGE, 0, k - CHUNK_CHECK_RANGE, i + CHUNK_CHECK_RANGE, 0, k + CHUNK_CHECK_RANGE)) {
			if (!this.world.isClientSide) {
				setCoarseYMotion(0.0D);
			}

			return;
		}

		TileEntityPulley pulley = null;
		Block<?> blockAbove = this.world.getBlockType(new TilePos(i, this.oldBottomY + 1, k));
		boolean forceValidation = false;

		if (!this.world.isClientSide) {
			if (this.world.getBlockType(this.pulleyPos) == BTXBlocks.PULLEY) {
				Block<?> blockTwoAbove = this.world.getBlockType(new TilePos(i, this.oldBottomY + 2, k));

				if (isRopeOrPulley(blockAbove) || isRopeOrPulley(blockTwoAbove)) {
					pulley = getPulley();

					if (pulley == null || this.pulleyRopeStateCounter == pulley.getUpdateRopeStateCounter()) {
						return;
					}

					if (this.yd > 0.0D ? pulley.isLowering() : pulley.isRaising()) {
						this.yd = -this.yd;
						forceValidation = true;
					}

					this.pulleyRopeStateCounter = pulley.getUpdateRopeStateCounter();
				}

				setCoarseYMotion(this.yd);
			}

			if (this.yd <= 0.01D && this.yd >= -0.01D) {
				convertToBlock(new TilePos(i, this.oldBottomY, k));
				return;
			}

			setCoarseYMotion(this.yd);
		}

		double previousX = this.x;
		double previousY = this.y;
		double previousZ = this.z;

		moveInternal(this.yd);

		if (this.world.isClientSide) {
			moveClientTowardsServer();
		}

		int newBottomY = MathHelper.floor(this.y - this.heightOffset);

		pushCollidingEntities(0.25D);

		if (this.world.isClientSide) {
			return;
		}

		if ((this.oldBottomY != newBottomY || forceValidation) && !handleBlockCrossing(pulley, blockAbove, i, k, newBottomY)) {
			return;
		}

		for (MovingPlatformEntity platform : getPlatformsDrivenFrom(previousX, previousY, previousZ)) {
			platform.driveFromAnchor(this, this.y - previousY);

			if (this.removed) {
				return;
			}
		}
	}

	private boolean handleBlockCrossing(@Nullable TileEntityPulley pulley, Block<?> blockAbove, int i, int k, int newBottomY) {
		TilePos newBottomPos = new TilePos(i, newBottomY, k);
		TilePos aboveNewBottomPos = new TilePos(i, newBottomY + 1, k);

		if (this.yd > 0.0D) {
			if (pulley != null && this.world.getBlockType(newBottomPos) == Blocks.ROPE) {
				pulley.attemptToRetractRope();
			}

			if (this.world.getBlockType(aboveNewBottomPos) != Blocks.ROPE || pulley == null || !pulley.isRaising() || newBottomY + 1 >= this.pulleyPos.y()) {
				convertToBlock(newBottomPos);
				return false;
			}
		} else {
			boolean stop = pulley == null || !pulley.isLowering() || pulley.getContainedRopeCount() < getRopeRequiredToDescend(blockAbove, i, k);

			if (!stop && !this.world.canPlaceInsideBlock(newBottomPos)) {
				if (LiftUtils.canLiftBreakBlock(this.world, newBottomPos)) {
					if (this.world.getBlockType(newBottomPos) == Blocks.ROPE) {
						if (!returnRopeToPulley()) {
							MiscUtils.ejectStackWithRandomOffset(this.world, newBottomPos, new ItemStack(Items.ROPE));
						}

						this.world.setBlockTypeNotify(newBottomPos, Blocks.AIR);
					} else {
						LiftUtils.breakBlockWithDrops(this.world, newBottomPos);
					}
				} else {
					stop = true;
				}
			}

			if (stop) {
				convertToBlock(new TilePos(i, this.oldBottomY, k));
				return false;
			}

			if (!isRopeOrPulley(this.world.getBlockType(aboveNewBottomPos))) {
				pulley.attemptToDispenseRope();
			}
		}

		this.oldBottomY = newBottomY;

		return true;
	}

	private boolean canResumeWithLinkedAnchors() {
		if (!isReadyToResume()) {
			return false;
		}

		AABBd area = new AABBd(this.x - LINKED_ANCHOR_RANGE, this.y - 1.0D, this.z - LINKED_ANCHOR_RANGE,
			this.x + LINKED_ANCHOR_RANGE, this.y + 1.0D, this.z + LINKED_ANCHOR_RANGE);

		for (MovingAnchorEntity other : this.world.getEntitiesWithinAABB(MovingAnchorEntity.class, area)) {
			if (other != this && !other.removed && !other.resumed && !other.isReadyToResume()) {
				return false;
			}
		}

		return true;
	}

	public boolean isReadyToResume() {
		int i = MathHelper.floor(this.x);
		int k = MathHelper.floor(this.z);

		if (!this.world.areBlocksLoaded(i - CHUNK_CHECK_RANGE, 0, k - CHUNK_CHECK_RANGE, i + CHUNK_CHECK_RANGE, 0, k + CHUNK_CHECK_RANGE)) {
			return false;
		}

		TileEntityPulley pulley = getPulley();

		return pulley == null || this.pulleyRopeStateCounter != pulley.getUpdateRopeStateCounter();
	}

	private int getRopeRequiredToDescend(Block<?> blockAbove, int i, int k) {
		if (!isRopeOrPulley(blockAbove)) {
			return 2;
		}

		return isRopeOrPulley(this.world.getBlockType(new TilePos(i, this.oldBottomY, k))) ? 0 : 1;
	}

	private static boolean isRopeOrPulley(Block<?> block) {
		return block == Blocks.ROPE || block == BTXBlocks.PULLEY;
	}

	public void forceStopByPlatform() {
		if (this.removed) {
			return;
		}

		int i = MathHelper.floor(this.x);
		int j = MathHelper.floor(this.y);
		int k = MathHelper.floor(this.z);

		if (this.yd > 0.0D) {
			TilePos above = new TilePos(i, j + 1, k);

			if (this.world.getBlockType(above) == Blocks.ROPE) {
				Blocks.ROPE.dropWithCause(this.world, EnumDropCause.WORLD, above, this.world.getBlockData(above), null, null);
				this.world.setBlockTypeNotify(above, Blocks.AIR);
			}
		}

		convertToBlock(new TilePos(i, j, k));
	}

	private void convertToBlock(TilePos pos) {
		boolean canPlace = true;

		if (!this.world.canPlaceInsideBlock(pos)) {
			if (this.world.getBlockType(pos) == Blocks.ROPE) {
				if (!returnRopeToPulley()) {
					MiscUtils.ejectStackWithRandomOffset(this.world, pos, new ItemStack(Items.ROPE));
				}
			} else if (LiftUtils.canLiftBreakBlock(this.world, pos)) {
				LiftUtils.breakBlockWithDrops(this.world, pos);
			} else {
				canPlace = false;
			}
		}

		if (canPlace) {
			this.world.setBlockTypeDataNotify(pos, BTXBlocks.ANCHOR, 1);
		} else {
			MiscUtils.ejectStackWithRandomOffset(this.world, pos, new ItemStack(BTXBlocks.ANCHOR));
		}

		notifyPulleyOfLossOfAnchorEntity();
		remove();
		convertPlatformsToBlocks();
	}

	private void convertPlatformsToBlocks() {
		boolean movingUp = this.yd > 0.0D;

		for (MovingPlatformEntity platform : getPlatformsDrivenFrom(this.x, this.y, this.z)) {
			platform.convertToBlock(movingUp);
		}
	}

	private List<MovingPlatformEntity> getPlatformsDrivenFrom(double anchorX, double anchorY, double anchorZ) {
		List<MovingPlatformEntity> platforms = new ArrayList<>();
		AABBd area = new AABBd(anchorX - 3.0D, anchorY - 4.0D, anchorZ - 3.0D, anchorX + 3.0D, anchorY + 1.0D, anchorZ + 3.0D);

		for (MovingPlatformEntity platform : this.world.getEntitiesWithinAABB(MovingPlatformEntity.class, area)) {
			if (platform.isDrivenByAnchorAt(anchorX, anchorY, anchorZ)) {
				platforms.add(platform);
			}
		}

		return platforms;
	}

	@Override
	public void destroyWithDrop() {
		remove();

		if (!this.world.isClientSide) {
			MiscUtils.ejectStackWithRandomOffset(this.world, new TilePos(MathHelper.floor(this.x), MathHelper.floor(this.y), MathHelper.floor(this.z)), new ItemStack(BTXBlocks.ANCHOR));
			notifyPulleyOfLossOfAnchorEntity();
			convertPlatformsToBlocks();
		}
	}

	public boolean returnRopeToPulley() {
		TileEntityPulley pulley = getPulley();

		if (pulley != null) {
			pulley.addRopeToInventory();
			return true;
		}

		return false;
	}

	private void notifyPulleyOfLossOfAnchorEntity() {
		TileEntityPulley pulley = getPulley();

		if (pulley != null) {
			pulley.notifyOfLossOfAnchorEntity();
		}
	}

	private @Nullable TileEntityPulley getPulley() {
		if (this.world.getBlockType(this.pulleyPos) == BTXBlocks.PULLEY && this.world.getTileEntity(this.pulleyPos) instanceof TileEntityPulley pulley) {
			return pulley;
		}

		return null;
	}

	public double getMotionY() {
		return this.yd;
	}
}
