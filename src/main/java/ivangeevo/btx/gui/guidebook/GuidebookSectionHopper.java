package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.hopper.RecipeEntryHopper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class GuidebookSectionHopper extends GuidebookSectionBTX<RecipeEntryHopper> {
	public GuidebookSectionHopper() {
		super("btx.guidebook.section.hopper", new ItemStack(BTXBlocks.HOPPER), 0x4A3A28, 0xB08850);
	}

	@Override
	protected List<RecipeEntryHopper> getRecipes() {
		return BTXRecipes.HOPPER == null ? new ArrayList<>() : new ArrayList<>(BTXRecipes.HOPPER.getAllRecipes());
	}

	@Override
	protected boolean matchesQuery(RecipeEntryHopper recipe, SearchQuery query) {
		return recipe.matchesQueryIgnoreExceptions(query);
	}

	@Override
	protected int getRecipesPerPage() {
		return RecipePage.RECIPES_PER_PAGE;
	}

	@Override
	protected GuidebookPage createPage(List<RecipeEntryHopper> recipes) {
		return new RecipePage(this, recipes);
	}

	@Environment(EnvType.CLIENT)
	public static class RecipePage extends RecipePageBTX<RecipeEntryHopper> {
		public static final int RECIPES_PER_PAGE = 3;
		private static final String GEAR_SPRITE = "btx:gui/guidebook/gear";
		private static final int FIRST_RECIPE_Y = 10;
		private static final int RECIPE_SPACING = 68;
		private static final int INPUT_X = 14;
		private static final int INPUT_Y = 17;
		private static final int FIRST_ARROW_X = 40;
		private static final int SECOND_ARROW_X = 96;
		private static final int ARROW_Y = 18;
		private static final int HOPPER_X = 71;
		private static final int FILTER_Y = 8;
		private static final int HOPPER_Y = 26;
		private static final int OUTPUT_X = 126;
		private static final int FILTERED_OUTPUT_Y = 8;
		private static final int HOPPER_OUTPUT_Y = 28;
		private static final int GEAR_X = 72;
		private static final int GEAR_Y = 45;
		private static final int GEAR_SIZE = 13;

		private final Map<SlotGuidebook, String> tooltipSuffixes = new HashMap<>();

		public RecipePage(GuidebookSection section, List<RecipeEntryHopper> recipes) {
			super(section, recipes);

			I18n i18n = I18n.getInstance();
			int id = 0;

			for (int i = 0; i < recipes.size(); i++) {
				RecipeEntryHopper recipe = recipes.get(i);
				int top = recipeTop(i);

				this.slots.add(createInputSlot(id++, INPUT_X, top + INPUT_Y, recipe.getInput(), recipe));

				SlotGuidebook filter = createInputSlot(id++, HOPPER_X, top + FILTER_Y, recipe.getFilter(), recipe);
				this.slots.add(filter);
				this.tooltipSuffixes.put(filter, i18n.translateKey("btx.guidebook.hopper.filter"));

				SlotGuidebook hopper = new SlotGuidebook(id++, HOPPER_X, top + HOPPER_Y, new RecipeSymbol(new ItemStack(BTXBlocks.HOPPER)), false, recipe);
				this.slots.add(hopper);
				this.backgroundlessSlots.add(hopper);

				ItemStack filteredOutput = recipe.getFilteredOutput();
				if (filteredOutput != null) {
					id = addOutput(id, OUTPUT_X, top + FILTERED_OUTPUT_Y, filteredOutput, recipe, i18n.translateKey("btx.guidebook.hopper.filtered_output"));
				}

				ItemStack hopperOutput = recipe.getHopperOutput();
				if (hopperOutput != null) {
					id = addOutput(id, OUTPUT_X, top + HOPPER_OUTPUT_Y, hopperOutput, recipe, i18n.translateKey("btx.guidebook.hopper.hopper_output"));
				}
			}
		}

		private int addOutput(int id, int x, int y, ItemStack output, RecipeEntryHopper recipe, String suffix) {
			SlotGuidebook slot = new SlotGuidebook(id, x, y, new RecipeSymbol(output), false, recipe).setAsOutput();
			slot.item = output;
			this.slots.add(slot);
			this.tooltipSuffixes.put(slot, suffix);
			return id + 1;
		}

		private static int recipeTop(int index) {
			return FIRST_RECIPE_Y + index * RECIPE_SPACING;
		}

		@Override
		protected void renderBackground(TextureManager re, int x, int y) {
			super.renderBackground(re, x, y);

			re.bindTexture(re.loadTexture(GUIDEBOOK_TEXTURE));

			for (int i = 0; i < this.recipes.size(); i++) {
				drawArrow(x + FIRST_ARROW_X, y + recipeTop(i) + ARROW_Y);
				drawArrow(x + SECOND_ARROW_X, y + recipeTop(i) + ARROW_Y);
			}

			GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			for (int i = 0; i < this.recipes.size(); i++) {
				if (this.recipes.get(i).containsSouls()) {
					this.drawGuiIcon(x + GEAR_X, y + recipeTop(i) + GEAR_Y, GEAR_SIZE, GEAR_SIZE, TextureRegistry.getTexture(GEAR_SPRITE));
				}
			}
		}

		@Override
		protected @Nullable String getSlotTooltipSuffix(SlotGuidebook slot) {
			return this.tooltipSuffixes.get(slot);
		}

		@Override
		protected @Nullable String getExtraTooltip(int x, int y, int mouseX, int mouseY) {
			for (int i = 0; i < this.recipes.size(); i++) {
				if (this.recipes.get(i).containsSouls() && isInside(mouseX, mouseY, x + GEAR_X, y + recipeTop(i) + GEAR_Y, GEAR_SIZE, GEAR_SIZE)) {
					return I18n.getInstance().translateKey("btx.guidebook.hopper.souls");
				}
			}

			return null;
		}
	}
}
