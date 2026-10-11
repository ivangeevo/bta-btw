package ivangeevo.btx.entity;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.block.entity.TileEntityMillStone;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.util.collection.NamespaceID;
import turniplabs.halplibe.helper.EntityHelper;

public final class BTXEntities {
	private	static boolean initialized = false;

	public static void init() {
		if (!initialized) {
			initialized = true;
			initializeEntities();
		}
	}

	private static void initializeEntities() {
		EntityHelper helper = new EntityHelper();
		EntityDispatcher dispatcher = EntityDispatcher.getInstance();

		// Tile Entities
		EntityHelper.addMapping(TileEntityMillStone.class, NamespaceID.fromPool(BTXMod.MOD_ID, "millstone"));
	}
}
