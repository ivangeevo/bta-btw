package ivangeevo.btx.util;

import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.Container;

public class InventoryUtils {

	public static ItemStack decrementStackSize(Container container, int slot, int amount) {
		ItemStack itemStack = container.getItem(slot);

		if (itemStack == null) return null;

		if (itemStack.stackSize <= amount) {
			ItemStack newStack = container.getItem(slot);
			container.setItem(slot, null);
			return newStack;
		}

		ItemStack splitStack = itemStack.splitStack(amount);
		if (itemStack.stackSize == 0) {
			container.setItem(slot, null);
		} else {
			container.setChanged();
		}

		return splitStack;
	}
}

