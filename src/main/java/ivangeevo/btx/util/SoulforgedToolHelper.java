package ivangeevo.btx.util;

import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.item.BTXMaterials;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.tool.ItemTool;
import org.jetbrains.annotations.Nullable;

public class SoulforgedToolHelper {
	public static boolean isSoulforgedTool(@Nullable Item item) {
		return item instanceof ItemTool tool && tool.getMaterial() == BTXMaterials.SOULFORGED_STEEL_TOOL
			|| item != null && item == BTXItems.SOULFORGED_STEEL_SWORD;
	}

	public static boolean isBrokenTool(@Nullable ItemStack stack) {
		return stack != null && isSoulforgedTool(stack.getItem()) && stack.getMetadata() >= stack.getMaxDamage();
	}
}