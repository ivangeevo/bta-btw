package ivangeevo.btx.gui.pulley;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import ivangeevo.btx.block.entity.TileEntityPulley;
import net.minecraft.core.InventoryAction;
import net.minecraft.core.crafting.ContainerListener;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.player.inventory.slot.Slot;
import org.jetbrains.annotations.NotNull;

public class MenuPulley extends MenuAbstract {
	private static final int SLOT_COLUMNS = 2;
	private static final int PULLEY_SLOTS = TileEntityPulley.INVENTORY_SIZE;
	private static final int INV_START = PULLEY_SLOTS;
	private static final int INV_SIZE = 27;
	private static final int HOTBAR_START = INV_START + INV_SIZE;
	private static final int HOTBAR_SIZE = 9;
	private static final int PLAYER_SLOTS = INV_SIZE + HOTBAR_SIZE;

	private final TileEntityPulley pulley;

	private int lastMechanicalPowerIndicator = -1;

	public MenuPulley(@NotNull ContainerInventory inventory, TileEntityPulley pulley) {
		this.pulley = pulley;

		for (int row = 0; row < PULLEY_SLOTS / SLOT_COLUMNS; row++) {
			for (int column = 0; column < SLOT_COLUMNS; column++) {
				this.addSlot(new Slot(pulley, column + row * SLOT_COLUMNS, 71 + column * 18, 43 + row * 18));
			}
		}

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 93 + row * 18));
			}
		}

		for (int column = 0; column < 9; column++) {
			this.addSlot(new Slot(inventory, column, 8 + column * 18, 151));
		}
	}

	@Override
	public IntList getMoveSlots(@NotNull InventoryAction inventoryAction, @NotNull Slot slot, int i, Player player) {
		if (slot.index >= 0 && slot.index < PULLEY_SLOTS) {
			return inventoryAction == InventoryAction.MOVE_ALL
				? this.getSlots(0, PULLEY_SLOTS, false)
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
			return this.getSlots(0, PULLEY_SLOTS, false);
		}

		if (slot.index >= 0 && slot.index < PULLEY_SLOTS) {
			return this.getSlots(INV_START, PLAYER_SLOTS, false);
		}

		return IntLists.EMPTY_LIST;
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		int indicator = this.pulley.mechanicalPowerIndicator;

		if (this.lastMechanicalPowerIndicator != indicator) {
			for (ContainerListener listener : this.containerListeners) {
				listener.updateCraftingInventoryInfo(this, 0, indicator);
			}

			this.lastMechanicalPowerIndicator = indicator;
		}
	}

	@Override
	public void setData(int id, int value) {
		if (id == 0) {
			this.pulley.mechanicalPowerIndicator = value;
		}
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return this.pulley.stillValid(player);
	}
}