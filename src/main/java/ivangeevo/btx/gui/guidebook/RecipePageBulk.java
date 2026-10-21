package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.recipe.cooking.RecipeEntryBulk;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.SlotGuidebook;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class RecipePageBulk extends RecipePageBTX<RecipeEntryBulk> {
	public static final int RECIPES_PER_PAGE = 4;
	private static final String FLAME_SPRITE = "btx:gui/guidebook/flame";
	private static final String STOKED_FLAME_SPRITE = "btx:gui/guidebook/stoked_flame";
	private static final int FIRST_RECIPE_Y = 8;
	private static final int RECIPE_SPACING = 46;
	private static final int INPUT_X = 14;
	private static final int INPUT_COLUMNS = 3;
	private static final int INPUT_SLOTS = 6;
	private static final int SLOT_SPACING = 18;
	private static final int ROW_Y = 1;
	private static final int ARROW_X = 71;
	private static final int FLAME_X = 75;
	private static final int FLAME_Y = 21;
	private static final int FLAME_SIZE = 14;
	private static final int OUTPUT_X = 99;
	private static final int OUTPUT_SPACING = 26;

	private final Predicate<RecipeEntryBulk> isStoked;

	public RecipePageBulk(GuidebookSection section, List<RecipeEntryBulk> recipes, Predicate<RecipeEntryBulk> isStoked) {
		super(section, recipes);
		this.isStoked = isStoked;

		int id = 0;

		for (int i = 0; i < recipes.size(); i++) {
			RecipeEntryBulk recipe = recipes.get(i);
			int top = recipeTop(i);
			List<RecipeSymbol> inputs = recipe.getInput();

			for (int slot = 0; slot < INPUT_SLOTS; slot++) {
				RecipeSymbol symbol = slot < inputs.size() ? inputs.get(slot) : null;
				this.slots.add(createInputSlot(id++, INPUT_X + slot % INPUT_COLUMNS * SLOT_SPACING, top + ROW_Y + slot / INPUT_COLUMNS * SLOT_SPACING, symbol, recipe));
			}

			List<ItemStack> outputs = mergeOutputs(recipe.getOutput());
			for (int slot = 0; slot < outputs.size(); slot++) {
				ItemStack output = outputs.get(slot);
				SlotGuidebook outputSlot = new SlotGuidebook(id++, OUTPUT_X + slot % 2 * OUTPUT_SPACING, top + ROW_Y + slot / 2 * OUTPUT_SPACING, new RecipeSymbol(output), false, recipe).setAsOutput();
				outputSlot.item = output;
				this.slots.add(outputSlot);
			}
		}
	}

	private static int recipeTop(int index) {
		return FIRST_RECIPE_Y + index * RECIPE_SPACING;
	}

	@Override
	protected void renderBackground(TextureManager re, int x, int y) {
		super.renderBackground(re, x, y);

		re.bindTexture(re.loadTexture(GUIDEBOOK_TEXTURE));

		for (int i = 0; i < this.recipes.size(); i++) {
			drawArrow(x + ARROW_X, y + recipeTop(i) + ROW_Y);
		}

		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		for (int i = 0; i < this.recipes.size(); i++) {
			String sprite = this.isStoked.test(this.recipes.get(i)) ? STOKED_FLAME_SPRITE : FLAME_SPRITE;
			this.drawGuiIcon(x + FLAME_X, y + recipeTop(i) + FLAME_Y, FLAME_SIZE, FLAME_SIZE, TextureRegistry.getTexture(sprite));
		}
	}

	@Override
	protected @Nullable String getExtraTooltip(int x, int y, int mouseX, int mouseY) {
		for (int i = 0; i < this.recipes.size(); i++) {
			if (isInside(mouseX, mouseY, x + FLAME_X, y + recipeTop(i) + FLAME_Y, FLAME_SIZE, FLAME_SIZE)) {
				String key = this.isStoked.test(this.recipes.get(i)) ? "btx.guidebook.cooking.stoked" : "btx.guidebook.cooking.fire";
				return I18n.getInstance().translateKey(key);
			}
		}

		return null;
	}
}
