package ivangeevo.btx.recipe.soulforge;

import net.minecraft.core.data.registry.recipe.RecipeGroup;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.player.inventory.container.ContainerCrafting;
import org.jetbrains.annotations.Nullable;

public class RecipeGroupSoulforge extends RecipeGroup<RecipeEntrySoulforgeBase<?, ?>> {
	public RecipeGroupSoulforge(RecipeSymbol machine) {
		super(machine);
	}

	public @Nullable RecipeEntrySoulforgeBase<?, ?> findRecipe(ContainerCrafting grid) {
		for (RecipeEntrySoulforgeBase<?, ?> recipe : this.getAllRecipes()) {
			if (recipe.matches(grid)) {
				return recipe;
			}
		}

		return null;
	}
}
