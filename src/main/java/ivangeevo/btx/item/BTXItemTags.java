package ivangeevo.btx.item;

import net.minecraft.core.data.tag.Tag;
import net.minecraft.core.item.IItemConvertible;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.Items;
import org.jetbrains.annotations.NotNull;

public final class BTXItemTags {
	public static final Tag<Item> LONG_BLOW_DISTANCE = Tag.of("btx:long_blow_distance");
	public static final Tag<Item> MEDIUM_BLOW_DISTANCE = Tag.of("btx:medium_blow_distance");
	public static final Tag<Item> SHORT_BLOW_DISTANCE = Tag.of("btx:short_blow_distance");

	static void init() {
		tag(LONG_BLOW_DISTANCE,
			Items.FEATHER_CHICKEN, Items.PAPER, Items.MAP,
			Items.GUNPOWDER, Items.DUST_SUGAR, Items.DUST_REDSTONE, Items.DUST_GLOWSTONE,
			BTXItems.FLOUR, BTXItems.GROUND_NETHERRACK, BTXItems.HELLFIRE_DUST, BTXItems.COAL_DUST);
		tag(MEDIUM_BLOW_DISTANCE,
			Items.STRING, BTXItems.HEMP_FIBERS,
			Items.SEEDS_WHEAT, Items.SEEDS_PUMPKIN, BTXItems.HEMP_SEEDS,
			BTXItems.HEMP);
		tag(SHORT_BLOW_DISTANCE,
			Items.AMMO_ARROW, Items.AMMO_ARROW_GOLD, Items.AMMO_ARROW_PURPLE, Items.AMMO_ARROW_FLAMING, BTXItems.BROADHEAD_ARROW,
			Items.WHEAT, BTXItems.FABRIC, BTXItems.STRAP, BTXItems.WICKER);
	}

	private static void tag(@NotNull Tag<Item> tag, IItemConvertible @NotNull ... items) {
		for (IItemConvertible item : items) {
			tag.tag(item.asItem());
		}
	}

	public static int getBlowDistance(@NotNull Item item) {
		if (item.hasTag(LONG_BLOW_DISTANCE)) return 3;
		if (item.hasTag(MEDIUM_BLOW_DISTANCE)) return 2;
		if (item.hasTag(SHORT_BLOW_DISTANCE)) return 1;
		return 0;
	}
}