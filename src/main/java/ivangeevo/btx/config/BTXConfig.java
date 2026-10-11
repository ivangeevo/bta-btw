package ivangeevo.btx.config;

import ivangeevo.btx.BTXMod;
import turniplabs.halplibe.util.TomlConfigHandler;
import turniplabs.halplibe.util.toml.Toml;

import java.io.IOException;

public class BTXConfig {
	private static TomlConfigHandler CONFIG;

	public static final String CATEGORY = "General";

	public static final int MILLSTONE_SCREEN_ID = 44;

	private static int BLOCK_ID_STARTING_FROM = 12500;
	private static int ITEM_ID_STARTING_FROM = 28500;

	private static int currentBlockID;
	private static int currentItemID;

	public static void init() {
		Toml properties = new Toml("Better Than [X] Configurations.toml");

		properties.addCategory(CATEGORY)
			.addEntry("cfgVersion", 1)
			.addEntry("MILLSTONE_SCREEN_ID", MILLSTONE_SCREEN_ID)
			.addEntry("BLOCK_ID_STARTING_FROM", BLOCK_ID_STARTING_FROM)
			.addEntry("ITEM_ID_STARTING_FROM", ITEM_ID_STARTING_FROM);

		CONFIG = new TomlConfigHandler(BTXMod.MOD_ID, properties);

		if (CONFIG.getConfigFile().exists()) {
			CONFIG.loadConfig();
		} else {
			try {
				CONFIG.getConfigFile().createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			CONFIG.writeConfig();
		}

		currentBlockID = BLOCK_ID_STARTING_FROM = CONFIG.getInt(CATEGORY + ".BLOCK_ID_STARTING_FROM");
		currentItemID = ITEM_ID_STARTING_FROM = CONFIG.getInt(CATEGORY + ".ITEM_ID_STARTING_FROM");
	}

	public static int blockID() {
		return currentBlockID++;
	}
	public static int itemID() {
		return currentItemID++;
	}
}
