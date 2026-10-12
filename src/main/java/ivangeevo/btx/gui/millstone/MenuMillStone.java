package ivangeevo.btx.gui.millstone;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import ivangeevo.btx.block.entity.TileEntityMillStone;
import net.minecraft.core.InventoryAction;
import net.minecraft.core.crafting.ContainerListener;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.player.inventory.slot.Slot;
import org.jetbrains.annotations.NotNull;

public class MenuMillStone extends MenuAbstract {
	private final TileEntityMillStone millStone;

	private final static int SLOT_ROWS = 3;
	private final static int SLOT_COLUMNS = 1;

	private static final int MILL_SLOTS = SLOT_ROWS * SLOT_COLUMNS;
	private static final int INV_START = MILL_SLOTS;
	private static final int INV_SIZE = 27;
	private static final int HOTBAR_START = INV_START + INV_SIZE;
	private static final int HOTBAR_SIZE = 9;
	private static final int PLAYER_SLOTS = INV_SIZE + HOTBAR_SIZE;

	private int currentGrindProgress = 0;

	public MenuMillStone(@NotNull ContainerInventory inventory, TileEntityMillStone tileEntityMillStone) {
		this.millStone = tileEntityMillStone;
		for (int row = 0; row < 3; row++) {

			for (int column = 0; column < 1; column++) {
				this.addSlot(new Slot(tileEntityMillStone, column + row, 8 + (column + 4) * 18, 43 + row * 18));
			}
		}

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 111 + row * 18));
			}
		}

		for (int column = 0; column < 9; column++) {
			this.addSlot(new Slot(inventory, column, 8 + column * 18, 169));
		}
	}

	@Override
	public IntList getMoveSlots(@NotNull InventoryAction inventoryAction, @NotNull Slot slot, int i, Player player) {
		if (slot.index >= 0 && slot.index < MILL_SLOTS) {
			return this.getSlots(slot.index, 1, false);
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
			// Player slot to millstone
			if (slot.getItemStack() != null) {
				return this.getSlots(0, MILL_SLOTS, false);
			}

			// Shuffle between main inventory and hotbar
			return slot.index < HOTBAR_START
				? this.getSlots(HOTBAR_START, HOTBAR_SIZE, false)
				: this.getSlots(INV_START, INV_SIZE, false);
		}

		// Millstone slot to player inventory
		if (slot.index >= 0 && slot.index < MILL_SLOTS) {
			return this.getSlots(INV_START, PLAYER_SLOTS, false);
		}

		return IntLists.EMPTY_LIST;
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		int progress = this.millStone.getGrindProgress();

		if (this.currentGrindProgress != progress) {
			for (ContainerListener listener : this.containerListeners) {
				listener.updateCraftingInventoryInfo(this, 0, progress);
			}
			this.currentGrindProgress = progress;
		}
	}

	@Override
	public void setData(int id, int value) {
		if (id == 0) {
			this.millStone.setGrindProgress(value);
		}
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return this.millStone.stillValid(player);
	}
}
