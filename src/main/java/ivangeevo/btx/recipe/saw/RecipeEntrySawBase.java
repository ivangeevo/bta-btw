package ivangeevo.btx.recipe.saw;

import net.minecraft.core.block.Block;
import net.minecraft.core.data.registry.recipe.RecipeEntryBase;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.block.ItemBlock;
import net.minecraft.core.util.helper.Direction;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public abstract class RecipeEntrySawBase<I, O> extends RecipeEntryBase<I, O, Void> {
	protected RecipeEntrySawBase(I input, O output) {
		super(input, output, null);
	}

	protected RecipeEntrySawBase() {
		super();
	}

	public abstract boolean matches(Block<?> block, int data, Direction sawFacing);

	// BTA's registry keeps recipes sorted by key, so overlapping entries are resolved by specificity instead of order
	public int getSpecificity() {
		return 0;
	}

	protected static Set<Block<?>> resolveBlocks(List<RecipeSymbol> symbols) {
		Set<Block<?>> blocks = Collections.newSetFromMap(new IdentityHashMap<>());
		for (RecipeSymbol symbol : symbols) {
			List<ItemStack> stacks = symbol.resolve();
			if (stacks == null) {
				continue;
			}
			for (ItemStack stack : stacks) {
				if (stack != null && stack.getItem() instanceof ItemBlock<?> itemBlock) {
					blocks.add(itemBlock.getBlock());
				}
			}
		}
		return blocks;
	}
}