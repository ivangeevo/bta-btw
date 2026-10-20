package ivangeevo.btx.recipe.hopper;

import net.minecraft.core.data.registry.recipe.RecipeGroup;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class RecipeGroupHopper extends RecipeGroup<RecipeEntryHopper> {
	public RecipeGroupHopper(RecipeSymbol machine) {
		super(machine);
	}

	public @Nullable RecipeEntryHopper findRecipe(@Nullable ItemStack input, @Nullable ItemStack filter) {
		if (input != null && filter != null) {
			for (RecipeEntryHopper recipe : this.getAllRecipes()) {
				if (recipe.matches(input, filter)) return recipe;
			}
		}

		return null;
	}
}
