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

		// Blocks
		// TODO: The blocks we add probably should be in their own category for machines/mech devices
		REG.register(BTXBlocks.MILLSTONE, inCategory(CreativeInventoryCategory.WORKBENCHES));
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
