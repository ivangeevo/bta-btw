package ivangeevo.btx.api.guidebook.recipe;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.SearchableGuidebookSection;
import net.minecraft.core.data.registry.recipe.RecipeEntryBase;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A tab that shows recipes a few per page and supports search, R and U lookups.
 * <p>
 * Implement {@link #getRecipes()}, {@link #getRecipesPerPage()} and {@link #createPage(List)}.
 * By default a recipe matches R lookups through its output and U lookups through its input;
 * override {@link #matchesRecipe} or {@link #matchesUsage} for recipes with other contents.
 * Pages are cached and rebuilt when the number of recipes changes.
 *
 * @param <T> the recipe type
 */
@Environment(EnvType.CLIENT)
public abstract class RecipeGuidebookSection<T extends RecipeEntryBase<?, ?, ?>> extends SearchableGuidebookSection {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final List<GuidebookPage> pages = new ArrayList<>();
	private int cachedRecipeCount = -1;
	private String cachedQuery = null;
	private List<GuidebookPage> cachedSearchPages = null;
	private boolean loggedError = false;

	public RecipeGuidebookSection(String translationKey, ItemStack tabIcon, int bgColor, int fgColor) {
		super(translationKey, tabIcon, bgColor, fgColor);
	}

	/** The recipes to show, in display order. Called often, so keep it cheap. */
	protected abstract List<T> getRecipes();

	/** How many recipes fit on one page. */
	protected abstract int getRecipesPerPage();

	/** Creates one page for up to {@link #getRecipesPerPage()} recipes. */
	protected abstract GuidebookPage createPage(List<T> recipes);

	/** Whether pressing R over an item should show this recipe. Defaults to checking the output. */
	protected boolean matchesRecipe(T recipe, SearchQuery query) {
		return GuidebookSearch.matchesAny(recipe.getOutput(), query);
	}

	/** Whether pressing U over an item should show this recipe. Defaults to checking the input. */
	protected boolean matchesUsage(T recipe, SearchQuery query) {
		return GuidebookSearch.matchesAny(recipe.getInput(), query);
	}

	/** Whether the recipe matches the query's mode and scope. Usually {@link #matchesRecipe} and {@link #matchesUsage} are enough. */
	protected boolean matchesQuery(T recipe, SearchQuery query) {
		boolean matches = switch (query.mode) {
			case ALL -> matchesRecipe(recipe, query) || matchesUsage(recipe, query);
			case RECIPE -> matchesRecipe(recipe, query);
			case USAGE -> matchesUsage(recipe, query);
		};

		return matches && GuidebookSearch.matchesScope(recipe, query);
	}

	/** Splits the recipes into pages. Override to group recipes differently. */
	protected List<List<T>> splitIntoPages(List<T> recipes) {
		int perPage = Math.max(1, getRecipesPerPage());
		List<List<T>> split = new ArrayList<>();

		for (int i = 0; i < recipes.size(); i += perPage) {
			split.add(new ArrayList<>(recipes.subList(i, Math.min(i + perPage, recipes.size()))));
		}

		return split;
	}

	/** Drops the cached pages so they are rebuilt next time, e.g. after the recipes changed without changing their count. */
	public void invalidate() {
		this.cachedRecipeCount = -1;
		this.cachedQuery = null;
		this.cachedSearchPages = null;
	}

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
		List<GuidebookPage> all = getPages();
		if (query.rawQuery == null || query.rawQuery.trim().isEmpty()) {
			return all;
		}

		if (this.cachedSearchPages != null && Objects.equals(this.cachedQuery, query.rawQuery)) {
			return this.cachedSearchPages;
		}

		List<T> filtered = new ArrayList<>();
		for (T recipe : getRecipes()) {
			try {
				if (matchesQuery(recipe, query)) {
					filtered.add(recipe);
				}
			} catch (RuntimeException e) {
				if (!this.loggedError) {
					this.loggedError = true;
					LOGGER.error("Guidebook section {} failed to search a recipe", getTranslationKey(), e);
				}
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
}
