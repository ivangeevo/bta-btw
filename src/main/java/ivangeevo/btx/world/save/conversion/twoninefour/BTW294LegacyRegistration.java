package ivangeevo.btx.world.save.conversion.twoninefour;

import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntityDispatcher;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.entity.factories.EntityFactory;
import net.minecraft.core.util.collection.NamespaceID;

/**
 * Entities and tile entities need NO conversion step. Both dispatchers keep a read-only "Legacy" name table, so we
 * register each addon class twice:
 *   - under its modern NamespaceID (used when SAVING), and
 *   - under BTW's old saved name (used when LOADING a b1.7.3 world).
 * Call register() once, after TileEntityDispatcher.init() and the vanilla EntityDispatcher have run.
 *
 * Legacy tile entity names must be unique (addMapping throws on duplicates); none of BTW's collide with stock ones.
 * Legacy entity numeric IDs must be unique too; BTW's tracker IDs 222-227 don't collide with stock (1-96).
 *
 * The addon classes read their own NBT, so keep BTW's key names as a fallback when loading (e.g. "dryTime",
 * "grindCounter", and the misspelled "iHoppeContainedSoulCount").
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

        // Entities: (class, modern key, factory, BTW saved name, BTW tracker id)
        // entity(BtwWaterWheel.class,       "water_wheel",             BtwWaterWheel::new,       "WaterWheel",            222);
        // entity(BtwWindMill.class,         "wind_mill",               BtwWindMill::new,         "WindMill",              223);
        // entity(BtwMovingAnchor.class,     "moving_anchor",           BtwMovingAnchor::new,     "MovingAnchor",          224);
        // entity(BtwMovingPlatform.class,   "moving_platform",         BtwMovingPlatform::new,   "MovingPlatform",        225);
        // entity(BtwLiftedBlock.class,      "block_lifted_by_platform",BtwLiftedBlock::new,      "BlockLiftedByPlatform", 226);
        // entity(BtwBroadheadArrow.class,   "broadhead_arrow",         BtwBroadheadArrow::new,   "BroadheadArrow",        227);
    }

    private static void tile(Class<? extends TileEntity> clazz, String key, String legacyName) {
        TileEntityDispatcher.addMapping(clazz, NamespaceID.fromPool("btw", key));
        TileEntityDispatcher.Legacy.addMapping(clazz, legacyName);
    }

    private static <T extends Entity> void entity(Class<T> clazz, String key, EntityFactory<T> factory,
                                                  String legacyName, int legacyId) {
        EntityDispatcher.getInstance().addMapping(clazz, NamespaceID.fromPool("btw", key), factory);
        EntityDispatcher.Legacy.addMapping(clazz, legacyName, legacyId);
    }
}
