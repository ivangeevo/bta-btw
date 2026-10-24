package ivangeevo.btx.networking;

import com.mojang.nbt.tags.CompoundTag;
import ivangeevo.btx.entity.entities.lift.BlockLiftedByPlatformEntity;
import ivangeevo.btx.entity.entities.lift.MovingAnchorEntity;
import ivangeevo.btx.entity.entities.lift.MovingPlatformEntity;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.net.entity.EntityTracker;
import net.minecraft.core.net.entity.EntityTrackerEntry;
import net.minecraft.core.net.entity.ITrackedEntry;
import net.minecraft.core.net.entity.IVehicleEntry;
import net.minecraft.core.net.packet.PacketAddEntity;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class NetEntryLift<T extends Entity> implements IVehicleEntry<T>, ITrackedEntry<T> {
	@Override
	public int getTrackingDistance() {
		return 160;
	}

	@Override
	public int getMovementPacketDelay() {
		return 3;
	}

	@Override
	public boolean sendMotionUpdates() {
		return false;
	}

	@Override
	public void onEntityTracked(EntityTracker tracker, EntityTrackerEntry entry, T entity) {}

	public static class MovingAnchor extends NetEntryLift<MovingAnchorEntity> {
		@Override
		public @NotNull Class<? extends MovingAnchorEntity> getAppliedClass() {
			return MovingAnchorEntity.class;
		}

		@Override
		public PacketAddEntity getSpawnPacket(EntityTrackerEntry entry, MovingAnchorEntity anchor) {
			return new PacketAddEntity(anchor, 0, -1, null, null, null, new CompoundTag());
		}

		@Override
		public Entity getEntity(World world, double x, double y, double z, int data, boolean b,
								double mx, double my, double mz, Entity owner, @Nullable CompoundTag tag) {
			return new MovingAnchorEntity(world, x, y, z);
		}
	}

	public static class MovingPlatform extends NetEntryLift<MovingPlatformEntity> {
		@Override
		public @NotNull Class<? extends MovingPlatformEntity> getAppliedClass() {
			return MovingPlatformEntity.class;
		}

		@Override
		public PacketAddEntity getSpawnPacket(EntityTrackerEntry entry, MovingPlatformEntity platform) {
			return new PacketAddEntity(platform, 0, -1, null, null, null, new CompoundTag());
		}

		@Override
		public Entity getEntity(World world, double x, double y, double z, int data, boolean b,
								double mx, double my, double mz, Entity owner, @Nullable CompoundTag tag) {
			return new MovingPlatformEntity(world, x, y, z);
		}
	}

	public static class BlockLiftedByPlatform extends NetEntryLift<BlockLiftedByPlatformEntity> {
		@Override
		public @NotNull Class<? extends BlockLiftedByPlatformEntity> getAppliedClass() {
			return BlockLiftedByPlatformEntity.class;
		}

		@Override
		public PacketAddEntity getSpawnPacket(EntityTrackerEntry entry, BlockLiftedByPlatformEntity lifted) {
			CompoundTag tag = new CompoundTag();
			tag.putInt("BlockId", lifted.getBlockId());
			tag.putInt("BlockData", lifted.getBlockData());

			return new PacketAddEntity(lifted, 0, -1, null, null, null, tag);
		}

		@Override
		public Entity getEntity(World world, double x, double y, double z, int data, boolean b,
								double mx, double my, double mz, Entity owner, @Nullable CompoundTag tag) {
			int blockId = tag != null ? tag.getInteger("BlockId") : 0;
			int blockData = tag != null ? tag.getInteger("BlockData") : 0;

			return new BlockLiftedByPlatformEntity(world, x, y, z, blockId, blockData);
		}
	}
}