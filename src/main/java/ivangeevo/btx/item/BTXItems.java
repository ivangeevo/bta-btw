package ivangeevo.btx.item;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.items.ItemWindMill;
import net.minecraft.core.data.tag.Tag;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemDye;
import net.minecraft.core.item.ItemSeeds;
import net.minecraft.core.item.tag.ItemTags;
import net.minecraft.core.util.helper.DyeColor;
import turniplabs.halplibe.helper.ItemBuilder;

import java.util.function.UnaryOperator;

import static ivangeevo.btx.config.BTXConfig.itemID;

public final class BTXItems {

	public static Item HEMP;
	public static Item HEMP_FIBERS;
	public static Item HEMP_SEEDS;
	public static Item GEAR;
	public static Item FLOUR;
	public static Item SCOURED_LEATHER;
	public static Item FABRIC;
	public static Item DUNG;
	public static Item WINDMILL;

	private BTXItems() {}

	public static void afterItemInit() {
		HEMP = registerItem("hemp", Item::new);
		HEMP_FIBERS = registerItem("hemp_fibers", Item::new);
		HEMP_SEEDS = registerItem("hemp_seeds", (key, namespaceId, id) ->
			new ItemSeeds(key, namespaceId, itemID(), BTXBlocks.HEMP).withTags(new Tag[]{ItemTags.CHICKENS_FAVOURITE_ITEM})
		);
		GEAR = registerItem("gear", Item::new);
		FLOUR = registerItem("flour", Item::new);
		SCOURED_LEATHER = registerItem("scoured_leather", Item::new);
		FABRIC = registerItem("fabric", Item::new);
		DUNG = registerItem("dung", (ItemDye::new));
		WINDMILL = registerItem("windmill", ItemWindMill::new);
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
