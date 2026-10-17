package ivangeevo.btx.entity;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.block.entity.TileEntityMillStone;
import ivangeevo.btx.block.entity.TileEntityTurntable;
import ivangeevo.btx.entity.entities.WaterWheelEntity;
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
	}

	private static void initializeEntities() {
		EntityDispatcher dispatcher = EntityDispatcher.getInstance();

		// Tile Entities
		EntityHelper.addMapping(TileEntityMillStone.class, NamespaceID.fromPool(BTXMod.MOD_ID, "millstone"));
		EntityHelper.addMapping(TileEntityTurntable.class, NamespaceID.fromPool(BTXMod.MOD_ID, "turntable"));

		// Custom Entities
		dispatcher.addMapping(WindMillEntity.class, NamespaceID.fromPool(BTXMod.MOD_ID, "windmill"), WindMillEntity::new);
		dispatcher.addMapping(WaterWheelEntity.class, NamespaceID.fromPool(BTXMod.MOD_ID, "waterwheel"), WaterWheelEntity::new);
	}
}
