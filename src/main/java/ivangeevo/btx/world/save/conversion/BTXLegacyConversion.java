package ivangeevo.btx.world.save.conversion;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.world.save.conversion.twoninefour.BTW294LegacyRegistration;
import ivangeevo.btx.world.save.conversion.twoninefour.BTW294SaveConverter;
import net.minecraft.core.item.Items;

public final class BTXLegacyConversion {
    public static void init() {
        // 1. item rules (needs BTXItems / BTXBlocks populated)
        BTW294SaveConverter.BtwItems.simple(223, BTXItems.FOOD_WOLF_CHOP_RAW.id);
        BTW294SaveConverter.BtwItems.simple(224, BTXItems.FOOD_WOLF_CHOP_COOKED.id);
        BTW294SaveConverter.BtwItems.simple(225, Items.NETHERCOAL.id);
        BTW294SaveConverter.BtwItems.simple(226, BTXItems.HEMP_SEEDS.id);
        BTW294SaveConverter.BtwItems.simple(227, BTXItems.HEMP.id);
        BTW294SaveConverter.BtwItems.simple(228, BTXItems.GEAR.id);
        BTW294SaveConverter.BtwItems.simple(229, BTXItems.FLOUR.id);
        BTW294SaveConverter.BtwItems.simple(230, BTXItems.HEMP_FIBERS.id);
        BTW294SaveConverter.BtwItems.simple(231, BTXItems.SCOURED_LEATHER.id);
        BTW294SaveConverter.BtwItems.simple(232, BTXItems.DONUT.id);
        BTW294SaveConverter.BtwItems.simple(233, Items.ROPE.id);
        BTW294SaveConverter.BtwItems.simple(235, BTXItems.DUNG.id);
        BTW294SaveConverter.BtwItems.simple(236, BTXItems.WATERWHEEL.id);
        BTW294SaveConverter.BtwItems.simple(237, BTXItems.SAIL.id);
        BTW294SaveConverter.BtwItems.simple(238, BTXItems.WINDMILL.id);
        BTW294SaveConverter.BtwItems.simple(239, BTXItems.FABRIC.id);
        BTW294SaveConverter.BtwItems.simple(242, BTXItems.TANNED_LEATHER.id);
        BTW294SaveConverter.BtwItems.simple(243, BTXItems.STRAP.id);
        BTW294SaveConverter.BtwItems.simple(244, BTXItems.BELT.id);
        BTW294SaveConverter.BtwItems.simple(245, BTXItems.FOUL_FOOD.id);
        BTW294SaveConverter.BtwItems.simple(246, BTXItems.WOOD_BLADE.id);
        BTW294SaveConverter.BtwItems.simple(247, BTXItems.GLUE.id);
        BTW294SaveConverter.BtwItems.simple(249, BTXItems.HAFT.id);
        BTW294SaveConverter.BtwItems.simple(256, BTXItems.GROUND_NETHERRACK.id);
        BTW294SaveConverter.BtwItems.simple(257, BTXItems.HELLFIRE_DUST.id);
        BTW294SaveConverter.BtwItems.simple(258, BTXItems.CONCENTRATED_HELLFIRE.id);
        BTW294SaveConverter.BtwItems.simple(267, BTXItems.COAL_DUST.id);
        BTW294SaveConverter.BtwItems.simple(250, BTXItems.SOULFORGED_STEEL_INGOT.id);
        BTW294SaveConverter.BtwItems.simple(251, BTXItems.SOULFORGED_STEEL_PICKAXE.id);
        BTW294SaveConverter.BtwItems.simple(252, BTXItems.SOULFORGED_STEEL_SHOVEL.id);
        BTW294SaveConverter.BtwItems.simple(253, BTXItems.SOULFORGED_STEEL_HOE.id);
        BTW294SaveConverter.BtwItems.simple(254, BTXItems.SOULFORGED_STEEL_BATTLEAXE.id);
        BTW294SaveConverter.BtwItems.simple(255, BTXItems.SOULFORGED_STEEL_SWORD.id);
        BTW294SaveConverter.BtwItems.simple(259, BTXItems.STEEL_ARMOR_PLATE.id);
        BTW294SaveConverter.BtwItems.simple(260, BTXItems.SOULFORGED_STEEL_HELMET.id);
        BTW294SaveConverter.BtwItems.simple(261, BTXItems.SOULFORGED_STEEL_CHESTPLATE.id);
        BTW294SaveConverter.BtwItems.simple(262, BTXItems.SOULFORGED_STEEL_LEGGINGS.id);
        BTW294SaveConverter.BtwItems.simple(263, BTXItems.SOULFORGED_STEEL_BOOTS.id);

        // 2.94's grate, rollers and wicker were items only; they become the CE pane blocks
        BTW294SaveConverter.BtwItems.simple(240, BTXBlocks.GRATE.id());
        BTW294SaveConverter.BtwItems.simple(234, BTXBlocks.SLATS.id());
        BTW294SaveConverter.BtwItems.simple(241, BTXBlocks.WICKER_PANE.id());
        BTW294SaveConverter.BtwItems.blockRule(237, d -> new int[]{d > 0 ? BTXBlocks.COMPANION_SLAB.id() : BTXBlocks.COMPANION_CUBE.id(), 0});

		// 2. old BTW names as aliases for classes already registered
        BTW294LegacyRegistration.register();

        // 3. replaces converters[0] and inserts the save converter at index 0
        BTW294SaveConverter.install();
    }
}
