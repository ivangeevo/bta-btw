package ivangeevo.btx;

import ivangeevo.btx.item.BTXItems;
import net.minecraft.core.item.IItemConvertible;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryCategory;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryPlacement;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryRegistry;

public final class BTXCreativeContents {
	private static CreativeInventoryRegistry REG = CreativeInventoryRegistry.INSTANCE;

	public static void init() {
		REG.register(BTXItems.HEMP, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.HEMP_FIBERS, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.HEMP_SEEDS, inCategory(CreativeInventoryCategory.ORGANIC));

	}
	private static CreativeInventoryPlacement.After after(IItemConvertible item) {
		return new CreativeInventoryPlacement.After(() -> item);
	}
	private static CreativeInventoryPlacement.Category inCategory(CreativeInventoryCategory category) {
		return new CreativeInventoryPlacement.Category(category);
	}
}
