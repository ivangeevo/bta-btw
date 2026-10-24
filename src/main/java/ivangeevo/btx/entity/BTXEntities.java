package ivangeevo.btx.entity;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.block.entity.TileEntityBlockDispenser;
import ivangeevo.btx.block.entity.TileEntityCauldron;
import ivangeevo.btx.block.entity.TileEntityCrucible;
import ivangeevo.btx.block.entity.TileEntityHopper;
import ivangeevo.btx.block.entity.TileEntityMillStone;
import ivangeevo.btx.block.entity.TileEntityCement;
import ivangeevo.btx.block.entity.TileEntityPulley;
import ivangeevo.btx.block.entity.TileEntityTurntable;
import ivangeevo.btx.block.entity.TileEntityVase;
import ivangeevo.btx.entity.entities.WaterWheelEntity;
import ivangeevo.btx.entity.entities.EntityBroadheadArrow;
import ivangeevo.btx.entity.entities.WindMillEntity;
import ivangeevo.btx.entity.entities.lift.BlockLiftedByPlatformEntity;
import ivangeevo.btx.entity.entities.lift.MovingAnchorEntity;
import ivangeevo.btx.entity.entities.lift.MovingPlatformEntity;
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
		EntityHelper.addMapping(TileEntityCrucible.class, NamespaceID.fromPool(BTXMod.MOD_ID, "crucible"));
		EntityHelper.addMapping(TileEntityCauldron.class, NamespaceID.fromPool(BTXMod.MOD_ID, "cauldron"));
		EntityHelper.addMapping(TileEntityVase.class, NamespaceID.fromPool(BTXMod.MOD_ID, "vase"));
		EntityHelper.addMapping(TileEntityHopper.class, NamespaceID.fromPool(BTXMod.MOD_ID, "hopper"));
		EntityHelper.addMapping(TileEntityBlockDispenser.class, NamespaceID.fromPool(BTXMod.MOD_ID, "block_dispenser"));
		EntityHelper.addMapping(TileEntityPulley.class, NamespaceID.fromPool(BTXMod.MOD_ID, "pulley"));
		EntityHelper.addMapping(TileEntityCement.class, NamespaceID.fromPool(BTXMod.MOD_ID, "cement"));

		// Custom Entities
		dispatcher.addMapping(WindMillEntity.class, NamespaceID.fromPool(BTXMod.MOD_ID, "windmill"), WindMillEntity::new);
		dispatcher.addMapping(WaterWheelEntity.class, NamespaceID.fromPool(BTXMod.MOD_ID, "waterwheel"), WaterWheelEntity::new);
		dispatcher.addMapping(MovingAnchorEntity.class, NamespaceID.fromPool(BTXMod.MOD_ID, "moving_anchor"), MovingAnchorEntity::new);
		dispatcher.addMapping(MovingPlatformEntity.class, NamespaceID.fromPool(BTXMod.MOD_ID, "moving_platform"), MovingPlatformEntity::new);
		dispatcher.addMapping(BlockLiftedByPlatformEntity.class, NamespaceID.fromPool(BTXMod.MOD_ID, "block_lifted_by_platform"), BlockLiftedByPlatformEntity::new);
		dispatcher.addMapping(EntityBroadheadArrow.class, NamespaceID.fromPool(BTXMod.MOD_ID, "broadhead_arrow"), EntityBroadheadArrow::new);
	}
}
