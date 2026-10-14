package ivangeevo.btx.config;

import ivangeevo.btx.BTXMod;
import turniplabs.halplibe.util.TomlConfigHandler;
import turniplabs.halplibe.util.toml.Toml;

import java.io.IOException;

public class BTXConfig {
	private static TomlConfigHandler CONFIG;

	public static final String ID_S_AND_TYPES = "ID's and Types Settings";
	public static final String BTW_RELATED = "Better Than Wolves General Settings";

	private static int BLOCK_ID_STARTING_FROM = 12500;
	private static int ITEM_ID_STARTING_FROM = 28500;

	private static int NET_ENTITY_TYPE_STARTING_FROM = 100;

	public static int MILLSTONE_SCREEN_ID = 44;

	private static int currentBlockID;
	private static int currentItemID;
	private static int currentNetEntityType;

	public static boolean disableGearBoxPowerDrain = false;

	public static void init() {
		Toml properties = new Toml("Better Than [X] Configurations.toml");

		properties.addCategory(ID_S_AND_TYPES)
			.addEntry("cfgVersion", 1)
			.addEntry("BLOCK_ID_STARTING_FROM", BLOCK_ID_STARTING_FROM)
			.addEntry("ITEM_ID_STARTING_FROM", ITEM_ID_STARTING_FROM)
			.addEntry("NET_ENTITY_TYPE_STARTING_FROM", NET_ENTITY_TYPE_STARTING_FROM);

		properties.addCategory(BTW_RELATED)
			.addEntry("DISABLE_GEARBOX_POWER_DRAIN", disableGearBoxPowerDrain);

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

		// ID's and Types
		currentBlockID = BLOCK_ID_STARTING_FROM = CONFIG.getInt(ID_S_AND_TYPES + ".BLOCK_ID_STARTING_FROM");
		currentItemID = ITEM_ID_STARTING_FROM = CONFIG.getInt(ID_S_AND_TYPES + ".ITEM_ID_STARTING_FROM");
		currentNetEntityType = NET_ENTITY_TYPE_STARTING_FROM = CONFIG.getInt(ID_S_AND_TYPES + ".NET_ENTITY_TYPE_STARTING_FROM");

		// BTW General Settings
		disableGearBoxPowerDrain = CONFIG.getBoolean(BTW_RELATED + ".DISABLE_GEARBOX_POWER_DRAIN");
	}

	public static int blockID() {
		return currentBlockID++;
	}
	public static int itemID() {
		return currentItemID++;
	}
	public static int netEntityTypeID() {
		return currentNetEntityType++;
	}
}
