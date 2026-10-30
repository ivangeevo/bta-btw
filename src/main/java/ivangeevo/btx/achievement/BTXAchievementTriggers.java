package ivangeevo.btx.achievement;

import ivangeevo.btx.util.SoulforgedArmorHelper;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.IItemConvertible;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class BTXAchievementTriggers {
	public static final double NEARBY_RADIUS = 12.0;
	public static final double WIDE_RADIUS = 24.0;
	private static final int SCAN_INTERVAL = 20;

	private static final Map<Item, Achievement> OBTAIN = new HashMap<>();
	private static final Map<Item, Achievement> COOKED = new HashMap<>();
	private static final Map<Player, Set<Achievement>> AWARDED = new WeakHashMap<>();

	private BTXAchievementTriggers() {}

	static void obtain(Achievement achievement, IItemConvertible... items) {
		for (IItemConvertible item : items) {
			OBTAIN.put(item.asItem(), achievement);
		}
	}

	static void cooked(Achievement achievement, IItemConvertible... items) {
		for (IItemConvertible item : items) {
			COOKED.put(item.asItem(), achievement);
		}
	}

	public static void onCookedTaken(Player player, ItemStack stack) {
		Achievement achievement = COOKED.get(stack.getItem());
		if (achievement != null) {
			award(player, achievement);
		}
	}

	public static void award(@Nullable Player player, Achievement achievement) {
		if (player == null || player.world.isClientSide) {
			return;
		}

		Set<Achievement> awarded = AWARDED.computeIfAbsent(player, p -> new HashSet<>());
		if (awarded.contains(achievement)) {
			return;
		}

		player.triggerAchievement(achievement);
		// the trigger is dropped while the parent is locked, so only remember it once it could have stuck
		if (achievement.parent == null || awarded.contains(achievement.parent) || player.getStat(achievement.parent) > 0) {
			awarded.add(achievement);
		}
	}

	public static void awardNearby(World world, TilePosc pos, Achievement achievement) {
		awardNearby(world, pos, NEARBY_RADIUS, achievement);
	}

	public static void awardNearby(World world, TilePosc pos, double radius, Achievement achievement) {
		awardNearby(world, pos.x() + 0.5, pos.y() + 0.5, pos.z() + 0.5, radius, achievement);
	}

	public static void awardNearby(World world, double x, double y, double z, double radius, Achievement achievement) {
		if (world == null || world.isClientSide) {
			return;
		}

		for (Player player : world.players) {
			if (player.distanceToSqr(x, y, z) <= radius * radius) {
				award(player, achievement);
			}
		}
	}

	public static void tickPlayer(Player player) {
		if (player.world.isClientSide || player.tickCount % SCAN_INTERVAL != 0) {
			return;
		}

		scan(player, player.inventory.mainInventory);
		scan(player, player.inventory.armorInventory);
		checkObtained(player, player.inventory.getHeldItemStack());

		if (SoulforgedArmorHelper.piecesWorn(player) > 0) {
			award(player, BTXAchievements.SF_ARMOR);

			if (SoulforgedArmorHelper.hasFullIntactSet(player)) {
				award(player, BTXAchievements.SF_FULL_SET);
			}
		}
	}

	private static void scan(Player player, ItemStack[] stacks) {
		for (ItemStack stack : stacks) {
			checkObtained(player, stack);
		}
	}

	private static void checkObtained(Player player, @Nullable ItemStack stack) {
		if (stack == null || stack.stackSize <= 0) {
			return;
		}

		Achievement achievement = OBTAIN.get(stack.getItem());
		if (achievement != null) {
			award(player, achievement);
		}
	}
}
