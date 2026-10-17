package ivangeevo.btx.recipe.turntable;

import net.minecraft.core.block.Block;
import net.minecraft.core.data.registry.recipe.RecipeGroup;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;

public class RecipeGroupTurntable extends RecipeGroup<RecipeEntryTurntable> {
	public RecipeGroupTurntable(RecipeSymbol machine) { super(machine); }

	public RecipeEntryTurntable findRecipe(WorldSource ws, TilePosc pos) {
		Block<?> block = ws.getBlockType(pos);
		if (block == null) return null;
		int data = ws.getBlockData(pos);
		for (RecipeEntryTurntable r : this.getAllRecipes()) {
			if (r.matchesBlock(block, data)) return r;
		}
		return null;
	}
}
