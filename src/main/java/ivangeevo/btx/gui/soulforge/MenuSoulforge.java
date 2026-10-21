package ivangeevo.btx.gui.soulforge;

import it.unimi.dsi.fastutil.ints.IntList;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.soulforge.RecipeEntrySoulforgeBase;
import ivangeevo.btx.sound.BTXSoundTypes;
import net.minecraft.core.InventoryAction;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.player.inventory.container.ContainerCrafting;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.player.inventory.container.ContainerResult;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.player.inventory.slot.Slot;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public class MenuSoulforge extends MenuAbstract {
	private static final int GRID_SIZE = RecipeEntrySoulforgeBase.GRID_SIZE;
	private static final int GRID_SLOTS = GRID_SIZE * GRID_SIZE;
	private static final int GRID_START = 1;
	private static final int INV_START = GRID_START + GRID_SLOTS;
	private static final int INV_SIZE = 27;
	private static final int HOTBAR_START = INV_START + INV_SIZE;
	private static final int HOTBAR_SIZE = 9;
	private static final int PLAYER_SLOTS = INV_SIZE + HOTBAR_SIZE;

	public final @NotNull ContainerCrafting craftSlots = new ContainerCrafting(this, GRID_SIZE, GRID_SIZE);
	public final @NotNull Container resultSlots = new ContainerResult();
	private final @NotNull World world;
	private final @NotNull TilePos tilePos;

	public MenuSoulforge(@NotNull ContainerInventory inventory, @NotNull World world, @NotNull TilePosc tilePos) {
		this.world = world;
		this.tilePos = new TilePos(tilePos);
		this.addSlot(new SlotResultSoulforge(inventory.player, this.craftSlots, this.resultSlots, 0, 136, 44));

		for (int y = 0; y < GRID_SIZE; y++) {
			for (int x = 0; x < GRID_SIZE; x++) {
				this.addSlot(new Slot(this.craftSlots, x + y * GRID_SIZE, 23 + x * 18, 17 + y * 18));
			}
		}

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 102 + y * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inventory, i, 8 + i * 18, 160));
		}

		this.slotsChanged(this.craftSlots);
	}

	@Override
	public void slotsChanged(Container container) {
		RecipeEntrySoulforgeBase<?, ?> recipe = BTXRecipes.SOULFORGE.findRecipe(this.craftSlots);
		this.resultSlots.setItem(0, recipe == null ? null : recipe.getCraftingResult(this.craftSlots));
	}

	@Override
	public void onCraftGuiClosed(@NotNull Player player) {
		super.onCraftGuiClosed(player);
		boolean returned = false;

		for (int i = 0; i < GRID_SLOTS; i++) {
			ItemStack stack = this.craftSlots.getItem(i);
			if (stack != null) {
				this.storeOrDropItem(player, stack);
				returned = true;
			}
			this.craftSlots.setItem(i, null);
		}

		this.resultSlots.setItem(0, null);

		if (returned) {
			player.world.playSoundAtEntity((Entity) null, player, BTXSoundTypes.SOULFORGE_RETURN_ITEMS, 0.1F, 1.0F);
		}
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		if (this.world.getBlockType(this.tilePos) != BTXBlocks.SOULFORGE) {
			return false;
		}
		return player.distanceToSqr(this.tilePos.x + 0.5D, this.tilePos.y + 0.5D, this.tilePos.z + 0.5D) <= 64.0D;
	}

	@Override
	public IntList getMoveSlots(@NotNull InventoryAction action, @NotNull Slot slot, int target, Player player) {
		if (slot.index == 0) {
			return this.getSlots(0, 1, false);
		}
		if (slot.index >= GRID_START && slot.index < INV_START) {
			return this.getSlots(GRID_START, GRID_SLOTS, false);
		}
		if (action == InventoryAction.MOVE_SIMILAR) {
			if (slot.index >= INV_START && slot.index < INV_START + PLAYER_SLOTS) {
				return this.getSlots(INV_START, PLAYER_SLOTS, false);
			}
		} else {
			if (slot.index >= INV_START && slot.index < HOTBAR_START) {
				return this.getSlots(INV_START, INV_SIZE, false);
			}
			if (slot.index >= HOTBAR_START && slot.index < HOTBAR_START + HOTBAR_SIZE) {
				return this.getSlots(HOTBAR_START, HOTBAR_SIZE, false);
			}
		}
		return null;
	}

	@Override
	public IntList getTargetSlots(@NotNull InventoryAction action, @NotNull Slot slot, int target, Player player) {
		if (slot.index >= INV_START && slot.index < INV_START + PLAYER_SLOTS) {
			if (target == 1) {
				return this.getSlots(GRID_START, GRID_SLOTS, false);
			}
			if (slot.index < HOTBAR_START) {
				return this.getSlots(HOTBAR_START, HOTBAR_SIZE, false);
			}
			return this.getSlots(INV_START, INV_SIZE, false);
		}
		return slot.index == 0
			? this.getSlots(INV_START, PLAYER_SLOTS, true)
			: this.getSlots(INV_START, PLAYER_SLOTS, false);
	}
}
