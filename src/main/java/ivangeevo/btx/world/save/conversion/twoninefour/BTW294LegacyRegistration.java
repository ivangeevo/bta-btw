package ivangeevo.btx.world.save.conversion.twoninefour;

import ivangeevo.btx.block.entity.*;
import ivangeevo.btx.block.entity.TileEntityCement;
import ivangeevo.btx.block.entity.TileEntityPulley;
import ivangeevo.btx.entity.entities.EntityBroadheadArrow;
import ivangeevo.btx.entity.entities.WaterWheelEntity;
import ivangeevo.btx.entity.entities.WindMillEntity;
import ivangeevo.btx.entity.entities.lift.BlockLiftedByPlatformEntity;
import ivangeevo.btx.entity.entities.lift.MovingAnchorEntity;
import ivangeevo.btx.entity.entities.lift.MovingPlatformEntity;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntityDispatcher;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityDispatcher;

/**
 * <h2>Why no conversion step is needed</h2>
 * <p>
 * Entities and tile entities require <strong>NO conversion step</strong>.
 * Both dispatchers maintain a read-only {@code "Legacy"} name table, so
 * each addon class must be registered twice:
 * </p>
 *
 * <ul>
 *   <li>Under its modern {@code NamespaceID}, used when <strong>SAVING</strong>.</li>
 *   <li>Under BTW's old saved name, used when <strong>LOADING</strong> a b1.7.3 world.</li>
 * </ul>
 *
 * <p>
 * Call {@code register()} once, after {@code TileEntityDispatcher.init()}
 * and the vanilla {@code EntityDispatcher} have run.
 * </p>
 *
 * <h2>Registration constraints</h2>
 * <ul>
 *   <li>
 *     <strong>Tile entities:</strong> Legacy names must be unique.
 *     {@code addMapping} throws an exception on duplicates.
 *     None of BTW's names collide with vanilla ones.
 *   </li>
 *   <li>
 *     <strong>Entities:</strong> Legacy numeric IDs must also be unique.
 *     BTW's tracker IDs {@code 222-227} do not collide with vanilla IDs
 *     {@code 1-96}.
 *   </li>
 * </ul>
 *
 * <h2>NBT compatibility</h2>
 * <p>
 * The addon classes read their own NBT, so preserve BTW's original key
 * names as fallbacks when loading legacy data. Examples include:
 * </p>
 *
 * <ul>
 *   <li>{@code "dryTime"}</li>
 *   <li>{@code "grindCounter"}</li>
 *   <li>{@code "iHoppeContainedSoulCount"} (original misspelling)</li>
 * </ul>
 */
public final class BTW294LegacyRegistration {
    private BTW294LegacyRegistration() {}

    public static void register() {
        // Tile entities: (class, modern key, BTW saved name)
        // tile(BtwCementEntity.class,          "cement",           "Cement");
        // tile(BtwCauldronEntity.class,        "cauldron",         "Cauldron");
        // tile(BtwMillStoneEntity.class,       "mill_stone",       "MillStone");
        // tile(BtwHopperEntity.class,          "hopper",           "Hopper");
        // tile(BtwBlockDispenserEntity.class,  "block_dispenser",  "BlockDispenser");
        // tile(BtwPulleyEntity.class,          "pulley",           "Pulley");
        // tile(BtwTurntableEntity.class,       "turntable",        "Turntable");
        // tile(BtwUnfiredPotteryEntity.class,  "unfired_pottery",  "UnfiredPottery");
        // tile(BtwVaseEntity.class,            "vase",             "Vase");
        // tile(BtwCrucibleEntity.class,        "crucible",         "Crucible");
		tile(TileEntityCauldron.class,"Cauldron");
		tile(TileEntityMillStone.class,"MillStone");
		tile(TileEntityHopper.class, "Hopper");
		tile(TileEntityBlockDispenser.class, "BlockDispenser");
		tile(TileEntityPulley.class, "Pulley");
		tile(TileEntityCement.class, "Cement");
		tile(TileEntityTurntable.class, "Turntable");
		tile(TileEntityVase.class, "Vase");
		// "UnfiredPottery" is left unmapped on purpose: it only held 2.94's in-kiln cook timer, and BTX's unfired
		// pottery has no tile entity (BlockLogicKiln does the cooking), so the block converts and the timer is dropped.
		tile(TileEntityCrucible.class, "Crucible");

        // Entities: (class, modern key, factory, BTW saved name, BTW tracker id)
        // entity(BtwWaterWheel.class,       "water_wheel",             BtwWaterWheel::new,       "WaterWheel",            222);
        // entity(BtwWindMill.class,         "wind_mill",               BtwWindMill::new,         "WindMill",              223);
        // entity(BtwMovingAnchor.class,     "moving_anchor",           BtwMovingAnchor::new,     "MovingAnchor",          224);
        // entity(BtwMovingPlatform.class,   "moving_platform",         BtwMovingPlatform::new,   "MovingPlatform",        225);
        // entity(BtwLiftedBlock.class,      "block_lifted_by_platform",BtwLiftedBlock::new,      "BlockLiftedByPlatform", 226);
        // entity(BtwBroadheadArrow.class,   "broadhead_arrow",         BtwBroadheadArrow::new,   "BroadheadArrow",        227);
		entity(WaterWheelEntity.class, "WaterWheel", 222);
		entity(WindMillEntity.class, "WindMill", 223);
		entity(MovingAnchorEntity.class, "MovingAnchor", 224);
		entity(MovingPlatformEntity.class, "MovingPlatform", 225);
		entity(BlockLiftedByPlatformEntity.class, "BlockLiftedByPlatform", 226);
		entity(EntityBroadheadArrow.class, "BroadheadArrow", 227);
	}

	private static void tile(Class<? extends TileEntity> clazz, String legacyName) {
		TileEntityDispatcher.Legacy.addMapping(clazz, legacyName);
	}

	private static <T extends Entity> void entity(Class<T> clazz, String legacyName, int legacyId) {
		EntityDispatcher.Legacy.addMapping(clazz, legacyName, legacyId);
	}
}
