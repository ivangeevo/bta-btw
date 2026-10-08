package ivangeevo.btx.item;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.config.BTXConfig;
import net.minecraft.core.item.Item;
import turniplabs.halplibe.helper.ItemBuilder;

import java.util.function.UnaryOperator;

public final class BTXItems {

	public static Item HEMP;
	public static Item HEMP_FIBERS;
	public static Item HEMP_SEEDS;

	private static boolean initialized = false;

	private BTXItems() {}

	/** Creates the item once the key, namespace ID and numeric ID are known. **/
	@FunctionalInterface
	public interface ItemFactory<T extends Item> {
		T create(String key, String namespaceId, int id);
	}

	public static void init() {
		if (!initialized) {
			initialized = true;
			initializeItems();
		}
	}

	private static void initializeItems() {
		HEMP = registerItem("hemp", Item::new);
		HEMP_FIBERS = registerItem("hemp_fibers", Item::new);
		HEMP_SEEDS = registerItem("hemp_seeds", Item::new);
	}

	// No extra settings builder
	public static <T extends Item> T registerItem(String name, ItemFactory<T> factory) {
		return registerItem(name, factory, UnaryOperator.identity());
	}

	// With extra builder settings
	public static <T extends Item> T registerItem(String name, ItemFactory<T> factory, UnaryOperator<ItemBuilder> customizer) {
		String translationKey = name.replace("_", "."); // "hemp.fibers", no mod ID
		String namespaceId = BTXMod.MOD_ID + ":item/" + name; // "btx:item/hemp_fibers"
		ItemBuilder builder = customizer.apply(new ItemBuilder(BTXMod.MOD_ID));
		return builder.build(factory.create(translationKey, namespaceId, BTXConfig.itemID()));
	}

}
