package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.hopper.RecipeEntryHopperFilter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.SearchableGuidebookSection;
import net.minecraft.client.gui.guidebook.SlotGuidebook;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.player.inventory.menu.MenuInventoryCreative;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class GuidebookSectionHopperFilter extends SearchableGuidebookSection {
	private final List<GuidebookPage> pages = new ArrayList<>();
	private final List<FilterGroup> groups = new ArrayList<>();
	private int cachedRecipeCount = -1;
	private String cachedQuery = null;
	private List<GuidebookPage> cachedSearchPages = null;

	public GuidebookSectionHopperFilter() {
		super("btx.guidebook.section.hopper_filter", new ItemStack(BTXBlocks.WICKER_PANE), 0x4A3A28, 0xC8B070);
	}

	@Override
	public List<GuidebookPage> getPages() {
		refresh();
		return this.pages;
	}

	@Override
	public List<GuidebookPage> searchPages(SearchQuery query) {
		refresh();

		if (this.cachedSearchPages != null && Objects.equals(this.cachedQuery, query.rawQuery)) {
			return this.cachedSearchPages;
		}

		List<GuidebookPage> result = new ArrayList<>();

		if (query.mode != SearchQuery.SearchMode.RECIPE) {
			for (FilterGroup group : this.groups) {
				try {
					if (!GuidebookSectionBTX.matchesScope(group.recipes.get(0), query)) {
						continue;
					}

					if (matchesFilter(group, query)) {
						result.addAll(paginate(group, group.accepted));
						continue;
					}

					List<ItemStack> matching = new ArrayList<>();
					for (ItemStack stack : group.accepted) {
						if (GuidebookSectionBTX.matchesStack(stack, query)) {
							matching.add(stack);
						}
					}

					if (!matching.isEmpty()) {
						result.addAll(paginate(group, matching));
					}
				} catch (NullPointerException | IllegalArgumentException ignored) {}
			}
		}

		this.cachedQuery = query.rawQuery;
		this.cachedSearchPages = result;

		return result;
	}

	@Override
	public List<Index> getIndices() {
		return null;
	}

	private void refresh() {
		List<RecipeEntryHopperFilter> recipes = BTXRecipes.HOPPER_FILTER == null ? new ArrayList<>() : new ArrayList<>(BTXRecipes.HOPPER_FILTER.getAllRecipes());

		if (recipes.size() == this.cachedRecipeCount) {
			return;
		}

		this.cachedRecipeCount = recipes.size();
		this.cachedQuery = null;
		this.cachedSearchPages = null;
		this.groups.clear();
		this.pages.clear();

		List<ItemStack> candidates = getCandidates();

		for (RecipeEntryHopperFilter recipe : recipes) {
			List<ItemStack> accepted = new ArrayList<>();

			for (ItemStack candidate : candidates) {
				if (recipe.canProcess(candidate)) {
					accepted.add(candidate);
				}
			}

			FilterGroup group = findGroup(accepted);
			if (group == null) {
				group = new FilterGroup(accepted);
				this.groups.add(group);
			}
			group.add(recipe);
		}

		for (FilterGroup group : this.groups) {
			this.pages.addAll(paginate(group, group.accepted));
		}
	}

	private FilterGroup findGroup(List<ItemStack> accepted) {
		for (FilterGroup group : this.groups) {
			if (sameStacks(group.accepted, accepted)) {
				return group;
			}
		}

		return null;
	}

	private static boolean sameStacks(List<ItemStack> a, List<ItemStack> b) {
		if (a.size() != b.size()) {
			return false;
		}

		for (int i = 0; i < a.size(); i++) {
			if (a.get(i).itemID != b.get(i).itemID || a.get(i).getMetadata() != b.get(i).getMetadata()) {
				return false;
			}
		}

		return true;
	}

	private static List<ItemStack> getCandidates() {
		List<ItemStack> candidates = new ArrayList<>();
		Set<String> seen = new HashSet<>();

		for (ItemStack stack : MenuInventoryCreative.creativeContents) {
			if (stack != null && stack.getItem() != null && seen.add(stack.itemID + ":" + stack.getMetadata())) {
				ItemStack copy = stack.copy();
				copy.stackSize = 1;
				candidates.add(copy);
			}
		}

		return candidates;
	}

	private static boolean matchesFilter(FilterGroup group, SearchQuery query) {
		for (ItemStack filter : group.filters) {
			if (GuidebookSectionBTX.matchesStack(filter, query)) {
				return true;
			}
		}

		return false;
	}

	private List<GuidebookPage> paginate(FilterGroup group, List<ItemStack> items) {
		List<GuidebookPage> result = new ArrayList<>();
		int perPage = RecipePage.ITEMS_PER_PAGE;
		int pageCount = Math.max(1, (items.size() + perPage - 1) / perPage);

		for (int page = 0; page < pageCount; page++) {
			List<ItemStack> pageItems = new ArrayList<>(items.subList(page * perPage, Math.min((page + 1) * perPage, items.size())));
			result.add(new RecipePage(this, group.recipes.get(0), group.filters, pageItems, page, pageCount));
		}

		return result;
	}

	private static final class FilterGroup {
		private final List<RecipeEntryHopperFilter> recipes = new ArrayList<>();
		private final List<ItemStack> filters = new ArrayList<>();
		private final List<ItemStack> accepted;

		private FilterGroup(List<ItemStack> accepted) {
			this.accepted = accepted;
		}

		private void add(RecipeEntryHopperFilter recipe) {
			this.recipes.add(recipe);

			List<ItemStack> resolved = recipe.getInput().resolve();
			if (resolved != null) {
				this.filters.addAll(resolved);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class RecipePage extends GuidebookSectionBTX.RecipePageBTX<RecipeEntryHopperFilter> {
		public static final int COLUMNS = 8;
		public static final int ROWS = 7;
		public static final int ITEMS_PER_PAGE = COLUMNS * ROWS;
		private static final int SLOT_SIZE = 18;
		private static final int HOPPER_X = 71;
		private static final int FILTER_Y = 8;
		private static final int HOPPER_Y = 26;
		private static final int LABEL_Y = 48;
		private static final int GRID_X = 8;
		private static final int GRID_Y = 60;
		private static final int LABEL_COLOR = 0xFF404040;

		private final SlotGuidebook filterSlot;
		private final int pageIndex;
		private final int pageCount;

		public RecipePage(GuidebookSection section, RecipeEntryHopperFilter recipe, List<ItemStack> filters, List<ItemStack> items, int pageIndex, int pageCount) {
			super(section, List.of(recipe));
			this.pageIndex = pageIndex;
			this.pageCount = pageCount;

			int id = 0;

			this.filterSlot = createInputSlot(id++, HOPPER_X, FILTER_Y, recipe.getInput(), recipe);
			if (filters.size() > 1) {
				this.filterSlot.searchFilteredItems = filters;
				this.filterSlot.randomizeCycleStart();
			}

			this.slots.add(this.filterSlot);

			SlotGuidebook hopper = new SlotGuidebook(id++, HOPPER_X, HOPPER_Y, new RecipeSymbol(new ItemStack(BTXBlocks.HOPPER)), false, recipe);
			this.slots.add(hopper);
			this.backgroundlessSlots.add(hopper);

			for (int i = 0; i < items.size(); i++) {
				ItemStack stack = items.get(i);
				SlotGuidebook slot = new SlotGuidebook(id++, GRID_X + i % COLUMNS * SLOT_SIZE, GRID_Y + i / COLUMNS * SLOT_SIZE, new RecipeSymbol(stack), false, recipe);
				slot.item = stack;
				this.slots.add(slot);
			}
		}

		@Override
		protected void renderForeground(TextureManager re, FontRenderer sr, int x, int y, int mouseX, int mouseY, float partialTicks) {
			String label = I18n.getInstance().translateKey("btx.guidebook.hopper_filter.filtered_items");
			if (this.pageCount > 1) {
				label = label + " (" + (this.pageIndex + 1) + "/" + this.pageCount + ")";
			}
			this.drawStringCenteredNoShadow(sr, label, x + this.width / 2, y + LABEL_Y, LABEL_COLOR);

			super.renderForeground(re, sr, x, y, mouseX, mouseY, partialTicks);
		}

		@Override
		protected @Nullable String getSlotTooltipSuffix(SlotGuidebook slot) {
			return slot == this.filterSlot ? I18n.getInstance().translateKey("btx.guidebook.hopper.filter") : null;
		}
	}
}
