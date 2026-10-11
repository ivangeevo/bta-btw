package ivangeevo.btx.networking;

import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public interface ClientDataChangeListener {
	void clientNotificationOfDataChange(World world, TilePosc pos, int oldData, int newData);
	default void clientBlockAdded(World world, TilePosc pos) {}
	default void clientBlockRemoved(World world, TilePosc pos, int oldData) {}
}
