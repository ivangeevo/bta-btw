package ivangeevo.btx;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.BTXItems;
import net.minecraft.core.item.IItemConvertible;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryCategory;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryPlacement;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryRegistry;

public final class BTXCreativeContents {
	private static CreativeInventoryRegistry REG = CreativeInventoryRegistry.INSTANCE;

	public static void init() {
		// Items
		REG.register(BTXItems.HEMP, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.HEMP_FIBERS, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.HEMP_SEEDS, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.GEAR, inCategory(CreativeInventoryCategory.BASICS));
		REG.register(BTXItems.FLOUR, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.SCOURED_LEATHER, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.FABRIC, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.DUNG, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.WINDMILL, inCategory(CreativeInventoryCategory.PLACEABLES));
		REG.register(BTXItems.SAIL, inCategory(CreativeInventoryCategory.MISCELLANEOUS));

		// Blocks
		// TODO: The blocks we add probably should be in their own category for machines/mech devices
		REG.register(BTXBlocks.MILLSTONE, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.HAND_CRANK, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.AXLE, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.AXLE_POWER_SOURCE, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.GEARBOX, inCategory(CreativeInventoryCategory.WORKBENCHES));
	}
	private static CreativeInventoryPlacement.After after(IItemConvertible item) {
		return new CreativeInventoryPlacement.After(() -> item);
	}
	private static CreativeInventoryPlacement.Category inCategory(CreativeInventoryCategory category) {
		return new CreativeInventoryPlacement.Category(category);
	}
}
