package ivangeevo.btx.util.utils;

import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class InventoryUtils {

	public static final int IGNORE_METADATA = 32767;


	public static void clearInventoryContents(Container inventory) {
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack itemStack = inventory.getItem(slot);

			if (itemStack != null) {
				inventory.setItem(slot, null);
			}
		}
	}

	/** This function puts everything in the inventory into the world (ripped from BlockChest in BTW). */
	public static void ejectInventoryContents(World world, TilePosc tilePos, @Nullable Container inventory) {
		if (inventory == null) {
			return;
		}

		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack itemStack = inventory.getItem(slot);

			if (itemStack == null) {
				continue;
			}

			float xOffset = world.rand.nextFloat() * 0.7F + 0.15F;
			float yOffset = world.rand.nextFloat() * 0.7F + 0.15F;
			float zOffset = world.rand.nextFloat() * 0.7F + 0.15F;

			int remaining = itemStack.stackSize;

			while (remaining > 0) {
				int count = Math.min(world.rand.nextInt(21) + 10, remaining);

				remaining -= count;

				ItemStack dropStack = itemStack.copy();
				dropStack.stackSize = count;

				EntityItem entityItem = new EntityItem(world,
					tilePos.x() + xOffset, tilePos.y() + yOffset, tilePos.z() + zOffset, dropStack);

				float spread = 0.05F;

				entityItem.xd = (float) world.rand.nextGaussian() * spread;
				entityItem.yd = (float) world.rand.nextGaussian() * spread + 0.2F;
				entityItem.zd = (float) world.rand.nextGaussian() * spread;
				entityItem.pickupDelay = 10;

				world.entityJoinedWorld(entityItem);
			}
		}
	}

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

	/** BTW: Container.calcRedstoneFromInventory, the 0-15 comparator level for how full an inventory is. */
	public static int calcRedstoneFromInventory(@Nullable Container inventory) {
		if (inventory == null) {
			return 0;
		}

		int occupied = 0;
		float fullness = 0.0F;

		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);

			if (stack != null) {
				fullness += (float) stack.stackSize / (float) Math.min(inventory.getMaxStackSize(), stack.getMaxStackSize());
				occupied++;
			}
		}

		fullness /= (float) inventory.getContainerSize();

		return MathHelper.floor(fullness * 14.0F) + (occupied > 0 ? 1 : 0);
	}

	public static int getNumOccupiedStacks(Container inventory) {
		return getNumOccupiedStacksInRange(inventory, 0, inventory.getContainerSize() - 1);
	}

	public static int getNumOccupiedStacksInRange(Container inventory, int minSlot, int maxSlot) {
		int count = 0;

		for (int tempSlot = minSlot; tempSlot <= maxSlot; tempSlot++) {
			if (inventory.getItem(tempSlot) != null) {
				count++;
			}
		}

		return count;
	}

	/** BTW CE: getRandomOccupiedStackInRange. Returns -1 if every slot in the range is empty. */
	public static int getRandomOccupiedStackInRange(Container inventory, Random rand, int minSlot, int maxSlot) {
		int occupied = getNumOccupiedStacksInRange(inventory, minSlot, maxSlot);

		if (occupied <= 0) {
			return -1;
		}

		int target = rand.nextInt(occupied);

		for (int tempSlot = minSlot; tempSlot <= maxSlot; tempSlot++) {
			if (inventory.getItem(tempSlot) != null) {
				if (target == 0) {
					return tempSlot;
				}

				target--;
			}
		}

		return -1;
	}

	/**
	 * BTW CE: getMaxNumberOfItemsForTransferInRange. How many of stack's item (up to maxCount) fit into the slot
	 * range, counting both room on matching stacks and empty slots.
	 */
	public static int getMaxNumberOfItemsForTransferInRange(Container inventory, ItemStack stack, int maxCount, int minSlot, int maxSlot) {
		int stackLimit = Math.min(stack.getMaxStackSize(), inventory.getMaxStackSize());
		int room = 0;

		for (int tempSlot = minSlot; tempSlot <= maxSlot && room < maxCount; tempSlot++) {
			ItemStack slotStack = inventory.getItem(tempSlot);

			if (slotStack == null) {
				room += stackLimit;
			} else if (slotStack.canStackWith(stack) && slotStack.isStackable()) {
				room += Math.max(0, stackLimit - slotStack.stackSize);
			}
		}

		return Math.min(room, maxCount);
	}

	public static int getFirstOccupiedStackOfItem(Container inventory, int itemId) {
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);

			if (stack != null && stack.itemID == itemId) {
				return i;
			}
		}

		return -1;
	}

	public static int getFirstOccupiedStackNotOfItem(Container inventory, int notItemId) {
		for (int tempSlot = 0; tempSlot < inventory.getContainerSize(); tempSlot++) {
			ItemStack stack = inventory.getItem(tempSlot);

			if (stack != null && stack.itemID != notItemId) {
				return tempSlot;
			}
		}

		return -1;
	}

	public static int getFirstEmptyStack(Container inventory) {
		return getFirstEmptyStackInSlotRange(inventory, 0, inventory.getContainerSize() - 1);
	}

	public static int getFirstEmptyStackInSlotRange(Container inventory, int minSlotIndex, int maxSlotIndex) {
		for (int tempSlot = minSlotIndex; tempSlot <= maxSlotIndex; tempSlot++) {
			if (inventory.getItem(tempSlot) == null) {
				return tempSlot;
			}
		}

		return -1;
	}

	public static int countItemsInInventory(Container inventory, int itemId, int itemDamage) {
		return countItemsInInventory(inventory, itemId, itemDamage, false);
	}

	public static int countItemsInInventory(Container inventory, int itemId, int itemDamage, boolean metadataExclusive) {
		int itemCount = 0;

		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack tempStack = inventory.getItem(i);

			if (tempStack != null && tempStack.itemID == itemId) {
				if (itemDamage == IGNORE_METADATA
					|| (!metadataExclusive && tempStack.getMetadata() == itemDamage)
					|| (metadataExclusive && tempStack.getMetadata() != itemDamage)) {
					itemCount += tempStack.stackSize;
				}
			}
		}

		return itemCount;
	}

	/** Consumes up to amountToConsume items matching itemId/itemDamage from anywhere in the inventory. */
	public static boolean consumeItemsInInventory(Container inventory, int itemId, int itemDamage, int amountToConsume) {
		int remaining = amountToConsume;

		for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
			ItemStack stack = inventory.getItem(slot);

			if (stack != null && stack.itemID == itemId
				&& (itemDamage == IGNORE_METADATA || stack.getMetadata() == itemDamage)) {
				int amountToRemove = Math.min(remaining, stack.stackSize);

				decrementStackSize(inventory, slot, amountToRemove);

				remaining -= amountToRemove;
			}
		}

		return remaining <= 0;
	}

	public static boolean addItemStackToInventory(Container inventory, ItemStack stack) {
		return addItemStackToInventoryInSlotRange(inventory, stack, 0, inventory.getContainerSize() - 1);
	}

	/** returns true if the full stack has been stored */
	public static boolean addItemStackToInventoryInSlotRange(Container inventory, ItemStack stack, int minSlotIndex, int maxSlotIndex) {
		if (!stack.isItemDamaged()) {
			if (attemptToMergeWithExistingStacksInSlotRange(inventory, stack, minSlotIndex, maxSlotIndex)) {
				return true;
			}
		}

		return attemptToPlaceInEmptySlotInSlotRange(inventory, stack, minSlotIndex, maxSlotIndex);
	}

	/** returns true if the full stack has been stored */
	private static boolean attemptToPlaceInEmptySlotInSlotRange(Container inventory, ItemStack stack, int minSlotIndex, int maxSlotIndex) {
		int emptySlot = getFirstEmptyStackInSlotRange(inventory, minSlotIndex, maxSlotIndex);

		while (emptySlot >= 0) {
			int numItemsToStore = Math.min(stack.stackSize, inventory.getMaxStackSize());

			ItemStack newStack = stack.copy();
			newStack.stackSize = numItemsToStore;

			inventory.setItem(emptySlot, newStack);

			stack.stackSize -= numItemsToStore;

			if (stack.stackSize <= 0) {
				return true;
			}

			emptySlot = getFirstEmptyStackInSlotRange(inventory, minSlotIndex, maxSlotIndex);
		}

		return false;
	}

	/** returns true if the full stack has been stored */
	private static boolean attemptToMergeWithExistingStacksInSlotRange(Container inventory, ItemStack stack, int minSlotIndex, int maxSlotIndex) {
		int mergeSlot = findSlotToMergeItemStackInSlotRange(inventory, stack, minSlotIndex, maxSlotIndex);

		while (mergeSlot >= 0) {
			int numItemsToStore = stack.stackSize;

			ItemStack tempStack = inventory.getItem(mergeSlot);

			if (numItemsToStore > tempStack.getMaxStackSize() - tempStack.stackSize) {
				numItemsToStore = tempStack.getMaxStackSize() - tempStack.stackSize;
			}

			if (numItemsToStore > inventory.getMaxStackSize() - tempStack.stackSize) {
				numItemsToStore = inventory.getMaxStackSize() - tempStack.stackSize;
			}

			if (numItemsToStore == 0) {
				return false;
			}

			stack.stackSize -= numItemsToStore;

			tempStack.stackSize += numItemsToStore;
			inventory.setItem(mergeSlot, tempStack);

			if (stack.stackSize <= 0) {
				return true;
			}

			mergeSlot = findSlotToMergeItemStackInSlotRange(inventory, stack, minSlotIndex, maxSlotIndex);
		}

		return false;
	}


	private static boolean canStacksMerge(ItemStack sourceStack, @Nullable ItemStack destStack, int inventoryStackSizeLimit) {
		return destStack != null
			&& destStack.isStackable()
			&& destStack.stackSize < destStack.getMaxStackSize()
			&& destStack.stackSize < inventoryStackSizeLimit
			&& destStack.canStackWith(sourceStack); // same item, data and custom tags
	}

	private static int findSlotToMergeItemStackInSlotRange(Container inventory, ItemStack itemStack, int minSlotIndex, int maxSlotIndex) {
		int inventoryStackLimit = inventory.getMaxStackSize();

		for (int i = minSlotIndex; i <= maxSlotIndex; i++) {
			if (canStacksMerge(itemStack, inventory.getItem(i), inventoryStackLimit)) {
				return i;
			}
		}

		return -1;
	}
}

