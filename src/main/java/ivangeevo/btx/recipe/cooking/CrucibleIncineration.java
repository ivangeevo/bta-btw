package ivangeevo.btx.recipe.cooking;

import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.mixin.BlockLogicFireAccessor;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemFood;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Which items a stoked crucible burns away.
 * <p>Other mods can call register(...) for their own items, or to override a default. Defaults never replace an
 * entry another mod registered or unregistered, so the order mods run in doesn't matter.
 */
public final class CrucibleIncineration {
	private static final Map<Item, Boolean> ITEM_RULES = new HashMap<>();
	private static final Set<Item> UNREGISTERED = new HashSet<>();

	private CrucibleIncineration() {}

	public static void register(Item item) {
		register(item, true);
	}

	/** false overrides the block fallback below, like BTW's chest and note block. */
	public static void register(Item item, boolean incinerated) {
		UNREGISTERED.remove(item);
		ITEM_RULES.put(item, incinerated);
	}

	/** Drops the entry, so the item goes back to the block fallback below (non-block items then don't burn). */
	public static void unregister(Item item) {
		ITEM_RULES.remove(item);
		UNREGISTERED.add(item);
	}

	public static boolean isIncinerated(@Nullable ItemStack stack) {
		if (stack == null) return false;

		Item item = stack.getItem();
		Boolean rule = ITEM_RULES.get(item);

		if (rule != null) return rule;

		Block<?> block = item.id < Blocks.blocksList.length ? Blocks.blocksList[item.id] : null;

		// IntelliJ reads the accessor's placeholder body (it always throws) and calls this always false;
		// Mixin replaces that body at runtime, so the check is real
		//noinspection ConstantValue
		return block != null && BlockLogicFireAccessor.btx$getBurnChance()[block.id()] > 0;
	}

	//------------- Defaults -------------//

	public static void registerDefaults() {
		registerDefault(BTXItems.HEMP);
		registerDefault(BTXItems.HEMP_SEEDS);
		registerDefault(BTXItems.GEAR);
		registerDefault(BTXItems.FLOUR);
		registerDefault(BTXItems.HEMP_FIBERS);
		registerDefault(BTXItems.SCOURED_LEATHER);
		registerDefault(BTXItems.TANNED_LEATHER);
		registerDefault(BTXItems.FABRIC);
		registerDefault(BTXItems.BELT);
		registerDefault(BTXItems.DUNG);
		registerDefault(BTXItems.WATERWHEEL);
		registerDefault(BTXItems.WOOD_BLADE);
		registerDefault(BTXItems.SAIL);
		registerDefault(BTXItems.WINDMILL);

		registerDefault(Items.STRING);
		registerDefault(Items.ROPE);
		registerDefault(Items.STICK);
		registerDefault(Items.TOOL_HOE_WOOD);

		registerDefault(Items.COAL);
		registerDefault(Items.BOWL);
		registerDefault(Items.FEATHER_CHICKEN);
		registerDefault(Items.PAINTING);
		registerDefault(Items.SADDLE);
		registerDefault(Items.LEATHER);
		registerDefault(Items.SUGARCANE);
		registerDefault(Items.PAPER);
		registerDefault(Items.DUST_SUGAR);
		registerDefault(Items.FOOD_CAKE);
		registerDefault(Items.BED);
		registerDefault(Items.BOOK);
		registerDefault(Items.BOAT);
		registerDefault(Items.TOOL_BOW);
		registerDefault(Items.SIGN);
		registerDefault(Items.AMMO_SNOWBALL);

		registerDefault(Items.SIGN_PAINTED);
		registerDefault(Items.CLOTH);
		registerDefault(Items.FLAG);
		registerDefault(Items.LABEL);
		registerDefault(Items.TOOL_CALENDAR);
		registerDefault(Items.ARMOR_QUIVER);
		registerDefault(Items.ARMOR_WOLF_LEATHER);
		registerDefault(Items.SEAT);
		registerDefault(Items.PAINTBRUSH);
		registerDefault(Items.BASKET);

		registerDefault(Items.FOOD_APPLE_GOLD, false);

		for (Item item : Item.itemsList) {
			if (item instanceof ItemFood) {
				registerDefault(item);
			}
		}
	}

	private static void registerDefault(Item item) {
		registerDefault(item, true);
	}

	private static void registerDefault(Item item, boolean incinerated) {
		if (item != null && !UNREGISTERED.contains(item)) {
			ITEM_RULES.putIfAbsent(item, incinerated);
		}
	}
}
