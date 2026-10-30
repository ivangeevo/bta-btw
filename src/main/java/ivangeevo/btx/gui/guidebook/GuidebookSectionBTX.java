package ivangeevo.btx.gui.guidebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ItemElement;
import net.minecraft.client.gui.TooltipElement;
import net.minecraft.client.gui.guidebook.*;
import net.minecraft.client.gui.guidebook.search.GuidebookPageSearch;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.enums.DescriptionPromptEnum;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.RecipeEntryBase;
import net.minecraft.core.data.registry.recipe.RecipeGroup;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Environment(EnvType.CLIENT)
public abstract class GuidebookSectionBTX<T extends RecipeEntryBase<?, ?, ?>> extends SearchableGuidebookSection {
	private final List<GuidebookPage> pages = new ArrayList<>();
	private int cachedRecipeCount = -1;
	private String cachedQuery = null;
	private List<GuidebookPage> cachedSearchPages = null;

	public GuidebookSectionBTX(String translationKey, ItemStack tabIcon, int bgColor, int fgColor) {
		super(translationKey, tabIcon, bgColor, fgColor);
	}

	protected abstract List<T> getRecipes();

	protected abstract boolean matchesQuery(T recipe, SearchQuery query);

	protected abstract int getRecipesPerPage();

	protected abstract GuidebookPage createPage(List<T> recipes);

	@Override
	public List<GuidebookPage> getPages() {
		List<T> recipes = getRecipes();
		if (recipes.size() != this.cachedRecipeCount) {
			this.cachedRecipeCount = recipes.size();
			this.cachedQuery = null;
			this.cachedSearchPages = null;
			this.pages.clear();
			this.pages.addAll(paginate(recipes));
		}
		return this.pages;
	}

	@Override
	public List<GuidebookPage> searchPages(SearchQuery query) {
		if (this.cachedSearchPages != null && Objects.equals(this.cachedQuery, query.rawQuery)) {
			return this.cachedSearchPages;
		}

		List<T> filtered = new ArrayList<>();
		for (T recipe : getRecipes()) {
			try {
				if (matchesQuery(recipe, query)) {
					filtered.add(recipe);
				}
			} catch (NullPointerException | IllegalArgumentException ignored) {}
		}

		this.cachedQuery = query.rawQuery;
		this.cachedSearchPages = paginate(filtered);
		return this.cachedSearchPages;
	}

	@Override
	public List<Index> getIndices() {
		return null;
	}

	private List<GuidebookPage> paginate(List<T> recipes) {
		List<GuidebookPage> result = new ArrayList<>();
		for (List<T> page : splitIntoPages(recipes)) {
			result.add(createPage(page));
		}
		return result;
	}

	protected List<List<T>> splitIntoPages(List<T> recipes) {
		int perPage = getRecipesPerPage();
		List<List<T>> pages = new ArrayList<>();
		for (int i = 0; i < recipes.size(); i += perPage) {
			pages.add(new ArrayList<>(recipes.subList(i, Math.min(i + perPage, recipes.size()))));
		}
		return pages;
	}

	protected static boolean matchesStack(ItemStack stack, SearchQuery query) {
		if (stack == null) {
			return false;
		}
		String term = query.query.getRight();
		if (query.query.getLeft() == SearchQuery.QueryType.NAME) {
			return query.strict ? stack.getDisplayName().equalsIgnoreCase(term) : stack.getDisplayName().toLowerCase().contains(term.toLowerCase());
		}
		if (query.query.getLeft() == SearchQuery.QueryType.GROUP && !Objects.equals(term, "")) {
			List<ItemStack> groupStacks = new RecipeSymbol(term).resolve();
			return groupStacks != null && groupStacks.contains(stack);
		}
		return false;
	}

	protected static boolean matchesScope(RecipeEntryBase<?, ?, ?> recipe, SearchQuery query) {
		return switch (query.scope.getLeft()) {
			case NONE -> true;
			case NAMESPACE -> Registries.RECIPES.getItem(query.scope.getRight()) == recipe.parent.getParent();
			case NAMESPACE_GROUP -> {
				RecipeGroup<?> group;
				try {
					group = Registries.RECIPES.getGroupFromKey(query.scope.getRight());
				} catch (IllegalArgumentException e) {
					group = null;
				}
				yield group == recipe.parent;
			}
		};
	}

	@Environment(EnvType.CLIENT)
	protected static abstract class RecipePageBTX<T extends RecipeEntryBase<?, ?, ?>> extends RecipePage<T> {
		protected static final String GUIDEBOOK_TEXTURE = "/assets/minecraft/textures/gui/container/guidebook/guidebook.png";
		protected static final Minecraft mc = Minecraft.getMinecraft();

		protected final List<SlotGuidebook> slots = new ArrayList<>();
		protected final Set<SlotGuidebook> backgroundlessSlots = new HashSet<>();
		protected final Set<SlotGuidebook> hiddenSlots = new HashSet<>();
		protected final TooltipElement tooltipElement = new TooltipElement(mc);
		protected final ItemElement itemElement = new ItemElement(mc);
		private long ticks = 0L;

		public RecipePageBTX(GuidebookSection section, List<T> recipes) {
			super(section);
			this.recipes = recipes;
		}

		protected @Nullable String getExtraTooltip(int x, int y, int mouseX, int mouseY) {
			return null;
		}

		protected @Nullable String getSlotTooltipSuffix(SlotGuidebook slot) {
			return null;
		}

		// RecipeSymbol.resolve() drops the amount for item groups, so the cycled stacks are rebuilt with it here
		protected static SlotGuidebook createInputSlot(int id, int x, int y, @Nullable RecipeSymbol symbol, RecipeEntryBase<?, ?, ?> recipe) {
			SlotGuidebook slot = new SlotGuidebook(id, x, y, symbol, false, recipe);

			if (symbol != null && symbol.getItemGroup() != null && symbol.getAmount() > 1) {
				List<ItemStack> stacks = new ArrayList<>();

				for (ItemStack stack : symbol.resolve()) {
					ItemStack copy = stack.copy();
					copy.stackSize = symbol.getAmount();
					stacks.add(copy);
				}

				slot.searchFilteredItems = stacks;
				slot.randomizeCycleStart();
			}

			return slot;
		}

		protected static List<ItemStack> mergeOutputs(List<ItemStack> outputs) {
			List<ItemStack> merged = new ArrayList<>();

			for (ItemStack output : outputs) {
				ItemStack existing = null;

				for (ItemStack stack : merged) {
					if (stack.itemID == output.itemID && stack.getMetadata() == output.getMetadata()) {
						existing = stack;
						break;
					}
				}

				if (existing != null) {
					existing.stackSize += output.stackSize;
				} else {
					merged.add(output.copy());
				}
			}

			return merged;
		}

		protected void drawArrow(int x, int y) {
			this.drawTexturedModalRect(x, y, 234, 0, 22, 15);
		}

		protected static boolean isInside(int mouseX, int mouseY, int x, int y, int width, int height) {
			return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
		}

		@Override
		public void onTick() {
			this.ticks++;
			if (this.ticks > 20L) {
				this.ticks = 0L;
				for (SlotGuidebook slot : this.slots) {
					slot.showRandomItem();
				}
			}
		}

		@Override
		public void onBecomeVisible() {
			for (SlotGuidebook slot : this.slots) {
				slot.randomizeCycleStart();
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

			String prefix;

			if (GameSettings.KEY_SHOW_RECIPE.isKeyboardKey(key)) {
				prefix = "r:";
			} else if (GameSettings.KEY_SHOW_USAGE.isKeyboardKey(key)) {
				prefix = "u:";
			} else {
				return false;
			}

			SlotGuidebook hovered = getHoveredSlot(x, y, mouseX, mouseY);

			if (hovered == null || !hovered.hasItem()) {
				return false;
			}

			String query = prefix + hovered.getItemStack().getDisplayName() + "!";
			GuidebookPageManager.searchQuery = SearchQuery.resolve(query);
			GuidebookPageSearch.searchField.setText(query);
			ScreenGuidebook.getPageManager().updatePages();
			ScreenGuidebook.getPageManager().setCurrentPage(ScreenGuidebook.getPageManager().getSectionIndex(GuidebookSections.CRAFTING), true);

			return true;
		}

		private @Nullable SlotGuidebook getHoveredSlot(int x, int y, int mouseX, int mouseY) {
			for (SlotGuidebook slot : this.slots) {
				if (isInside(mouseX, mouseY, x + slot.x - 1, y + slot.y - 1, 18, 18)) {
					return slot;
				}
			}

			return null;
		}
	}
}
