package ivangeevo.btx.recipe.hopper;

import net.minecraft.core.data.registry.recipe.RecipeGroup;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class RecipeGroupHopperFilter extends RecipeGroup<RecipeEntryHopperFilter> {
	public RecipeGroupHopperFilter(RecipeSymbol machine) {
		super(machine);
	}

	public @Nullable RecipeEntryHopperFilter findRecipe(@Nullable ItemStack filter) {
		if (filter != null) {
			for (RecipeEntryHopperFilter recipe : this.getAllRecipes()) {
				if (recipe.matches(filter)) return recipe;
			}
		}

		return null;
	}

	public boolean isFilter(@Nullable ItemStack stack) {
		return findRecipe(stack) != null;
	}

	public boolean canFilterProcess(@Nullable ItemStack filter, ItemStack stack) {
		RecipeEntryHopperFilter recipe = findRecipe(filter);

		return recipe == null || recipe.canProcess(stack);
	}

	public @Nullable RecipeEntryHopperFilter findByName(String name) {
		for (RecipeEntryHopperFilter recipe : this.getAllRecipes()) {
			if (recipe.toString().equals(name)) return recipe;
		}

		return null;
	}
}
