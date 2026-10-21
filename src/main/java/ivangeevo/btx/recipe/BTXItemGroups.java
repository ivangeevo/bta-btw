package ivangeevo.btx.recipe;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.BTXItems;
import net.minecraft.core.block.Block;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.DyeColor;

import java.util.ArrayList;
import java.util.List;

public final class BTXItemGroups {
	public static final String SIDINGS_PLANKS = "btx:sidings_planks";
	public static final String MOULDINGS_PLANKS = "btx:mouldings_planks";
	public static final String CORNERS_PLANKS = "btx:corners_planks";
	public static final String SOULFORGED_TOOLS = "btx:soulforged_tools";
	public static final String SOULFORGED_ARMOR = "btx:soulforged_armor";

	private BTXItemGroups() {
	}

	public static void register() {
		register(SIDINGS_PLANKS, withPaintedVariants(BTXBlocks.SIDING_OAK, BTXBlocks.SIDING_OAK_PAINTED));
		register(MOULDINGS_PLANKS, withPaintedVariants(BTXBlocks.MOULDING_OAK, BTXBlocks.MOULDING_OAK_PAINTED));
		register(CORNERS_PLANKS, withPaintedVariants(BTXBlocks.CORNER_OAK, BTXBlocks.CORNER_OAK_PAINTED));
		register(SOULFORGED_TOOLS, stacksOf(
			BTXItems.SOULFORGED_STEEL_PICKAXE, BTXItems.SOULFORGED_STEEL_SHOVEL, BTXItems.SOULFORGED_STEEL_MATTOCK,
			BTXItems.SOULFORGED_STEEL_AXE, BTXItems.SOULFORGED_STEEL_HOE, BTXItems.SOULFORGED_STEEL_SWORD,
			BTXItems.SOULFORGED_STEEL_BATTLEAXE));
		register(SOULFORGED_ARMOR, stacksOf(
			BTXItems.SOULFORGED_STEEL_HELMET, BTXItems.SOULFORGED_STEEL_CHESTPLATE,
			BTXItems.SOULFORGED_STEEL_LEGGINGS, BTXItems.SOULFORGED_STEEL_BOOTS));
	}

	private static void register(String key, List<ItemStack> stacks) {
		if (Registries.ITEM_GROUPS.getItem(key) == null) {
			Registries.ITEM_GROUPS.register(key, stacks);
		}
	}

	private static List<ItemStack> stacksOf(Item... items) {
		List<ItemStack> stacks = new ArrayList<>();
		for (Item item : items) {
			stacks.add(new ItemStack(item));
		}
		return stacks;
	}

	private static List<ItemStack> withPaintedVariants(Block<?> plain, Block<?> painted) {
		List<ItemStack> stacks = new ArrayList<>();
		stacks.add(new ItemStack(plain));
		for (DyeColor color : DyeColor.values()) {
			stacks.add(new ItemStack(painted, 1, color.blockMeta << 4));
		}
		return stacks;
	}
}