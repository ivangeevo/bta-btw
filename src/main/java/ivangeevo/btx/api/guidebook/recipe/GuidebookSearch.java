package ivangeevo.btx.api.guidebook.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.RecipeEntryBase;
import net.minecraft.core.data.registry.recipe.RecipeGroup;
import net.minecraft.core.data.registry.recipe.RecipeNamespace;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Helpers for matching recipe contents against a guidebook {@link SearchQuery}.
 * <p>
 * Name queries compare display names; {@code #group} queries compare item id and metadata,
 * so copied stacks match too.
 */
@Environment(EnvType.CLIENT)
public final class GuidebookSearch {
	private GuidebookSearch() {}

	/** Whether the stack matches the query's name or {@code #group}. */
	public static boolean matchesStack(@Nullable ItemStack stack, SearchQuery query) {
		if (stack == null) {
			return false;
		}

		String term = query.query.getRight();

		if (query.query.getLeft() == SearchQuery.QueryType.NAME) {
			String name = stack.getDisplayName();
			return query.strict ? name.equalsIgnoreCase(term) : name.toLowerCase(Locale.ROOT).contains(term.toLowerCase(Locale.ROOT));
		}

		if (query.query.getLeft() == SearchQuery.QueryType.GROUP && term != null && !term.isEmpty()) {
			return isInGroup(stack, term);
		}

		return false;
	}

	/** Whether any stack the symbol can stand for matches the query. */
	public static boolean matchesSymbol(@Nullable RecipeSymbol symbol, SearchQuery query) {
		if (symbol == null) {
			return false;
		}

		List<ItemStack> stacks = symbol.resolve();
		if (stacks == null) {
			return false;
		}

		for (ItemStack stack : stacks) {
			if (matchesStack(stack, query)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Whether anything inside the value matches the query.
	 * Accepts an {@link ItemStack}, a {@link RecipeSymbol}, or an array, {@link Iterable} or {@link Map} values of them.
	 */
	public static boolean matchesAny(@Nullable Object value, SearchQuery query) {
		if (value instanceof ItemStack stack) {
			return matchesStack(stack, query);
		}

		if (value instanceof RecipeSymbol symbol) {
			return matchesSymbol(symbol, query);
		}

		if (value instanceof Object[] array) {
			for (Object element : array) {
				if (matchesAny(element, query)) {
					return true;
				}
			}
			return false;
		}

		if (value instanceof Iterable<?> iterable) {
			for (Object element : iterable) {
				if (matchesAny(element, query)) {
					return true;
				}
			}

			return false;
		}

		if (value instanceof Map<?, ?> map) {
			return matchesAny(map.values(), query);
		}

		return false;
	}

	/** Whether the recipe is inside the query's {@code @namespace} or {@code @namespace:group} scope. */
	public static boolean matchesScope(RecipeEntryBase<?, ?, ?> recipe, SearchQuery query) {
		return switch (query.scope.getLeft()) {
			case NONE -> true;
			case NAMESPACE -> {
				RecipeNamespace namespace = Registries.RECIPES.getItem(query.scope.getRight());
				yield namespace != null && recipe.parent != null && namespace == recipe.parent.getParent();
			}
			case NAMESPACE_GROUP -> {
				RecipeGroup<?> group;
				try {
					group = Registries.RECIPES.getGroupFromKey(query.scope.getRight());
				} catch (IllegalArgumentException e) {
					group = null;
				}
				yield group != null && group == recipe.parent;
			}
		};
	}

	/** Whether the stack is one of the items of the named item group. */
	public static boolean isInGroup(ItemStack stack, String group) {
		List<ItemStack> groupStacks;

		try {
			groupStacks = new RecipeSymbol(group).resolve();
		} catch (RuntimeException e) {
			return false;
		}

		if (groupStacks == null) {
			return false;
		}

		for (ItemStack groupStack : groupStacks) {
			if (groupStack != null && groupStack.itemID == stack.itemID
				&& (groupStack.getMetadata() == -1 || stack.getMetadata() == -1 || groupStack.getMetadata() == stack.getMetadata())) {
				return true;
			}
		}

		return false;
	}
}
