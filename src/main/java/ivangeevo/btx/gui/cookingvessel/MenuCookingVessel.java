package ivangeevo.btx.gui.cookingvessel;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import ivangeevo.btx.block.entity.TileEntityCookingVessel;
import net.minecraft.core.InventoryAction;
import net.minecraft.core.crafting.ContainerListener;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.player.inventory.slot.Slot;
import org.jetbrains.annotations.NotNull;

public class MenuCookingVessel extends MenuAbstract {
	private static final int VESSEL_ROWS = 3;
	private static final int VESSEL_COLUMNS = 9;
	private static final int VESSEL_SLOTS = VESSEL_ROWS * VESSEL_COLUMNS;
	private static final int INV_START = VESSEL_SLOTS;
	private static final int INV_SIZE = 27;
	private static final int HOTBAR_START = INV_START + INV_SIZE;
	private static final int HOTBAR_SIZE = 9;
	private static final int PLAYER_SLOTS = INV_SIZE + HOTBAR_SIZE;

	private final TileEntityCookingVessel vessel;
	private int lastScaledCookCounter;

	public MenuCookingVessel(@NotNull ContainerInventory inventory, @NotNull TileEntityCookingVessel vessel) {
		this.vessel = vessel;

		for (int row = 0; row < VESSEL_ROWS; row++) {
			for (int column = 0; column < VESSEL_COLUMNS; column++) {
				this.addSlot(new Slot(vessel, column + row * VESSEL_COLUMNS, 8 + column * 18, 43 + row * 18));
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
		if (slot.index >= 0 && slot.index < VESSEL_SLOTS) {
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
			// Player slot to vessel
			if (slot.getItemStack() != null) {
				return this.getSlots(0, VESSEL_SLOTS, false);
			}

			// Shuffle between main inventory and hotbar
			return slot.index < HOTBAR_START
				? this.getSlots(HOTBAR_START, HOTBAR_SIZE, false)
				: this.getSlots(INV_START, INV_SIZE, false);
		}

		// Vessel slot to player inventory
		if (slot.index >= 0 && slot.index < VESSEL_SLOTS) {
			return this.getSlots(INV_START, PLAYER_SLOTS, false);
		}

		return IntLists.EMPTY_LIST;
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		int progress = this.vessel.scaledCookCounter;

		if (this.lastScaledCookCounter != progress) {
			for (ContainerListener listener : this.containerListeners) {
				listener.updateCraftingInventoryInfo(this, 0, progress);
			}
			this.lastScaledCookCounter = progress;
		}
	}

	@Override
	public void setData(int id, int value) {
		if (id == 0) {
			this.vessel.scaledCookCounter = value;
		}
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return this.vessel.stillValid(player);
	}
}
