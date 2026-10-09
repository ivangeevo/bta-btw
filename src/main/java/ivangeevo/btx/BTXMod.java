package ivangeevo.btx;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.config.BTXConfig;
import ivangeevo.btx.item.BTXItems;
import net.fabricmc.api.ModInitializer;
import turniplabs.halplibe.HalpLibe;
import turniplabs.halplibe.event.defs.CommonEvents;
import turniplabs.halplibe.util.dependency.Key;

public class BTXMod implements ModInitializer {
	public static final String MOD_ID = HalpLibe.registerMod("btx", true);

	@Override
	public void onInitialize() {
		CommonEvents.BEFORE_GAME_START.listen(Key.of(MOD_ID), this::beforeGameStart);
		CommonEvents.AFTER_GAME_START.listen(Key.of(MOD_ID), this::afterGameStart);
		CommonEvents.AFTER_BLOCK_INIT.listen(Key.of(MOD_ID), BTXBlocks::afterBlockInit);
		CommonEvents.AFTER_ITEM_INIT.listen(Key.of(MOD_ID), BTXItems::afterItemInit);

		BTXGlobals.LOGGER.info("Better Than [X] initialized.");
	}

	public void beforeGameStart() {
		BTXConfig.init();
		// -> Entities
	}

	public void afterGameStart() {
		BTXCreativeContents.init();
	}
}
