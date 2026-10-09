package ivangeevo.btx.item;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.block.BTXBlocks;
import net.minecraft.core.data.tag.Tag;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemSeeds;
import net.minecraft.core.item.tag.ItemTags;
import turniplabs.halplibe.helper.ItemBuilder;

import java.util.function.UnaryOperator;

import static ivangeevo.btx.config.BTXConfig.itemID;

public final class BTXItems {

	public static Item HEMP;
	public static Item HEMP_FIBERS;
	public static Item HEMP_SEEDS;


	private BTXItems() {}


	public static void afterItemInit() {
		HEMP = registerItem("hemp", Item::new);
		HEMP_FIBERS = registerItem("hemp_fibers", Item::new);
		HEMP_SEEDS = registerItem("hemp_seeds", (key, namespaceId, id) ->
			new ItemSeeds(key, namespaceId, itemID(), BTXBlocks.HEMP).withTags(new Tag[]{ItemTags.CHICKENS_FAVOURITE_ITEM})
		);
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
		return builder.build(factory.create(translationKey, namespaceId, itemID()));
	}

	/** Creates the item once the key, namespace ID and numeric ID are known. **/
	@FunctionalInterface
	public interface ItemFactory<T extends Item> {
		T create(String key, String namespaceId, int id);
	}

}
