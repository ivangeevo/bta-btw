package ivangeevo.btx.gui.hopper;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import ivangeevo.btx.block.entity.TileEntityHopper;
import ivangeevo.btx.recipe.BTXRecipes;
import net.minecraft.core.InventoryAction;
import net.minecraft.core.crafting.ContainerListener;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.player.inventory.slot.Slot;
import org.jetbrains.annotations.NotNull;

public class MenuHopper extends MenuAbstract {
	private static final int SLOT_ROWS = 2;
	private static final int SLOT_COLUMNS = 9;

	private static final int STORAGE_SLOTS = SLOT_ROWS * SLOT_COLUMNS;
	private static final int FILTER_SLOT = STORAGE_SLOTS;
	private static final int INV_START = STORAGE_SLOTS + 1;
	private static final int INV_SIZE = 27;
	private static final int HOTBAR_START = INV_START + INV_SIZE;
	private static final int HOTBAR_SIZE = 9;
	private static final int PLAYER_SLOTS = INV_SIZE + HOTBAR_SIZE;

	private final TileEntityHopper hopper;

	private int lastMechanicalPowerIndicator = 0;

	public MenuHopper(@NotNull ContainerInventory inventory, TileEntityHopper hopper) {
		this.hopper = hopper;

		for (int row = 0; row < SLOT_ROWS; row++) {
			for (int column = 0; column < SLOT_COLUMNS; column++) {
				this.addSlot(new Slot(hopper, column + row * SLOT_COLUMNS, 8 + column * 18, 60 + row * 18));
			}
		}

		this.addSlot(new SlotHopperFilter(hopper, TileEntityHopper.FILTER_SLOT, 80, 37));

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
		if (slot.index >= 0 && slot.index <= FILTER_SLOT) {
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
			// player slot to hopper storage (CE: shift-clicking never fills the filter slot)
			if (slot.getItemStack() != null) {
				return this.getSlots(0, STORAGE_SLOTS, false);
			}

			return slot.index < HOTBAR_START
				? this.getSlots(HOTBAR_START, HOTBAR_SIZE, false)
				: this.getSlots(INV_START, INV_SIZE, false);
		}

		// hopper slot to player inventory
		if (slot.index >= 0 && slot.index <= FILTER_SLOT) {
			return this.getSlots(INV_START, PLAYER_SLOTS, false);
		}

		return IntLists.EMPTY_LIST;
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		int indicator = this.hopper.mechanicalPowerIndicator;

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
			this.hopper.mechanicalPowerIndicator = value;
		}
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return this.hopper.stillValid(player);
	}

	public static class SlotHopperFilter extends Slot {
		public SlotHopperFilter(Container container, int index, int x, int y) {
			super(container, index, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return BTXRecipes.HOPPER_FILTER.isFilter(stack);
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}
	}
}
