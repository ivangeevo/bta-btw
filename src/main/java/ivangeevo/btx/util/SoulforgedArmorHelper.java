package ivangeevo.btx.util;

import ivangeevo.btx.item.items.ItemArmorSoulforged;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.IArmorItem;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.DamageType;

public class SoulforgedArmorHelper {
	public static final float KNOCKBACK_REDUCTION_PER_PIECE = 0.2F;
	public static final float SPEED_REDUCTION_PER_PIECE = 0.05F;
	public static final float SWIM_REDUCTION_PER_PIECE = 0.25F;
	public static final double SINK_PER_PIECE = 0.005D;
	public static final float FALL_DAMAGE_INCREASE_PER_PIECE = 0.15F;
	private static final int FULL_SET = 4;

	public static int piecesWorn(Player player) {
		int count = 0;

		for (ItemStack stack : player.inventory.armorInventory) {
			if (ItemArmorSoulforged.isSoulforged(stack)) {
				count++;
			}
		}

		return count;
	}

	public static int intactPieces(Player player) {
		int count = 0;

		for (ItemStack stack : player.inventory.armorInventory) {
			if (ItemArmorSoulforged.isSoulforged(stack) && !ItemArmorSoulforged.isBroken(stack)) {
				count++;
			}
		}

		return count;
	}

	public static boolean hasFullIntactSet(Player player) {
		return intactPieces(player) == FULL_SET;
	}

	public static float brokenProtection(Player player, DamageType damageType) {
		float protection = 0.0F;

		for (ItemStack stack : player.inventory.armorInventory) {
			if (ItemArmorSoulforged.isBroken(stack) && stack.getItem() instanceof IArmorItem<?> armor) {
				protection += armor.getArmorMaterial().getProtection(damageType) * armor.getArmorPieceProtectionPercentage();
			}
		}

		return protection;
	}
}
