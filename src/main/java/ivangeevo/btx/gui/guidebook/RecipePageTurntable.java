package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.recipe.turntable.RecipeEntryTurntable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.SlotGuidebook;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class RecipePageTurntable extends RecipePageBTX<RecipeEntryTurntable> {
	public static final int RECIPES_PER_PAGE = 3;
	private static final int ORIGIN_X = 24;
	private static final int FIRST_RECIPE_Y = 8;
	private static final int RECIPE_SPACING = 60;
	private static final int INPUT_X = 11;
	private static final int TURNTABLE_Y = 21;
	private static final int ARROW_X = 34;
	private static final int ARROW_Y = 12;
	private static final int OUTPUT_X = 64;
	private static final int OUTPUT_Y = 11;
	private static final int EJECTED_X = 84;
	private static final int EJECTED_SLOTS = 2;
	private static final int SLOT_SPACING = 18;
	private static final int TEXT_X = 2;
	private static final int TEXT_Y = 41;
	private static final int TEXT_COLOR = 0x404040;

	private final Set<SlotGuidebook> ejectedSlots = new HashSet<>();

	public RecipePageTurntable(GuidebookSection section, List<RecipeEntryTurntable> recipes) {
		super(section, recipes);

		int id = 0;

		for (int i = 0; i < recipes.size(); i++) {
			RecipeEntryTurntable recipe = recipes.get(i);
			int top = recipeTop(i);

			this.slots.add(createInputSlot(id++, ORIGIN_X + INPUT_X, top + 1, recipe.getInput(), recipe));

			SlotGuidebook turntable = new SlotGuidebook(id++, ORIGIN_X + INPUT_X, top + TURNTABLE_Y, new RecipeSymbol(new ItemStack(BTXBlocks.TURNTABLE)), true, recipe);

			this.slots.add(turntable);
			this.backgroundlessSlots.add(turntable);

			ItemStack output = recipe.getOutput();
			RecipeSymbol outputSymbol = isAir(output) ? null : new RecipeSymbol(output);

			this.slots.add(new SlotGuidebook(id++, ORIGIN_X + OUTPUT_X, top + OUTPUT_Y, outputSymbol, false, recipe).setAsOutput());

			List<ItemStack> ejected = mergeOutputs(recipe.getExtraOutputs());

			for (int slot = 0; slot < EJECTED_SLOTS; slot++) {
				int slotY = top + 1 + slot * SLOT_SPACING;

				if (slot < ejected.size()) {
					ItemStack stack = ejected.get(slot);
					SlotGuidebook ejectedSlot = new SlotGuidebook(id++, ORIGIN_X + EJECTED_X, slotY, new RecipeSymbol(stack), false, recipe).setAsOutput();
					ejectedSlot.item = stack;
					this.slots.add(ejectedSlot);
					this.ejectedSlots.add(ejectedSlot);
				} else {
					this.slots.add(new SlotGuidebook(id++, ORIGIN_X + EJECTED_X, slotY, null, false, recipe));
				}
			}
		}
	}

	private static boolean isAir(ItemStack stack) {
		return stack == null || stack.getItem() == null || stack.getItem() == Blocks.AIR.asItem();
	}

	private static int recipeTop(int index) {
		return FIRST_RECIPE_Y + index * RECIPE_SPACING;
	}

	@Override
	protected void renderBackground(TextureManager re, int x, int y) {
		super.renderBackground(re, x, y);

		re.bindTexture(re.loadTexture(GUIDEBOOK_TEXTURE));

		for (int i = 0; i < this.recipes.size(); i++) {
			drawArrow(x + ORIGIN_X + ARROW_X, y + recipeTop(i) + ARROW_Y);
		}
	}

	@Override
	protected void renderForeground(TextureManager re, FontRenderer sr, int x, int y, int mouseX, int mouseY, float partialTicks) {
		super.renderForeground(re, sr, x, y, mouseX, mouseY, partialTicks);

		I18n i18n = I18n.getInstance();

		for (int i = 0; i < this.recipes.size(); i++) {
			String text = i18n.translateKeyAndFormat("btx.guidebook.turntable.turns", this.recipes.get(i).getRotations());
			this.drawStringNoShadow(sr, text, x + ORIGIN_X + TEXT_X, y + recipeTop(i) + TEXT_Y, TEXT_COLOR);
		}
	}

	@Override
	protected @Nullable String getSlotTooltipSuffix(SlotGuidebook slot) {
		return this.ejectedSlots.contains(slot) ? I18n.getInstance().translateKey("btx.guidebook.turntable.ejected_items") : null;
	}
}
