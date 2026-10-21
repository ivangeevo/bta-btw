package ivangeevo.btx.recipe.soulforge;

import net.minecraft.core.data.registry.recipe.RecipeEntryBase;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerCrafting;
import org.jetbrains.annotations.Nullable;

public abstract class RecipeEntrySoulforgeBase<I, O> extends RecipeEntryBase<I, O, Void> {
	public static final int GRID_SIZE = 4;

	protected RecipeEntrySoulforgeBase(I input, O output) {
		super(input, output, null);
	}

	protected RecipeEntrySoulforgeBase() {
		super();
	}

	public abstract boolean matches(ContainerCrafting grid);

	public abstract @Nullable ItemStack getCraftingResult(ContainerCrafting grid);

	public void onCraftResult(ContainerCrafting grid) {
		for (int i = 0; i < grid.getContainerSize(); i++) {
			if (grid.getItem(i) != null) {
				grid.removeItem(i, 1);
			}
		}
	}
}