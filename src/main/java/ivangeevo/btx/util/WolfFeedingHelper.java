package ivangeevo.btx.util;

import com.mojang.nbt.tags.CompoundTag;
import ivangeevo.btx.entity.FeedableWolf;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.sound.BTXSoundTypes;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.animal.MobWolf;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemFood;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBd;

import java.util.List;

public class WolfFeedingHelper {
	private static final int FULL_FED_COUNT = 24000;
	private static final int FED_COUNT_VARIANCE = 1200;
	private static final double LOOSE_FOOD_RANGE = 2.5D;
	private static final String FED_COUNTDOWN_KEY = "btxFedCountdown";
	private static final String LEGACY_FED_KEY = "bIsFed";

	public static boolean onInteract(MobWolf wolf, Player player) {
		if (!wolf.isWolfTamed()) {
			return false;
		}

		ItemStack stack = player.inventory.getCurrentItem();
		if (stack == null) {
			return false;
		}

		if (isWolfChop(stack)) {
			turnHostile(wolf, player);
			return true;
		}

		return isWolfFood(stack) && tryHandFeed(wolf, player, stack);
	}

	public static void updateFedState(MobWolf wolf) {
		World world = wolf.world;

		if (world.isClientSide || !wolf.isAlive()) {
			return;
		}

		FeedableWolf feedable = (FeedableWolf) wolf;

		if (feedable.btx$getFedCountdown() < 0) {
			resetFedCountdown(wolf);
		}

		if (isFed(wolf)) {
			feedable.btx$setFedCountdown(feedable.btx$getFedCountdown() - 1);
			WolfDungHelper.rollForDung(wolf);
		} else if (!eatHeldFood(wolf)) {
			eatLooseFood(wolf);
		}
	}

	public static boolean isFed(MobWolf wolf) {
		return ((FeedableWolf) wolf).btx$getFedCountdown() > 0;
	}

	public static boolean isWolfFood(ItemStack stack) {
		return stack.getItem() instanceof ItemFood food && food.getIsWolfsFavoriteMeat();
	}

	public static void writeFedState(MobWolf wolf, @NotNull CompoundTag tag) {
		tag.putInt(FED_COUNTDOWN_KEY, ((FeedableWolf) wolf).btx$getFedCountdown());
	}

	public static void readFedState(MobWolf wolf, @NotNull CompoundTag tag) {
		FeedableWolf feedable = (FeedableWolf) wolf;

		if (tag.containsKey(FED_COUNTDOWN_KEY)) {
			feedable.btx$setFedCountdown(Math.min(tag.getInteger(FED_COUNTDOWN_KEY), FULL_FED_COUNT + FED_COUNT_VARIANCE));
		} else if (tag.containsKey(LEGACY_FED_KEY) && !tag.getBoolean(LEGACY_FED_KEY)) {
			feedable.btx$setFedCountdown(0);
		} else {
			feedable.btx$setFedCountdown(-1);
		}
	}

	private static boolean isWolfChop(ItemStack stack) {
		return stack.itemID == BTXItems.FOOD_WOLF_CHOP_RAW.id || stack.itemID == BTXItems.FOOD_WOLF_CHOP_COOKED.id;
	}

	private static void turnHostile(MobWolf wolf, Player player) {
		World world = wolf.world;

		if (world.isClientSide) {
			return;
		}

		world.playSoundAtEntity(null, wolf, BTXSoundTypes.WOLF_TURN_HOSTILE, ((FeedableWolf) wolf).btx$getSoundVolume(),
			(world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);

		wolf.setWolfAngry(true);
		wolf.setWolfTamed(false);
		wolf.setWolfOwner(null);
		wolf.setWolfSitting(false);
		wolf.setPathToEntity(null);
		wolf.setTarget(player);
	}

	private static boolean tryHandFeed(MobWolf wolf, Player player, ItemStack stack) {
		boolean hurt = wolf.getHealth() < wolf.getMaxHealth();

		if (wolf.world.isClientSide) {
			return hurt;
		}

		if (!hurt && isFed(wolf)) {
			return false;
		}

		int healAmount = ((ItemFood) stack.getItem()).getHealAmount(stack);

		if (player.getGamemode().hasBlockConsumption()) {
			--stack.stackSize;
			if (stack.stackSize <= 0) {
				player.inventory.setItem(player.inventory.getCurrentSlot(), null);
			}
		}

		eat(wolf, healAmount);

		return true;
	}

	private static boolean eatHeldFood(MobWolf wolf) {
		ItemStack held = wolf.getHeldItem();

		if (held == null || held.stackSize <= 0 || !isWolfFood(held)) {
			return false;
		}

		int healAmount = ((ItemFood) held.getItem()).getHealAmount(held);

		--held.stackSize;
		if (held.stackSize <= 0) {
			wolf.setHeldItem(null);
		}

		eat(wolf, healAmount);

		return true;
	}

	private static void eatLooseFood(MobWolf wolf) {
		AABBd box = new AABBd(wolf.x - LOOSE_FOOD_RANGE, wolf.y - 1.0D, wolf.z - LOOSE_FOOD_RANGE,
			wolf.x + LOOSE_FOOD_RANGE, wolf.y + 1.0D, wolf.z + LOOSE_FOOD_RANGE);
		List<EntityItem> nearbyItems = wolf.world.getEntitiesWithinAABB(EntityItem.class, box);

		for (EntityItem entityItem : nearbyItems) {
			ItemStack stack = entityItem.item;

			if (entityItem.pickupDelay == 0 && stack != null && stack.stackSize > 0 && isWolfFood(stack)) {
				int healAmount = ((ItemFood) stack.getItem()).getHealAmount(stack);

				--stack.stackSize;

				if (stack.stackSize <= 0) {
					entityItem.remove();
				}

				eat(wolf, healAmount);

				return;
			}
		}
	}

	private static void eat(MobWolf wolf, int healAmount) {
		World world = wolf.world;

		wolf.heal(healAmount);
		resetFedCountdown(wolf);

		world.playSoundAtEntity(null, wolf, BTXSoundTypes.WOLF_EAT, 0.25F,
			((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
	}

	private static void resetFedCountdown(MobWolf wolf) {
		((FeedableWolf) wolf).btx$setFedCountdown(FULL_FED_COUNT + wolf.world.rand.nextInt(FED_COUNT_VARIANCE * 2) - FED_COUNT_VARIANCE);
	}
}
