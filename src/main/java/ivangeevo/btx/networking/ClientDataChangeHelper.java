package ivangeevo.btx.networking;

import net.minecraft.core.block.Block;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public final class ClientDataChangeHelper {
	public static void notifyChange(
		World world, TilePosc pos, Block<?> oldBlock, int oldData, Block<?> newBlock, int newData
	) {
		System.out.println("[BTX] notifyChange " + pos.x() + "," + pos.y() + "," + pos.z()
			+ " old=" + world.getBlockType(pos) + " oldData=" + world.getBlockData(pos)
			+ " new=" + newBlock + " newData=" + newData + " clientSide=" + world.isClientSide
		);

		if (oldBlock == newBlock) {
			if (oldData != newData && oldBlock != null
				&& oldBlock.getLogic() instanceof ClientDataChangeListener l) {
				l.clientNotificationOfDataChange(world, pos, oldData, newData);
			}
			return;
		}

		if (oldBlock != null && oldBlock.getLogic() instanceof ClientDataChangeListener l) {
			l.clientBlockRemoved(world, pos, oldData);
		}
		if (newBlock != null && newBlock.getLogic() instanceof ClientDataChangeListener l) {
			l.clientBlockAdded(world, pos);
		}
	}
}
