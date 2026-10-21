package ivangeevo.btx.gui.guidebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.SearchableGuidebookSection;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.RecipeEntryBase;
import net.minecraft.core.data.registry.recipe.RecipeGroup;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
			} catch (NullPointerException | IllegalArgumentException ignored) {
			}
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
}