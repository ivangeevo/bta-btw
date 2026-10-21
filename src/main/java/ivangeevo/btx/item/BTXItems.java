package ivangeevo.btx.item;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.items.ItemArmorSoulforged;
import ivangeevo.btx.item.items.ItemDung;
import ivangeevo.btx.item.items.ItemFoulFood;
import ivangeevo.btx.item.items.ItemKibble;
import ivangeevo.btx.item.items.ItemToolAreaPickaxe;
import ivangeevo.btx.item.items.ItemToolAreaShovel;
import ivangeevo.btx.item.items.ItemToolBattleAxe;
import ivangeevo.btx.item.items.ItemToolMattock;
import ivangeevo.btx.item.items.ItemWaterWheel;
import ivangeevo.btx.item.items.ItemWindMill;
import net.minecraft.core.data.tag.Tag;
import net.minecraft.core.enums.HumanArmorShape;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemFood;
import net.minecraft.core.item.ItemSeeds;
import net.minecraft.core.item.material.ArmorMaterial;
import net.minecraft.core.item.material.ToolMaterial;
import net.minecraft.core.item.tag.ItemTags;
import net.minecraft.core.item.tool.ItemToolAxe;
import net.minecraft.core.item.tool.ItemToolHoe;
import net.minecraft.core.item.tool.ItemToolSword;
import turniplabs.halplibe.helper.ItemBuilder;

import java.util.function.UnaryOperator;

import static ivangeevo.btx.config.BTXConfig.itemID;

public final class BTXItems {

	public static Item CEMENT_BUCKET;
	public static Item HEMP_SEEDS;
	public static Item HEMP;
	public static Item GEAR;
	public static Item FLOUR;
	public static Item HEMP_FIBERS;
	public static Item SCOURED_LEATHER;
	public static Item DONUT;
	public static Item DUNG;
	public static Item WATERWHEEL;
	public static Item SAIL;
	public static Item WINDMILL;
	public static Item FABRIC;
	public static Item WICKER;
	public static Item TANNED_LEATHER;
	public static Item STRAP;
	public static Item BELT;
	public static Item FOUL_FOOD;
	public static Item KIBBLE;
	public static Item FOOD_WOLF_CHOP_RAW;
	public static Item FOOD_WOLF_CHOP_COOKED;
	public static Item WOOD_BLADE;
	public static Item GLUE;
	public static Item TALLOW;
	public static Item HAFT;
	public static Item SOULFORGED_STEEL_INGOT;
	public static Item SOULFORGED_STEEL_PICKAXE;
	public static Item SOULFORGED_STEEL_SHOVEL;
	public static Item SOULFORGED_STEEL_MATTOCK;
	public static Item SOULFORGED_STEEL_HOE;
	public static Item SOULFORGED_STEEL_AXE;
	public static Item SOULFORGED_STEEL_BATTLEAXE;
	public static Item SOULFORGED_STEEL_SWORD;
	public static Item SOULFORGED_STEEL_HELMET;
	public static Item SOULFORGED_STEEL_CHESTPLATE;
	public static Item SOULFORGED_STEEL_LEGGINGS;
	public static Item SOULFORGED_STEEL_BOOTS;
	public static Item GROUND_NETHERRACK;
	public static Item HELLFIRE_DUST;
	public static Item CONCENTRATED_HELLFIRE;
	public static Item STEEL_ARMOR_PLATE;
	public static Item COMPOSITE_BOW;
	public static Item BROADHEAD;
	public static Item BROADHEAD_ARROW;
	public static Item COAL_DUST;

	private BTXItems() {}

	public static void afterItemInit() {
		HEMP_SEEDS = registerItem("hemp_seeds", (key, namespaceId, id) ->
			new ItemSeeds(key, namespaceId, itemID(), BTXBlocks.HEMP).withTags(new Tag[]{ItemTags.CHICKENS_FAVOURITE_ITEM})
		);
		HEMP = registerItem("hemp", Item::new);
		GEAR = registerItem("gear", Item::new);
		FLOUR = registerItem("flour", Item::new);
		HEMP_FIBERS = registerItem("hemp_fibers", Item::new);
		SCOURED_LEATHER = registerItem("scoured_leather", Item::new);
		DUNG = registerItem("dung", ItemDung::new);
		WATERWHEEL = registerItem("waterwheel", (key, namespaceId, id) ->
			new ItemWaterWheel(key, namespaceId, id).setMaxStackSize(1)
		);
		SAIL = registerItem("sail", (key, namespaceId, id) ->
			new Item(key, namespaceId, id).setMaxStackSize(1)
		);
		WINDMILL = registerItem("windmill", (key, namespaceId, id) ->
			new ItemWindMill(key, namespaceId, id).setMaxStackSize(1)
		);
		FABRIC = registerItem("fabric", Item::new);
		TANNED_LEATHER = registerItem("tanned_leather", Item::new); //recipe needed
		BELT = registerItem("belt", Item::new); //recipe needed
		WOOD_BLADE = registerItem("wood_blade", (key, namespaceId, id) ->
			new Item(key, namespaceId, id).setMaxStackSize(1)
		);
		CONCENTRATED_HELLFIRE = registerItem("concentrated_hellfire", Item::new); //recipe needed
		HELLFIRE_DUST = registerItem("hellfire_dust", Item::new);
		FOUL_FOOD = registerItem("foul_food", ItemFoulFood::new);
		KIBBLE = registerItem("kibble", ItemKibble::new);
		COAL_DUST = registerItem("coal_dust", Item::new);
		GROUND_NETHERRACK = registerItem("ground_netherrack", Item::new);
		WICKER = registerItem("wicker", Item::new);
		FOOD_WOLF_CHOP_RAW = registerItem("food_wolf_chop_raw", (key, namespaceId, id) ->
			new ItemFood(key, namespaceId, id, 3, 16, false, 4)
		);
		FOOD_WOLF_CHOP_COOKED = registerItem("food_wolf_chop_cooked", (key, namespaceId, id) ->
			new ItemFood(key, namespaceId, id, 8, 16, false, 4)
		);
		DONUT = registerItem("donut", (key, namespaceId, id) ->
			new ItemFood(key, namespaceId, id, 1, 0, false, 4)
		);

		SOULFORGED_STEEL_INGOT = registerItem("soulforged_steel_ingot", Item::new);
		STEEL_ARMOR_PLATE = registerItem("steel_armor_plate", Item::new);

		ToolMaterial tool = BTXMaterials.SOULFORGED_STEEL_TOOL;
		SOULFORGED_STEEL_PICKAXE = registerItem("soulforged_steel_pickaxe", (key, namespaceId, id) -> new ItemToolAreaPickaxe(key, namespaceId, id, tool));
		SOULFORGED_STEEL_SHOVEL = registerItem("soulforged_steel_shovel", (key, namespaceId, id) -> new ItemToolAreaShovel(key, namespaceId, id, tool));
		SOULFORGED_STEEL_MATTOCK = registerItem("soulforged_steel_mattock", (key, namespaceId, id) -> new ItemToolMattock(key, namespaceId, id, tool));
		SOULFORGED_STEEL_HOE = registerItem("soulforged_steel_hoe", (key, namespaceId, id) -> new ItemToolHoe(key, namespaceId, id, tool));
		SOULFORGED_STEEL_AXE = registerItem("soulforged_steel_axe", (key, namespaceId, id) -> new ItemToolAxe(key, namespaceId, id, tool));
		SOULFORGED_STEEL_BATTLEAXE = registerItem("soulforged_steel_battleaxe", (key, namespaceId, id) -> new ItemToolBattleAxe(key, namespaceId, id, tool));
		SOULFORGED_STEEL_SWORD = registerItem("soulforged_steel_sword", (key, namespaceId, id) -> new ItemToolSword(key, namespaceId, id, tool));

		ArmorMaterial armor = BTXMaterials.SOULFORGED_STEEL_ARMOR;
		SOULFORGED_STEEL_HELMET = registerItem("soulforged_steel_helmet", (key, namespaceId, id) -> new ItemArmorSoulforged(key, namespaceId, id, armor, HumanArmorShape.HEAD));
		SOULFORGED_STEEL_CHESTPLATE = registerItem("soulforged_steel_chestplate", (key, namespaceId, id) -> new ItemArmorSoulforged(key, namespaceId, id, armor, HumanArmorShape.CHEST));
		SOULFORGED_STEEL_LEGGINGS = registerItem("soulforged_steel_leggings", (key, namespaceId, id) -> new ItemArmorSoulforged(key, namespaceId, id, armor, HumanArmorShape.LEGS));
		SOULFORGED_STEEL_BOOTS = registerItem("soulforged_steel_boots", (key, namespaceId, id) -> new ItemArmorSoulforged(key, namespaceId, id, armor, HumanArmorShape.BOOTS));

		STRAP = registerItem("strap", Item::new);
		GLUE = registerItem("glue", Item::new);
		HAFT = registerItem("haft", Item::new);
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
