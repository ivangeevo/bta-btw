package ivangeevo.btx.recipe.kiln;

import net.minecraft.core.block.Block;
import net.minecraft.core.data.registry.recipe.RecipeGroup;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;

public class RecipeGroupKiln extends RecipeGroup<RecipeEntryKiln> {

	public RecipeGroupKiln(RecipeSymbol machine) {
		super(machine);
	}

	public RecipeEntryKiln findRecipe(ItemStack input) {
		if (input != null) {
			for (RecipeEntryKiln recipe : this.getAllRecipes()) {
				if (recipe.matches(input)) return recipe;
			}
		}
		return null;
	}

	public RecipeEntryKiln findRecipe(WorldSource ws, TilePosc pos) {
		Block<?> block = ws.getBlockType(pos);
		return findRecipe(new ItemStack(block, 1, ws.getBlockData(pos)));
	}

	/** What the kiln calls for the block above it. */
	public RecipeEntryKiln findRecipe(Block<?> block) {
		return block == null ? null : findRecipe(block.getDefaultStack());
	}

	public boolean canCook(Block<?> block) {
		return findRecipe(block) != null;
	}
}
