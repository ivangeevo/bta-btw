package ivangeevo.btx.world.save.conversion;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.world.save.conversion.twoninefour.BTW294LegacyRegistration;
import ivangeevo.btx.world.save.conversion.twoninefour.BTW294SaveConverter;

public final class BTXLegacyConversion {
    public static void init() {
        // 1. item rules (needs BTXItems / BTXBlocks populated)
        BTW294SaveConverter.BtwItems.simple(238, BTXItems.WINDMILL.id);
        // 2.94's grate, rollers and wicker were items only; they become the CE pane blocks
        BTW294SaveConverter.BtwItems.simple(240, BTXBlocks.GRATE.id());
        BTW294SaveConverter.BtwItems.simple(234, BTXBlocks.SLATS.id());
        BTW294SaveConverter.BtwItems.simple(241, BTXBlocks.WICKER_PANE.id());
        // BtwSaveConverter.BtwItems.rule(237, d -> new int[]{ d > 0 ? COMPANION_SLAB : COMPANION_CUBE, 0 });

		// 2. old BTW names as aliases for classes already registered
        BTW294LegacyRegistration.register();

        // 3. replaces converters[0] and inserts the save converter at index 0
        BTW294SaveConverter.install();
    }
}
