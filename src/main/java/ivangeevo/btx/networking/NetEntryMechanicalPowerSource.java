package ivangeevo.btx.networking;

import ivangeevo.btx.entity.entities.MechanicalPowerSourceEntity;
import net.minecraft.core.net.entity.EntityTracker;
import net.minecraft.core.net.entity.EntityTrackerEntry;
import net.minecraft.core.net.entity.ITrackedEntry;
import net.minecraft.core.net.entity.IVehicleEntry;

public abstract class NetEntryMechanicalPowerSource<T extends MechanicalPowerSourceEntity> implements IVehicleEntry<T>, ITrackedEntry<T> {

    @Override public int getTrackingDistance() {
		return 160;
	}
    @Override public int getMovementPacketDelay() {
		return 3;
	}
    @Override public boolean sendMotionUpdates() {
		return false;
	}

    @Override
    public void onEntityTracked(EntityTracker tracker, EntityTrackerEntry entry, T entity) {}

}
