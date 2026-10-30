package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.impl.RecipeEntryMillstone;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.SlotGuidebook;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class GuidebookSectionMillstone extends GuidebookSectionBTX<RecipeEntryMillstone> {
	public GuidebookSectionMillstone() {
		super("btx.guidebook.section.millstone", new ItemStack(BTXBlocks.MILLSTONE), 0x606060, 0x00A29C);
	}

	@Override
	protected List<RecipeEntryMillstone> getRecipes() {
		return BTXRecipes.MILLSTONE == null ? new ArrayList<>() : new ArrayList<>(BTXRecipes.MILLSTONE.getAllRecipes());
	}

	@Override
	protected boolean matchesQuery(RecipeEntryMillstone recipe, SearchQuery query) {
		return recipe.matchesQueryIgnoreExceptions(query);
	}

	@Override
	protected int getRecipesPerPage() {
		return RecipePage.RECIPES_PER_PAGE;
	}

	@Override
	protected GuidebookPage createPage(List<RecipeEntryMillstone> recipes) {
		return new RecipePage(this, recipes);
	}

	@Environment(EnvType.CLIENT)
	protected static class RecipePage extends RecipePageBTX<RecipeEntryMillstone> {
		public static final int RECIPES_PER_PAGE = 4;
		private static final String GEAR_SPRITE = "btx:gui/guidebook/gear";
		private static final int FIRST_RECIPE_Y = 8;
		private static final int RECIPE_SPACING = 46;
		private static final int INPUT_X = 27;
		private static final int ROW_Y = 1;
		private static final int ARROW_X = 52;
		private static final int GEAR_X = 56;
		private static final int GEAR_Y = 21;
		private static final int GEAR_SIZE = 13;
		private static final int OUTPUT_X = 84;
		private static final int OUTPUT_SPACING = 26;

		public RecipePage(GuidebookSection section, List<RecipeEntryMillstone> recipes) {
			super(section, recipes);

			int id = 0;

			for (int i = 0; i < recipes.size(); i++) {
				RecipeEntryMillstone recipe = recipes.get(i);
				int top = recipeTop(i);

				this.slots.add(new SlotGuidebook(id++, INPUT_X, top + ROW_Y, recipe.getInput(), false, recipe));

				List<ItemStack> outputs = mergeOutputs(recipe.getOutputs());

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
				this.drawGuiIcon(x + GEAR_X, y + recipeTop(i) + GEAR_Y, GEAR_SIZE, GEAR_SIZE, TextureRegistry.getTexture(GEAR_SPRITE));
			}
		}

		@Override
		protected @Nullable String getExtraTooltip(int x, int y, int mouseX, int mouseY) {
			for (int i = 0; i < this.recipes.size(); i++) {
				if (isInside(mouseX, mouseY, x + GEAR_X, y + recipeTop(i) + GEAR_Y, GEAR_SIZE, GEAR_SIZE)) {
					return I18n.getInstance().translateKey("btx.guidebook.millstone.mechanical");
				}
			}

			return null;
		}
	}
}
