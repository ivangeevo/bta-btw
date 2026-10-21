package ivangeevo.btx.recipe.saw;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.data.registry.recipe.RecipeGroup;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RecipeGroupSaw extends RecipeGroup<RecipeEntrySawBase<?, ?>> {
	public RecipeGroupSaw(RecipeSymbol machine) {
		super(machine);
	}

	public @Nullable RecipeEntrySawBase<?, ?> findRecipe(WorldSource source, TilePosc pos, Direction sawFacing) {
		Block<?> block = source.getBlockType(pos);

		if (block == null || block == Blocks.AIR) {
			return null;
		}

		int data = source.getBlockData(pos);
		RecipeEntrySawBase<?, ?> best = null;

		for (RecipeEntrySawBase<?, ?> recipe : this.getAllRecipes()) {
			if (recipe.matches(block, data, sawFacing) && (best == null || recipe.getSpecificity() > best.getSpecificity())) {
				best = recipe;
			}
		}

		return best;
	}

	public List<RecipeEntrySaw> getCutRecipes() {
		List<RecipeEntrySaw> cuts = new ArrayList<>();
		for (RecipeEntrySawBase<?, ?> recipe : this.getAllRecipes()) {
			if (recipe instanceof RecipeEntrySaw cut) {
				cuts.add(cut);
			}
		}

		return cuts;
	}
}
