package ivangeevo.btx.recipe.impl;

import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.*;
import net.minecraft.core.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public abstract class RecipeEntryBTX extends RecipeEntryBase<RecipeSymbol, ItemStack, Void> {

	public RecipeEntryBTX(RecipeSymbol input, ItemStack output) {
		super(input, output, null);
	}

	public RecipeEntryBTX() {}

	public boolean matches(ItemStack stack) {
		return stack != null && this.getInput().matches(stack);
	}

	public boolean matchesQueryIgnoreExceptions(SearchQuery query) {
		try {
			return this.matchesQuery(query);
		} catch (IllegalArgumentException | NullPointerException exception) {
			return false;
		}
	}

	public boolean matchesQuery(@NotNull SearchQuery query) {
		boolean matches = switch (query.mode) {
			case ALL -> this.matchesRecipe(query) || this.matchesUsage(query);
			case RECIPE -> this.matchesRecipe(query);
			case USAGE -> this.matchesUsage(query);
		};

		return matches && this.matchesScope(query);
	}

	public boolean matchesScope(@NotNull SearchQuery query) {
		SearchQuery.SearchScope scope = query.scope.getLeft();

		if (scope == SearchQuery.SearchScope.NONE) {
			return true;
		}

		if (scope == SearchQuery.SearchScope.NAMESPACE) {
			RecipeNamespace namespace =
				Registries.RECIPES.getItem(query.scope.getRight());

			return namespace == this.parent.getParent();
		}

		if (scope == SearchQuery.SearchScope.NAMESPACE_GROUP) {
			RecipeGroup<?> group;

			try {
				group = Registries.RECIPES.getGroupFromKey(query.scope.getRight());
			} catch (IllegalArgumentException exception) {
				return false;
			}

			return group == this.parent;
		}

		return false;
	}

	public boolean matchesRecipe(@NotNull SearchQuery query) {
		if (query.query.getLeft() == SearchQuery.QueryType.NAME) {
			return matchesName(this.getOutput(), query);
		}

		if (query.query.getLeft() == SearchQuery.QueryType.GROUP
			&& !Objects.equals(query.query.getRight(), "")) {
			List<ItemStack> stacks =
				new RecipeSymbol(query.query.getRight()).resolve();

			return stacks != null && stacks.contains(this.getOutput());
		}

		return false;
	}

	public boolean matchesUsage(@NotNull SearchQuery query) {
		List<ItemStack> inputs = this.getInput().resolve();

		if (inputs == null) {
			return false;
		}

		if (query.query.getLeft() == SearchQuery.QueryType.GROUP
			&& !Objects.equals(query.query.getRight(), "")
		) {
			List<ItemStack> groupStacks =
				new RecipeSymbol(query.query.getRight()).resolve();

			if (groupStacks == null) {
				return false;
			}

			for (ItemStack input : inputs) {
				if (input != null && groupStacks.contains(input)) {
					return true;
				}
			}

			return false;
		}

		for (ItemStack input : inputs) {
			if (input != null && matchesName(input, query)) {
				return true;
			}
		}

		return false;
	}

	protected boolean matchesName(ItemStack stack, SearchQuery query) {
		if (query.query.getLeft() != SearchQuery.QueryType.NAME) {
			return false;
		}

		String name = stack.getDisplayName();
		String search = query.query.getRight();

		if (query.strict) {
			return name.equalsIgnoreCase(search);
		}

		return name.toLowerCase(Locale.ROOT)
			.contains(search.toLowerCase(Locale.ROOT));
	}

}
