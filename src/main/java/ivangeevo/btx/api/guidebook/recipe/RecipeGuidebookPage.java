package ivangeevo.btx.api.guidebook.recipe;

import ivangeevo.btx.api.guidebook.Guidebooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ItemElement;
import net.minecraft.client.gui.TooltipElement;
import net.minecraft.client.gui.guidebook.GuidebookPageManager;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.RecipePage;
import net.minecraft.client.gui.guidebook.SlotGuidebook;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.enums.DescriptionPromptEnum;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.core.data.registry.recipe.RecipeEntryBase;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * A guidebook page that shows recipes with item slots.
 * <p>
 * Add slots in the constructor with {@link #addInput}, {@link #addOutput} or {@link #addCycling},
 * then draw the rest (arrows, labels) in {@link #renderBackground} or {@link #renderForeground}.
 * Slots draw themselves, show tooltips, cycle through item groups and answer R/U lookups.
 * Coordinates are relative to the page's top-left corner; a page is 158×220.
 *
 * @param <T> the recipe type
 */
@Environment(EnvType.CLIENT)
public abstract class RecipeGuidebookPage<T extends RecipeEntryBase<?, ?, ?>> extends RecipePage<T> {
	/** The vanilla guidebook texture, which has the arrow at (234, 0). */
	protected static final String GUIDEBOOK_TEXTURE = "/assets/minecraft/textures/gui/container/guidebook/guidebook.png";
	/** Ticks between two items of a cycling slot. */
	protected static final int CYCLE_TICKS = 20;
	protected static final int SLOT_SIZE = 18;
	protected static final Minecraft mc = Minecraft.getMinecraft();
	private static final Random RANDOM = new Random();

	/** Every slot on the page, in id order. */
	protected final List<SlotGuidebook> slots = new ArrayList<>();
	/** Slots drawn without the slot background. */
	protected final Set<SlotGuidebook> backgroundlessSlots = new HashSet<>();
	/** Slots that are not drawn but still show tooltips and answer R/U. */
	protected final Set<SlotGuidebook> hiddenSlots = new HashSet<>();
	protected final TooltipElement tooltipElement = new TooltipElement(mc);
	protected final ItemElement itemElement = new ItemElement(mc);

	private final Map<SlotGuidebook, String> tooltipSuffixes = new HashMap<>();
	private final Map<SlotGuidebook, Cycle> cycles = new HashMap<>();
	private int ticks = 0;

	public RecipeGuidebookPage(GuidebookSection section, List<T> recipes) {
		super(section);
		this.recipes = recipes;
	}

	/** Adds an input slot showing a recipe symbol, cycling through item groups with their amount kept. */
	protected SlotGuidebook addInput(int x, int y, @Nullable RecipeSymbol symbol, @Nullable T recipe) {
		SlotGuidebook slot = new SlotGuidebook(this.slots.size(), x, y, symbol, false, recipe);

		if (symbol != null && symbol.getItemGroup() != null) {
			List<ItemStack> stacks = new ArrayList<>();

			for (ItemStack stack : symbol.resolve()) {
				ItemStack copy = stack.copy();
				copy.stackSize = Math.max(1, symbol.getAmount());
				stacks.add(copy);
			}

			this.cycles.put(slot, new Cycle(stacks));
		}

		return addSlot(slot);
	}

	/** Adds an input slot showing a single stack. */
	protected SlotGuidebook addInput(int x, int y, @Nullable ItemStack stack, @Nullable T recipe) {
		return addStack(x, y, stack, recipe, false);
	}

	/** Adds an output slot showing a single stack. */
	protected SlotGuidebook addOutput(int x, int y, @Nullable ItemStack stack, @Nullable T recipe) {
		return addStack(x, y, stack, recipe, true);
	}

	/** Adds a slot that cycles through the given stacks, e.g. every filter accepted by a recipe. */
	protected SlotGuidebook addCycling(int x, int y, @NotNull List<ItemStack> stacks, @Nullable T recipe) {
		SlotGuidebook slot = addStack(x, y, stacks.isEmpty() ? null : stacks.get(0), recipe, false);

		if (stacks.size() > 1) {
			this.cycles.put(slot, new Cycle(new ArrayList<>(stacks)));
		}

		return slot;
	}

	/** Adds a slot built by hand. Its id should be {@code slots.size()}. */
	protected SlotGuidebook addSlot(SlotGuidebook slot) {
		this.slots.add(slot);

		Cycle cycle = this.cycles.get(slot);

		if (cycle != null) {
			cycle.randomize(slot);
		}

		return slot;
	}

	/** Shows an extra line under the slot's tooltip, e.g. what the slot means. */
	protected void setTooltipSuffix(SlotGuidebook slot, @Nullable String suffix) {
		if (suffix == null) {
			this.tooltipSuffixes.remove(slot);
		} else {
			this.tooltipSuffixes.put(slot, suffix);
		}
	}

	/** An extra line under a slot's tooltip. Defaults to the one set with {@link #setTooltipSuffix}. */
	protected @Nullable String getSlotTooltipSuffix(SlotGuidebook slot) {
		return this.tooltipSuffixes.get(slot);
	}

	/** A tooltip for something that is not a slot, e.g. an icon. Called when no slot is hovered. */
	protected @Nullable String getExtraTooltip(int x, int y, int mouseX, int mouseY) {
		return null;
	}

	/** Draws the guidebook arrow (22×15). Bind {@link #GUIDEBOOK_TEXTURE} first. */
	protected void drawArrow(int x, int y) {
		this.drawTexturedModalRect(x, y, 234, 0, 22, 15);
	}

	/** Whether the mouse is inside the rectangle. */
	protected static boolean isInside(int mouseX, int mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	/** Copies the stacks, adding up the amounts of equal items. */
	protected static List<ItemStack> mergeStacks(List<ItemStack> stacks) {
		List<ItemStack> merged = new ArrayList<>();

		for (ItemStack stack : stacks) {
			ItemStack existing = null;

			for (ItemStack other : merged) {
				if (other.itemID == stack.itemID && other.getMetadata() == stack.getMetadata()) {
					existing = other;
					break;
				}
			}

			if (existing != null) {
				existing.stackSize += stack.stackSize;
			} else {
				merged.add(stack.copy());
			}
		}

		return merged;
	}

	@Override
	public void onTick() {
		if (++this.ticks < CYCLE_TICKS) {
			return;
		}

		this.ticks = 0;

		for (SlotGuidebook slot : this.slots) {
			Cycle cycle = this.cycles.get(slot);
			if (cycle != null) {
				cycle.next(slot);
			} else {
				slot.showRandomItem();
			}
		}
	}

	@Override
	public void onBecomeVisible() {
		SearchQuery query = Guidebooks.isLookupActive() ? GuidebookPageManager.searchQuery : null;
		for (SlotGuidebook slot : this.slots) {
			Cycle cycle = this.cycles.get(slot);
			if (cycle != null) {
				cycle.filter(query);
				cycle.randomize(slot);
			} else {
				slot.applySearchFilter(query);
				slot.randomizeCycleStart();
			}
		}
	}

	@Override
	protected void renderForeground(TextureManager re, FontRenderer sr, int x, int y, int mouseX, int mouseY, float partialTicks) {
		if (this.recipes.isEmpty()) {
			this.drawStringCenteredNoShadow(sr, I18n.getInstance().translateKey("guidebook.section.search.error.no_recipes"), x + this.width / 2, y + this.height / 2, -8355712);
		}

		SlotGuidebook hovered = getHoveredSlot(x, y, mouseX, mouseY);
		for (SlotGuidebook slot : this.slots) {
			if (this.hiddenSlots.contains(slot)) {
				continue;
			}

			if (!this.backgroundlessSlots.contains(slot)) {
				this.drawSlot(x + slot.x - 1, y + slot.y - 1, -1);
			}

			this.itemElement.render(slot.getItemStack(), x + slot.x, y + slot.y, hovered == slot, slot);
		}
	}

	@Override
	protected void renderOverlay(TextureManager re, FontRenderer sr, int x, int y, int mouseX, int mouseY, float partialTicks) {
		super.renderOverlay(re, sr, x, y, mouseX, mouseY, partialTicks);

		SlotGuidebook hovered = getHoveredSlot(x, y, mouseX, mouseY);
		String text = null;

		if (hovered != null && hovered.hasItem()) {
			text = this.tooltipElement.getTooltipText(hovered.getItemStack(), DescriptionPromptEnum.showDescription(), hovered);
			String suffix = getSlotTooltipSuffix(hovered);
			if (suffix != null) {
				text = text + "\n" + suffix;
			}
		} else if (hovered == null) {
			text = getExtraTooltip(x, y, mouseX, mouseY);
		}

		if (text != null && !text.isEmpty()) {
			this.tooltipElement.render(text, mouseX, mouseY, 8, -8);
		}
	}

	@Override
	public boolean keyTyped(char c, int key, int x, int y, int mouseX, int mouseY) {
		super.keyTyped(c, key, x, y, mouseX, mouseY);

		SearchQuery.SearchMode mode;
		if (GameSettings.KEY_SHOW_RECIPE.isKeyboardKey(key)) {
			mode = SearchQuery.SearchMode.RECIPE;
		} else if (GameSettings.KEY_SHOW_USAGE.isKeyboardKey(key)) {
			mode = SearchQuery.SearchMode.USAGE;
		} else {
			return false;
		}

		SlotGuidebook hovered = getHoveredSlot(x, y, mouseX, mouseY);
		if (hovered == null || !hovered.hasItem()) {
			return false;
		}

		Guidebooks.lookup(hovered.getItemStack(), mode);

		return true;
	}

	/** The slot under the mouse, or {@code null}. */
	protected @Nullable SlotGuidebook getHoveredSlot(int x, int y, int mouseX, int mouseY) {
		for (SlotGuidebook slot : this.slots) {
			if (isInside(mouseX, mouseY, x + slot.x - 1, y + slot.y - 1, SLOT_SIZE, SLOT_SIZE)) {
				return slot;
			}
		}

		return null;
	}

	private SlotGuidebook addStack(int x, int y, @Nullable ItemStack stack, @Nullable T recipe, boolean output) {
		SlotGuidebook slot = new SlotGuidebook(this.slots.size(), x, y, null, false, recipe);
		slot.item = stack;

		if (output) {
			slot.setAsOutput();
		}

		return addSlot(slot);
	}

	private static final class Cycle {
		private final List<ItemStack> all;
		private List<ItemStack> shown;
		private int index = 0;

		private Cycle(List<ItemStack> all) {
			this.all = all;
			this.shown = all;
		}

		private void filter(@Nullable SearchQuery query) {
			this.shown = this.all;

			if (query == null || this.all.size() < 2) {
				return;
			}

			List<ItemStack> matches = new ArrayList<>();
			for (ItemStack stack : this.all) {
				if (GuidebookSearch.matchesStack(stack, query)) {
					matches.add(stack);
				}
			}

			if (!matches.isEmpty() && matches.size() < this.all.size()) {
				this.shown = matches;
			}
		}

		private void randomize(SlotGuidebook slot) {
			if (this.shown.isEmpty()) {
				return;
			}

			this.index = RANDOM.nextInt(this.shown.size());
			slot.item = this.shown.get(this.index);
		}

		private void next(SlotGuidebook slot) {
			if (this.shown.isEmpty()) {
				return;
			}
			this.index = (this.index + 1) % this.shown.size();
			slot.item = this.shown.get(this.index);
		}
	}
}
