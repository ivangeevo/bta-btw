package ivangeevo.btx.networking;

import com.mojang.nbt.tags.CompoundTag;
import ivangeevo.btx.entity.entities.WaterWheelEntity;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.net.entity.EntityTracker;
import net.minecraft.core.net.entity.EntityTrackerEntry;
import net.minecraft.core.net.packet.PacketAddEntity;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetEntryWaterWheel extends NetEntryMechanicalPowerSource<WaterWheelEntity> {

    @Override
    public @NotNull Class<? extends WaterWheelEntity> getAppliedClass() {
		return WaterWheelEntity.class;
	}

    @Override
    public PacketAddEntity getSpawnPacket(EntityTrackerEntry entry, WaterWheelEntity wheel) {
		CompoundTag tag = new CompoundTag();
        tag.putBoolean("AlignedToX", wheel.alignedToX);
        tag.putInt("RotationSpeed", wheel.getRotationSpeedScaled());

		return new PacketAddEntity(wheel, 81, -1, null, null, null, tag);
    }

    @Override
    public Entity getEntity(World world, double x, double y, double z, int data, boolean b,
							double mx, double my, double mz, Entity owner, @Nullable CompoundTag tag
	) {
        boolean alignedToX = tag == null || tag.getBoolean("AlignedToX");
		WaterWheelEntity wheel = new WaterWheelEntity(world, x, y, z, alignedToX);
        if (tag != null) {
            wheel.setRotationSpeedScaled(tag.getInteger("RotationSpeed"));
        }

		return wheel;
    }

	@Override
	public void onEntityTracked(EntityTracker var1, EntityTrackerEntry var2, WaterWheelEntity var3) {}

}
