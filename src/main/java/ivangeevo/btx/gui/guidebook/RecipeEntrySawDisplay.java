package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.recipe.saw.RecipeEntrySaw;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.core.data.registry.recipe.RecipeEntryBase;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// guidebook-only view of one saw recipe, or of a family of saw recipes that only differ by paint color
@Environment(EnvType.CLIENT)
public class RecipeEntrySawDisplay extends RecipeEntryBase<List<RecipeEntrySaw>, List<ItemStack>, Void> {
	public record Frame(ItemStack input, List<ItemStack> outputs) {
	}

	private static final int NOTE_MAX_WIDTH = 150;

	private final List<Frame> frames = new ArrayList<>();
	private @Nullable List<String> noteLines = null;

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

	public List<String> getNoteLines() {
		if (this.noteLines == null) {
			this.noteLines = wrapNote(Minecraft.getMinecraft().font, buildNote(getRecipe()), NOTE_MAX_WIDTH);
		}
		return this.noteLines;
	}

	private static String buildNote(RecipeEntrySaw recipe) {
		I18n i18n = I18n.getInstance();
		List<String> notes = new ArrayList<>();

		switch (recipe.getFacing()) {
			case VERTICAL -> notes.add(i18n.translateKey("btx.guidebook.saw.vertical"));
			case HORIZONTAL -> notes.add(i18n.translateKey("btx.guidebook.saw.horizontal"));
			default -> {
			}
		}

		if (recipe.dropsBlockItems()) {
			notes.add(i18n.translateKey("btx.guidebook.saw.drops"));
		}

		if (recipe.getNote() != null) {
			notes.add(i18n.translateKey(recipe.getNote()));
		}

		return String.join(", ", notes);
	}

	private static List<String> wrapNote(FontRenderer font, String note, int maxWidth) {
		List<String> lines = new ArrayList<>();
		StringBuilder current = new StringBuilder();

		for (String word : note.split(" ")) {
			if (word.isEmpty()) {
				continue;
			}

			String candidate = current.isEmpty() ? word : current + " " + word;
			if (!current.isEmpty() && font.stringWidth(candidate) > maxWidth) {
				lines.add(current.toString());
				current = new StringBuilder(word);
			} else {
				current = new StringBuilder(candidate);
			}
		}

		if (!current.isEmpty()) {
			lines.add(current.toString());
		}
		return lines;
	}
}