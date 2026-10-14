package ivangeevo.btx.networking;

import com.mojang.nbt.tags.CompoundTag;
import ivangeevo.btx.BTXGlobals;
import ivangeevo.btx.entity.entities.WindMillEntity;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.net.entity.EntityTracker;
import net.minecraft.core.net.entity.EntityTrackerEntry;
import net.minecraft.core.net.packet.PacketAddEntity;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetEntryWindMill extends NetEntryMechanicalPowerSource<WindMillEntity> {

    @Override
    public @NotNull Class<? extends WindMillEntity> getAppliedClass() {
		return WindMillEntity.class;
	}


    @Override
    public PacketAddEntity getSpawnPacket(EntityTrackerEntry entry, WindMillEntity mill) {

		CompoundTag tag = new CompoundTag();
        tag.putBoolean("AlignedToX", mill.alignedToX);
        tag.putInt("RotationSpeed", mill.getRotationSpeedScaled());
        for (int i = 0; i < 4; i++) {
			tag.putInt("Blade" + i, mill.getBladeColor(i));
		}

		return new PacketAddEntity(mill, 80, -1, null, null, null, tag);
    }


    @Override
    public Entity getEntity(World world, double x, double y, double z, int data, boolean b,
							double mx, double my, double mz, Entity owner, @Nullable CompoundTag tag
	) {
        boolean alignedToX = tag == null || tag.getBoolean("AlignedToX");
        WindMillEntity mill = new WindMillEntity(world, x, y, z, alignedToX);
        if (tag != null) {
            mill.setRotationSpeedScaled(tag.getInteger("RotationSpeed"));
            for (int i = 0; i < 4; i++) {
				mill.setBladeColor(i, tag.getInteger("Blade" + i));
			}
        }

		return mill;
    }

	@Override
	public void onEntityTracked(EntityTracker var1, EntityTrackerEntry var2, WindMillEntity var3) {}
}
