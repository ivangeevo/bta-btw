package ivangeevo.btx.gui.blockdispenser;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import ivangeevo.btx.block.entity.TileEntityBlockDispenser;
import net.minecraft.core.InventoryAction;
import net.minecraft.core.crafting.ContainerListener;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.player.inventory.slot.Slot;
import org.jetbrains.annotations.NotNull;

public class MenuBlockDispenser extends MenuAbstract {
	public static final int SLOT_COLUMNS = 4;
	private static final int DISPENSER_SLOTS = TileEntityBlockDispenser.INVENTORY_SIZE;
	private static final int INV_START = DISPENSER_SLOTS;
	private static final int INV_SIZE = 27;
	private static final int HOTBAR_START = INV_START + INV_SIZE;
	private static final int HOTBAR_SIZE = 9;
	private static final int PLAYER_SLOTS = INV_SIZE + HOTBAR_SIZE;

	private final TileEntityBlockDispenser dispenser;

	private int lastNextSlotIndex = -1;

	public MenuBlockDispenser(@NotNull ContainerInventory inventory, TileEntityBlockDispenser dispenser) {
		this.dispenser = dispenser;

		for (int row = 0; row < SLOT_COLUMNS; row++) {
			for (int column = 0; column < SLOT_COLUMNS; column++) {
				this.addSlot(new Slot(dispenser, column + row * SLOT_COLUMNS, 53 + column * 18, 17 + row * 18));
			}
		}

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 102 + row * 18));
			}
		}

		for (int column = 0; column < 9; column++) {
			this.addSlot(new Slot(inventory, column, 8 + column * 18, 160));
		}
	}

	@Override
	public IntList getMoveSlots(@NotNull InventoryAction inventoryAction, @NotNull Slot slot, int i, Player player) {
		if (slot.index >= 0 && slot.index < DISPENSER_SLOTS) {
			return inventoryAction == InventoryAction.MOVE_ALL
				? this.getSlots(0, DISPENSER_SLOTS, false)
				: this.getSlots(slot.index, 1, false);
		}

		if (inventoryAction == InventoryAction.MOVE_ALL) {
			if (slot.index >= INV_START && slot.index < HOTBAR_START) {
				return this.getSlots(INV_START, INV_SIZE, false);
			}

			if (slot.index >= HOTBAR_START && slot.index < HOTBAR_START + HOTBAR_SIZE) {
				return this.getSlots(HOTBAR_START, HOTBAR_SIZE, false);
			}
		}

		return slot.index >= INV_START && slot.index < INV_START + PLAYER_SLOTS
			? this.getSlots(INV_START, PLAYER_SLOTS, false)
			: null;
	}

	@Override
	public IntList getTargetSlots(@NotNull InventoryAction inventoryAction, @NotNull Slot slot, int i, Player player) {
		if (slot.index >= INV_START && slot.index < INV_START + PLAYER_SLOTS) {
			return this.getSlots(0, DISPENSER_SLOTS, false);
		}

		if (slot.index >= 0 && slot.index < DISPENSER_SLOTS) {
			return this.getSlots(INV_START, PLAYER_SLOTS, false);
		}

		return IntLists.EMPTY_LIST;
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		int nextSlot = this.dispenser.getNextSlotIndexToDispense();

		if (this.lastNextSlotIndex != nextSlot) {
			for (ContainerListener listener : this.containerListeners) {
				listener.updateCraftingInventoryInfo(this, 0, nextSlot);
			}

			this.lastNextSlotIndex = nextSlot;
		}
	}

	@Override
	public void setData(int id, int value) {
		if (id == 0) {
			this.dispenser.setNextSlotIndexToDispense(value);
		}
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return this.dispenser.stillValid(player);
	}
}