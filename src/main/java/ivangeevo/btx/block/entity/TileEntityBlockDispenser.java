package ivangeevo.btx.block.entity;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import ivangeevo.btx.util.utils.InventoryUtils;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityBlockDispenser extends TileEntity implements Container {
	public static final int INVENTORY_SIZE = 16;
	private static final int STACK_SIZE_LIMIT = 64;
	private static final double MAX_PLAYER_INTERACTION_DIST = 64.0;

	private ItemStack[] contents = new ItemStack[INVENTORY_SIZE];
	private int nextSlotIndexToDispense = 0;

	@Override
	public @NotNull String getNameTranslationKey() {
		return "container.btx.block_dispenser.name";
	}

	@Override
	public int getContainerSize() {
		return INVENTORY_SIZE;
	}

	@Override
	public @Nullable ItemStack getItem(int slot) {
		return contents[slot];
	}

	@Override
	public @Nullable ItemStack removeItem(int slot, int amount) {
		return InventoryUtils.decrementStackSize(this, slot, amount);
	}

	@Override
	public void setItem(int slot, @Nullable ItemStack itemStack) {
		contents[slot] = itemStack;

		if (itemStack != null && itemStack.stackSize > getMaxStackSize()) {
			itemStack.stackSize = getMaxStackSize();
		}

		setChanged();
	}

	@Override
	public int getMaxStackSize() {
		return STACK_SIZE_LIMIT;
	}

	@Override
	public void sort() {}

	@Override
	public boolean stillValid(@NotNull Player player) {
		if (worldObj == null || worldObj.getTileEntity(tilePos) != this) {
			return false;
		}

		return player.distanceToSqr(tilePos.x + 0.5, tilePos.y + 0.5, tilePos.z + 0.5) <= MAX_PLAYER_INTERACTION_DIST;
	}

	@Override
	public void readAdditionalData(@NotNull CompoundTag compoundTag) {
		ListTag nbtTagList = compoundTag.getList("Items");

		contents = new ItemStack[getContainerSize()];

		for (int i = 0; i < nbtTagList.getValue().size(); i++) {
			CompoundTag slotTag = (CompoundTag) nbtTagList.tagAt(i);

			int slot = slotTag.getByte("Slot") & 0xff;

			if (slot < contents.length) {
				contents[slot] = ItemStack.readItemStackFromNbt(slotTag);
			}
		}

		if (compoundTag.containsKey("NextSlot")) {
			nextSlotIndexToDispense = compoundTag.getInteger("NextSlot");
		}
	}

	@Override
	public void writeAdditionalData(@NotNull CompoundTag compoundTag) {
		ListTag nbtTagList = new ListTag();

		for (int i = 0; i < contents.length; i++) {
			if (contents[i] != null) {
				CompoundTag slotTag = new CompoundTag();
				slotTag.putByte("Slot", (byte) i);

				contents[i].writeToNBT(slotTag);

				nbtTagList.addTag(slotTag);
			}
		}

		compoundTag.put("Items", nbtTagList);
		compoundTag.putInt("NextSlot", nextSlotIndexToDispense);
	}

	@Override
	public void dropContents(World world, int x, int y, int z) {
		super.dropContents(world, x, y, z);

		InventoryUtils.ejectInventoryContents(world, new TilePos(x, y, z), this);
		InventoryUtils.clearInventoryContents(this);
	}

	public int getNextSlotIndexToDispense() {
		return nextSlotIndexToDispense;
	}

	public void setNextSlotIndexToDispense(int slot) {
		nextSlotIndexToDispense = slot;
	}

	public @Nullable ItemStack getCurrentItemToDispense() {
		if (nextSlotIndexToDispense >= contents.length || contents[nextSlotIndexToDispense] == null) {
			int slot = findNextValidSlotIndex(nextSlotIndexToDispense);

			if (slot < 0) {
				return null;
			}

			nextSlotIndexToDispense = slot;
		}

		ItemStack stack = contents[nextSlotIndexToDispense].copy();
		stack.stackSize = 1;

		return stack;
	}

	public void onDispenseCurrentSlot() {
		removeItem(nextSlotIndexToDispense, 1);

		int slot = findNextValidSlotIndex(nextSlotIndexToDispense);
		nextSlotIndexToDispense = Math.max(slot, 0);
	}

	public boolean addStack(ItemStack stack) {
		int initialSize = stack.stackSize;
		boolean wholeStackAdded = InventoryUtils.addItemStackToInventory(this, stack);

		// a partial fit still counts as swallowed, the excess is lost
		return wholeStackAdded || stack.stackSize < initialSize;
	}

	private int findNextValidSlotIndex(int currentSlot) {
		for (int slot = currentSlot + 1; slot < contents.length; slot++) {
			if (contents[slot] != null) {
				return slot;
			}
		}

		for (int slot = 0; slot < currentSlot && slot < contents.length; slot++) {
			if (contents[slot] != null) {
				return slot;
			}
		}

		return currentSlot < contents.length && contents[currentSlot] != null ? currentSlot : -1;
	}
}
