package ivangeevo.btx;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.config.BTXConfig;
import ivangeevo.btx.item.BTXItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.HalpLibe;
import turniplabs.halplibe.event.defs.CommonEvents;
import turniplabs.halplibe.util.dependency.Key;

public class BTXMod implements ModInitializer {
	public static final String MOD_ID = HalpLibe.registerMod("btx", true);

	@Override
	public void onInitialize() {
		CommonEvents.BEFORE_GAME_START.listen(Key.of(MOD_ID), this::beforeGameStart);
		CommonEvents.AFTER_GAME_START.listen(Key.of(MOD_ID), this::afterGameStart);

		BTXGlobals.LOGGER.info("Better Than [X] initialized.");
	}

	public void beforeGameStart() {
		BTXConfig.init();
		// -> Entities
		BTXBlocks.init();
		BTXItems.init();
	}

	public void afterGameStart() {
		BTXCreativeContents.init();
	}
}
