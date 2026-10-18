package ivangeevo.btx.item;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public final class HoeDrops {
	public record Context(World world, TilePosc pos, Block<?> block, ItemStack hoe, @Nullable Player player) {}

	public interface Rule {
		/** Returns the stack to drop, or null for nothing. */
		@Nullable ItemStack roll(Context ctx);
	}

	private static final Map<Block<?>, List<Rule>> TILL = new HashMap<>();
	private static final Map<Block<?>, List<Rule>> BREAK = new HashMap<>();
	private static final Set<Block<?>> REPLACE_VANILLA_SEEDS = new HashSet<>();

	private HoeDrops() {}

	public static void onTill(Block<?> block, Rule rule) {
		TILL.computeIfAbsent(block, b -> new ArrayList<>()).add(rule);
	}
	public static void onBreak(Block<?> block, Rule rule) {
		BREAK.computeIfAbsent(block, b -> new ArrayList<>()).add(rule);
	}

	/** Suppress the vanilla wheat seed roll for this block (BTW replaces it with hemp). */
	public static void replaceVanillaSeeds(Block<?> block) {
		REPLACE_VANILLA_SEEDS.add(block);
	}
	public static boolean replacesVanillaSeeds(Block<?> block) {
		return REPLACE_VANILLA_SEEDS.contains(block);
	}

	public static void rollTill(Context ctx, TilePosc dropPos) {
		roll(TILL, ctx, dropPos);
	}
	public static void rollBreak(Context ctx, TilePosc dropPos) {
		roll(BREAK, ctx, dropPos);
	}

	private static void roll(Map<Block<?>, List<Rule>> map, Context ctx, TilePosc dropPos) {
		List<Rule> rules = map.get(ctx.block());
		if (rules == null) return;
		for (Rule r : rules) {
			ItemStack out = r.roll(ctx);
			if (out != null) ctx.world().dropItem(dropPos, out);
		}
	}

	/** Percent chance by hoe tier, like BTW's wood 1 / stone 2 / iron 4 / diamond 8 / gold 16. */
	public static Rule chanceByHoe(Supplier<ItemStack> stack, Map<Item, Integer> percentByHoe) {
		return ctx -> {
			int percent = percentByHoe.getOrDefault(ctx.hoe().getItem(), 0);
			return ctx.world().rand.nextInt(100) < percent ? stack.get() : null;
		};
	}

	public static void registerDefaults() {
		Map<Item, Integer> hempChance = Map.of(
			Items.TOOL_HOE_WOOD, 1,
			Items.TOOL_HOE_STONE, 2,
			Items.TOOL_HOE_IRON, 4,
			Items.TOOL_HOE_DIAMOND, 8,
			Items.TOOL_HOE_STEEL, 10,
			Items.TOOL_HOE_GOLD, 16
		);
		Rule hemp = chanceByHoe(() -> new ItemStack(BTXItems.HEMP_SEEDS), hempChance);

		for (Block<?> grass : List.of(Blocks.GRASS, Blocks.GRASS_RETRO)) {
			onTill(grass, hemp);
			replaceVanillaSeeds(grass);
		}
	}
}
