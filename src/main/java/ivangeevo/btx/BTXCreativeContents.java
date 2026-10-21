package ivangeevo.btx;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.BTXItems;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.IPainted;
import net.minecraft.core.item.IItemConvertible;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.DyeColor;
import org.jetbrains.annotations.NotNull;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryCategory;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryPlacement;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryRegistry;

import java.util.ArrayList;
import java.util.List;

public final class BTXCreativeContents {
	private static CreativeInventoryRegistry REG = CreativeInventoryRegistry.INSTANCE;

	private static final DyeColor[] RAINBOW_ORDER = new DyeColor[]{
		DyeColor.RED,
		DyeColor.ORANGE,
		DyeColor.YELLOW,
		DyeColor.LIME,
		DyeColor.GREEN,
		DyeColor.CYAN,
		DyeColor.LIGHT_BLUE,
		DyeColor.BLUE,
		DyeColor.PURPLE,
		DyeColor.MAGENTA,
		DyeColor.PINK,
		DyeColor.BROWN,
		DyeColor.WHITE,
		DyeColor.SILVER,
		DyeColor.GRAY,
		DyeColor.BLACK
	};


	public static void init() {
		// Items
		REG.register(BTXItems.HEMP_SEEDS, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.HEMP, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.GEAR, inCategory(CreativeInventoryCategory.BASICS));
		REG.register(BTXItems.FLOUR, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.HEMP_FIBERS, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.SCOURED_LEATHER, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.DUNG, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.WATERWHEEL, inCategory(CreativeInventoryCategory.PLACEABLES));
		REG.register(BTXItems.SAIL, inCategory(CreativeInventoryCategory.MISCELLANEOUS));
		REG.register(BTXItems.WINDMILL, inCategory(CreativeInventoryCategory.PLACEABLES));
		REG.register(BTXItems.FABRIC, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.WOOD_BLADE, inCategory(CreativeInventoryCategory.MISCELLANEOUS));
		REG.register(BTXItems.CONCENTRATED_HELLFIRE, inCategory(CreativeInventoryCategory.MISCELLANEOUS));
		REG.register(BTXItems.HELLFIRE_DUST, inCategory(CreativeInventoryCategory.MISCELLANEOUS));
		REG.register(BTXItems.GROUND_NETHERRACK, inCategory(CreativeInventoryCategory.MISCELLANEOUS));
		REG.register(BTXItems.FOUL_FOOD, inCategory(CreativeInventoryCategory.MISCELLANEOUS));
		REG.register(BTXItems.FOOD_WOLF_CHOP_RAW, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.FOOD_WOLF_CHOP_COOKED, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.DONUT, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.KIBBLE, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXItems.SOULFORGED_STEEL_INGOT, inCategory(CreativeInventoryCategory.ORE_PRODUCTS));
		REG.register(BTXItems.STEEL_ARMOR_PLATE, inCategory(CreativeInventoryCategory.MISCELLANEOUS));
		REG.register(BTXItems.SOULFORGED_STEEL_PICKAXE, inCategory(CreativeInventoryCategory.TOOLS));
		REG.register(BTXItems.SOULFORGED_STEEL_SHOVEL, inCategory(CreativeInventoryCategory.TOOLS));
		REG.register(BTXItems.SOULFORGED_STEEL_MATTOCK, inCategory(CreativeInventoryCategory.TOOLS));
		REG.register(BTXItems.SOULFORGED_STEEL_AXE, inCategory(CreativeInventoryCategory.TOOLS));
		REG.register(BTXItems.SOULFORGED_STEEL_HOE, inCategory(CreativeInventoryCategory.TOOLS));
		REG.register(BTXItems.SOULFORGED_STEEL_SWORD, inCategory(CreativeInventoryCategory.TOOLS));
		REG.register(BTXItems.SOULFORGED_STEEL_BATTLEAXE, inCategory(CreativeInventoryCategory.TOOLS));
		REG.register(BTXItems.SOULFORGED_STEEL_HELMET, inCategory(CreativeInventoryCategory.ARMOR));
		REG.register(BTXItems.SOULFORGED_STEEL_CHESTPLATE, inCategory(CreativeInventoryCategory.ARMOR));
		REG.register(BTXItems.SOULFORGED_STEEL_LEGGINGS, inCategory(CreativeInventoryCategory.ARMOR));
		REG.register(BTXItems.SOULFORGED_STEEL_BOOTS, inCategory(CreativeInventoryCategory.ARMOR));
		REG.register(BTXItems.COAL_DUST, inCategory(CreativeInventoryCategory.MISCELLANEOUS));
		REG.register(BTXItems.TANNED_LEATHER, inCategory(CreativeInventoryCategory.BASICS));
		REG.register(BTXItems.STRAP, inCategory(CreativeInventoryCategory.BASICS));
		REG.register(BTXItems.BELT, inCategory(CreativeInventoryCategory.BASICS));
		REG.register(BTXItems.GLUE, inCategory(CreativeInventoryCategory.BASICS));
		REG.register(BTXItems.HAFT, inCategory(CreativeInventoryCategory.BASICS));

		// Blocks
		// TODO: The blocks we add probably should be in their own category for machines/mech devices
		REG.register(BTXBlocks.MILLSTONE, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.HOPPER, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.GRATE, inCategory(CreativeInventoryCategory.WOOD));
		REG.register(BTXBlocks.SLATS, inCategory(CreativeInventoryCategory.WOOD));
		REG.register(BTXBlocks.WICKER_PANE, inCategory(CreativeInventoryCategory.WOOD));
		REG.register(BTXBlocks.WICKER_BLOCK, inCategory(CreativeInventoryCategory.WOOD));
		REG.register(BTXBlocks.CONCENTRATED_HELLFIRE_BLOCK, inCategory(CreativeInventoryCategory.STORAGE));
		REG.register(BTXBlocks.SOULFORGE, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.SAW, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.WICKER_SLAB, inCategory(CreativeInventoryCategory.WOOD));
		REG.register(BTXBlocks.COMPANION_CUBE, inCategory(CreativeInventoryCategory.MISCELLANEOUS));
		REG.register(BTXBlocks.COMPANION_SLAB, inCategory(CreativeInventoryCategory.MISCELLANEOUS));
		REG.register(BTXBlocks.HAND_CRANK, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.AXLE, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.AXLE_POWER_SOURCE, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.GEARBOX, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.SIDING_OAK, inCategory(CreativeInventoryCategory.WOOD));
		REG.register(BTXBlocks.MOULDING_OAK, inCategory(CreativeInventoryCategory.WOOD));
		REG.register(BTXBlocks.CORNER_OAK, inCategory(CreativeInventoryCategory.WOOD));
		REG.register(BTXBlocks.SIDING_OAK_PAINTED, inCategory(CreativeInventoryCategory.WOOD).setCustomSupplier(() -> allColors(BTXBlocks.SIDING_OAK_PAINTED)));
		REG.register(BTXBlocks.MOULDING_OAK_PAINTED, inCategory(CreativeInventoryCategory.WOOD).setCustomSupplier(() -> allColors(BTXBlocks.MOULDING_OAK_PAINTED)));
		REG.register(BTXBlocks.CORNER_OAK_PAINTED, inCategory(CreativeInventoryCategory.WOOD).setCustomSupplier(() -> allColors(BTXBlocks.CORNER_OAK_PAINTED)));
		REG.register(BTXBlocks.CORNER_OAK_PAINTED, inCategory(CreativeInventoryCategory.WOOD).setCustomSupplier(() -> allColors(BTXBlocks.CORNER_OAK_PAINTED)));
		REG.register(BTXBlocks.HIBACHI, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.BELLOWS, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.TURNTABLE, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.UNFIRED_CRUCIBLE, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXBlocks.UNFIRED_PLANTER, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXBlocks.UNFIRED_VASE, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXBlocks.VASE, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXBlocks.VASE_PAINTED, inCategory(CreativeInventoryCategory.ORGANIC).setCustomSupplier(() -> allColorsExcept(BTXBlocks.VASE_PAINTED, DyeColor.WHITE)));
		REG.register(BTXBlocks.CAULDRON, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.CRUCIBLE, inCategory(CreativeInventoryCategory.WORKBENCHES));
		REG.register(BTXBlocks.PLANTER, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXBlocks.PLANTER_SOIL, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXBlocks.PLANTER_GRASS, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXBlocks.PLANTER_GRASS_RETRO, inCategory(CreativeInventoryCategory.ORGANIC));
		REG.register(BTXBlocks.PLANTER_GRASS_SCORCHED, inCategory(CreativeInventoryCategory.ORGANIC));
	}

	private static List<ItemStack> allColorsExcept(Block<?> block, DyeColor excluded) {
		List<ItemStack> list = allColors(block);
		int excludedMeta = painted(block).toMetadata(excluded);
		list.removeIf(stack -> stack.getMetadata() == excludedMeta);
		return list;
	}

	private static List<ItemStack> allColors(Block<?> block) {
		List<ItemStack> list = new ArrayList<>();
		for (DyeColor color : RAINBOW_ORDER) {
			list.add(new ItemStack(block, 1, painted(block).toMetadata(color)));
		}
		return list;
	}

	@NotNull
	private static IPainted painted(@NotNull Block<?> block) {
		return (IPainted)block.getLogic();
	}

	private static CreativeInventoryPlacement.After after(IItemConvertible item) {
		return new CreativeInventoryPlacement.After(() -> item);
	}
	private static CreativeInventoryPlacement.Category inCategory(CreativeInventoryCategory category) {
		return new CreativeInventoryPlacement.Category(category);
	}
}
