package ivangeevo.btx.entity;

import ivangeevo.btx.BTXGlobals;
import ivangeevo.btx.BTXMod;
import ivangeevo.btx.block.entity.TileEntityMillStone;
import ivangeevo.btx.config.BTXConfig;
import ivangeevo.btx.entity.entities.WindMillEntity;
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
		BTXGlobals.LOGGER.info("[WindMill] BTXEntities.init() done, netEntityTypeID={}", BTXConfig.netEntityTypeID());
	}

	private static void initializeEntities() {
		EntityDispatcher dispatcher = EntityDispatcher.getInstance();

		// Tile Entities
		EntityHelper.addMapping(TileEntityMillStone.class, NamespaceID.fromPool(BTXMod.MOD_ID, "millstone"));

		// Custom Entities
		dispatcher.addMapping(WindMillEntity.class, NamespaceID.fromPool(BTXMod.MOD_ID, "windmill"), WindMillEntity::new);
	}
}
