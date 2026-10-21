package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.recipe.saw.RecipeEntrySaw;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.data.registry.recipe.RecipeEntryBase;
import net.minecraft.core.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

// guidebook-only view of one saw recipe, or of a family of saw recipes that only differ by paint color
@Environment(EnvType.CLIENT)
public class RecipeEntrySawDisplay extends RecipeEntryBase<List<RecipeEntrySaw>, List<ItemStack>, Void> {
	public record Frame(ItemStack input, List<ItemStack> outputs) {
	}

	private final List<Frame> frames = new ArrayList<>();

	public RecipeEntrySawDisplay(List<RecipeEntrySaw> variants) {
		super(variants, List.of(), null);
		this.parent = variants.get(0).parent;

		if (variants.size() > 1) {
			for (RecipeEntrySaw variant : variants) {
				List<ItemStack> inputs = variant.getInput().resolve();
				if (inputs != null && !inputs.isEmpty()) {
					this.frames.add(new Frame(displayStack(inputs.get(0), variant.getDisplayMeta()), variant.getOutput()));
				}
			}
		} else {
			RecipeEntrySaw recipe = variants.get(0);
			List<ItemStack> inputs = recipe.getInput().resolve();
			if (inputs != null) {
				for (ItemStack input : inputs) {
					int meta = inputs.size() == 1 ? recipe.getDisplayMeta() : input.getMetadata();
					this.frames.add(new Frame(displayStack(input, meta), recipe.getOutput()));
				}
			}
		}
	}

	private static ItemStack displayStack(ItemStack stack, int meta) {
		ItemStack copy = stack.copy();
		copy.stackSize = 1;
		copy.setMetadata(meta);
		return copy;
	}

	public RecipeEntrySaw getRecipe() {
		return getInput().get(0);
	}

	public List<Frame> getFrames() {
		return this.frames;
	}
}