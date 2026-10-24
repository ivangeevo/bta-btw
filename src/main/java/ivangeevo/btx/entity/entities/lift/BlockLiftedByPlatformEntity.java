package ivangeevo.btx.entity.entities.lift;

import com.mojang.nbt.tags.CompoundTag;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.util.utils.MiscUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicRail;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;

import java.util.List;

public class BlockLiftedByPlatformEntity extends Entity {
	private static final int MISSING_PLATFORM_GRACE_TICKS = 20;

	private int ticksWithoutPlatform;
	private long driveTick = Long.MIN_VALUE;
	private double driveStartY;
	private int blockId;
	private int blockData;

	public BlockLiftedByPlatformEntity(World world) {
		super(world);
		fireImmune = true;
		setSize(0.98F, 0.98F);
		this.heightOffset = 0.49F;
	}

	public BlockLiftedByPlatformEntity(World world, TilePosc pos) {
		this(world);

		Block<?> block = world.getBlockType(pos);
		int data = world.getBlockData(pos);

		if (block == Blocks.RAIL_POWERED || block == Blocks.RAIL_DETECTOR) {
			data &= BlockLogicRail.MASK_DIRECTION;
		} else if (block == Blocks.WIRE_REDSTONE) {
			data = 0;
		}

		this.blockId = block.id();
		this.blockData = data;

		setPos(pos.x() + 0.5D, pos.y() + 0.5D, pos.z() + 0.5D);
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
	}

	public BlockLiftedByPlatformEntity(World world, double x, double y, double z, int blockId, int blockData) {
		this(world);
		this.blockId = blockId;
		this.blockData = blockData;
		setPos(x, y, z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
	}

	@Override
	protected void defineSynchedData() {}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		tag.putInt("m_iBlockID", this.blockId);
		tag.putInt("m_iBlockMetaData", this.blockData);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		this.blockId = tag.getInteger("m_iBlockID");
		this.blockData = tag.getInteger("m_iBlockMetaData");
	}

	@Override
	protected boolean makeStepSound() {
		return false;
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean hasCollision() {
		return false;
	}

	@Override
	public boolean hurt(Entity attacker, int baseDamage, DamageType type) {
		return false;
	}

	@Override
	public void move(double xd, double yd, double zd) {
		if (!this.removed) {
			destroyWithDrop();
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
		if (Math.abs(x - this.x) > 0.5D || Math.abs(y - this.y) > 0.5D || Math.abs(z - this.z) > 0.5D) {
			setPos(x, y, z);
		}
	}

	@Override
	public void tick() {
		if (this.removed) {
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

		List<MovingPlatformEntity> platforms = this.world.getEntitiesWithinAABB(MovingPlatformEntity.class, new AABBd(
			this.x - 0.25D, this.y - 1.25D, this.z - 0.25D,
			this.x + 0.25D, this.y - 0.75D, this.z + 0.25D));

		for (MovingPlatformEntity platform : platforms) {
			if (!platform.removed) {
				setPos(platform.x, platform.y + 1.0D, platform.z);
				this.ticksWithoutPlatform = 0;
				return;
			}
		}

		TilePos below = new TilePos(MathHelper.floor(this.x), MathHelper.floor(this.y) - 1, MathHelper.floor(this.z));

		if (!this.world.areBlocksLoaded(below, 2) || this.ticksWithoutPlatform++ < MISSING_PLATFORM_GRACE_TICKS) {
			return;
		}

		if (!this.world.isClientSide) {
			convertToBlock(new TilePos(MathHelper.floor(this.x), MathHelper.floor(this.y), MathHelper.floor(this.z)));
		}
	}

	public void followPlatform(MovingPlatformEntity platform) {
		if (this.removed) {
			return;
		}

		this.driveTick = this.world.getTotalWorldTime();
		this.driveStartY = this.y;
		this.ticksWithoutPlatform = 0;
		setPos(platform.x, platform.y + 1.0D, platform.z);
	}

	public void convertToBlock(TilePos pos) {
		if (this.removed) {
			return;
		}

		Block<?> block = getBlock();

		if (block != null && this.world.getBlockType(pos.down(new TilePos())) == BTXBlocks.PLATFORM && this.world.canPlaceInsideBlock(pos)) {
			this.world.setBlockTypeDataNotify(pos, block, this.blockData);
			remove();
		} else {
			destroyWithDrop();
		}
	}

	public void destroyWithDrop() {
		Block<?> block = getBlock();

		if (!this.world.isClientSide && block != null) {
			ItemStack[] drops = block.getLogic().getBreakResult(this.world, EnumDropCause.WORLD, this.blockData, null);
			TilePos pos = new TilePos(MathHelper.floor(this.x), MathHelper.floor(this.y), MathHelper.floor(this.z));

			if (drops != null) {
				for (ItemStack drop : drops) {
					if (drop != null) {
						MiscUtils.ejectStackWithRandomOffset(this.world, pos, drop.copy());
					}
				}
			}
		}

		remove();
	}

	public @Nullable Block<?> getBlock() {
		return Blocks.getBlock(this.blockId);
	}

	public int getBlockId() {
		return this.blockId;
	}

	public int getBlockData() {
		return this.blockData;
	}

	public static boolean canBlockBeConvertedToEntity(World world, TilePosc pos) {
		Block<?> block = world.getBlockType(pos);

		if (block.getLogic() instanceof BlockLogicRail rail) {
			return !rail.getRailDirection(world.getBlockData(pos)).isSloped();
		}

		return block == Blocks.WIRE_REDSTONE;
	}
}