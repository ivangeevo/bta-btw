package ivangeevo.btx.item.items;

import net.minecraft.core.enums.HumanArmorShape;
import net.minecraft.core.item.ItemArmor;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.material.ArmorMaterial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemArmorSoulforged extends ItemArmor<HumanArmorShape> {
	public ItemArmorSoulforged(@NotNull String name, @NotNull String namespaceId, int id, ArmorMaterial material, HumanArmorShape shape) {
		super(name, namespaceId, id, material, shape);
	}

	public static boolean isSoulforged(@Nullable ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemArmorSoulforged;
	}

	public static boolean isBroken(@Nullable ItemStack stack) {
		return isSoulforged(stack) && stack.getMetadata() >= stack.getMaxDamage();
	}
}