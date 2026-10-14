package ivangeevo.btx.util.utils;

import ivangeevo.btx.world.TickPersistence;
import net.minecraft.core.block.Block;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public class WorldUtils {

	public static boolean isUpdateScheduled(World world, TilePosc pos, Block<?> block) {
		return world instanceof TickPersistence tp && tp.btx$isUpdateScheduled(pos, block);
	}

}
