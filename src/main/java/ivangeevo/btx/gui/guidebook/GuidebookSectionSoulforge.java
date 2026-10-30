package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.recipe.BTXItemGroups;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.soulforge.RecipeEntrySoulforge;
import ivangeevo.btx.recipe.soulforge.RecipeEntrySoulforgeBase;
import ivangeevo.btx.recipe.soulforge.RecipeEntrySoulforgeRepair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.SlotGuidebook;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.core.data.registry.recipe.RecipeEntryBase;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class GuidebookSectionSoulforge extends GuidebookSectionBTX<RecipeEntryBase<?, ?, ?>> {
	private final Map<RecipeEntrySoulforgeRepair, List<RepairDisplay>> repairDisplays = new IdentityHashMap<>();

	public GuidebookSectionSoulforge() {
		super("btx.guidebook.section.soulforge", new ItemStack(BTXBlocks.SOULFORGE), 0x3A2E2E, 0x8B1A1A);
	}

	@Override
	protected List<RecipeEntryBase<?, ?, ?>> getRecipes() {
		List<RecipeEntryBase<?, ?, ?>> recipes = new ArrayList<>();
		if (BTXRecipes.SOULFORGE == null) {
			return recipes;
		}
		for (RecipeEntrySoulforgeBase<?, ?> recipe : BTXRecipes.SOULFORGE.getAllRecipes()) {
			if (recipe instanceof RecipeEntrySoulforge shaped) {
				recipes.add(shaped);
			} else if (recipe instanceof RecipeEntrySoulforgeRepair repair) {
				recipes.addAll(this.repairDisplays.computeIfAbsent(repair, source -> List.of(
					new RepairDisplay(source, BTXItemGroups.SOULFORGED_TOOLS),
					new RepairDisplay(source, BTXItemGroups.SOULFORGED_ARMOR)))
				);
			}
		}
		return recipes;
	}

	@Override
	protected int getRecipesPerPage() {
		return RecipePage.RECIPES_PER_PAGE;
	}

	@Override
	protected GuidebookPage createPage(List<RecipeEntryBase<?, ?, ?>> recipes) {
		return new RecipePage(this, recipes);
	}

	@Override
	protected boolean matchesQuery(RecipeEntryBase<?, ?, ?> recipe, SearchQuery query) {
		boolean matches = switch (query.mode) {
			case ALL -> matchesRecipe(recipe, query) || matchesUsage(recipe, query);
			case RECIPE -> matchesRecipe(recipe, query);
			case USAGE -> matchesUsage(recipe, query);
		};
		return matches && matchesScope(recipe, query);
	}

	private static boolean matchesRecipe(RecipeEntryBase<?, ?, ?> recipe, SearchQuery query) {
		if (recipe instanceof RecipeEntrySoulforge shaped) {
			return matchesStack(shaped.getOutput(), query);
		}
		if (recipe instanceof RepairDisplay repair) {
			return matchesSymbol(repair.getOutput(), query);
		}
		return false;
	}

	private static boolean matchesUsage(RecipeEntryBase<?, ?, ?> recipe, SearchQuery query) {
		if (recipe instanceof RepairDisplay repair) {
			return matchesSymbol(repair.getInput(), query) || matchesSymbol(repair.getIngot(), query);
		}
		if (!(recipe instanceof RecipeEntrySoulforge shaped)) {
			return false;
		}
		for (int y = 0; y < shaped.getHeight(); y++) {
			for (int x = 0; x < shaped.getWidth(); x++) {
				if (matchesSymbol(shaped.getSymbol(x, y), query)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean matchesSymbol(RecipeSymbol symbol, SearchQuery query) {
		if (symbol == null) {
			return false;
		}
		for (ItemStack stack : symbol.resolve()) {
			if (matchesStack(stack, query)) {
				return true;
			}
		}
		return false;
	}

	@Environment(EnvType.CLIENT)
	protected static class RecipePage extends RecipePageBTX<RecipeEntryBase<?, ?, ?>> {
		public static final int RECIPES_PER_PAGE = 2;
		private static final int GRID = RecipeEntrySoulforgeBase.GRID_SIZE;
		private static final int SLOT_SPACING = 18;
		private static final int GRID_X = 14;
		private static final int FIRST_GRID_Y = 8;
		private static final int RECIPE_SPACING = 96;
		private static final int OUTPUT_X = GRID_X + GRID * SLOT_SPACING + 34;
		private static final int REPAIR_INGOTS = 2;
		private static final int REPAIR_TEXT_Y = GRID * SLOT_SPACING + 2;
		private static final int TEXT_COLOR = 0x404040;

		private final List<SlotGuidebook> outputSlots = new ArrayList<>();
		private final List<RecipePage.RepairSlots> repairSlots = new ArrayList<>();

		public RecipePage(GuidebookSection section, List<RecipeEntryBase<?, ?, ?>> recipes) {
			super(section, recipes);

			int id = 0;

			for (int i = 0; i < recipes.size(); i++) {
				RecipeEntryBase<?, ?, ?> recipe = recipes.get(i);
				int gridY = FIRST_GRID_Y + i * RECIPE_SPACING;
				int outputY = gridY + (GRID * SLOT_SPACING - SLOT_SPACING) / 2;

				if (recipe instanceof RepairDisplay repair) {
					SlotGuidebook input = null;

					for (int y = 0; y < GRID; y++) {
						for (int x = 0; x < GRID; x++) {
							RecipeSymbol symbol = repairSymbolAt(repair, x, y);
							SlotGuidebook slot = new SlotGuidebook(id++, GRID_X + x * SLOT_SPACING, gridY + y * SLOT_SPACING, symbol, false, recipe);

							this.slots.add(slot);

							if (symbol == repair.getInput()) {
								input = slot;
							}
						}
					}

					SlotGuidebook output = new SlotGuidebook(id++, OUTPUT_X, outputY, repair.getOutput(), false, recipe).setAsOutput();

					this.slots.add(output);
					this.outputSlots.add(output);
					this.repairSlots.add(new RecipePage.RepairSlots(input, output, gridY));
				} else if (recipe instanceof RecipeEntrySoulforge shaped) {
					for (int y = 0; y < GRID; y++) {
						for (int x = 0; x < GRID; x++) {
							RecipeSymbol symbol = shaped.getSymbol(x, y);
							this.slots.add(new SlotGuidebook(id++, GRID_X + x * SLOT_SPACING, gridY + y * SLOT_SPACING, symbol, false, recipe));
						}
					}

					SlotGuidebook output = new SlotGuidebook(id++, OUTPUT_X, outputY, new RecipeSymbol(shaped.getOutput()), false, recipe).setAsOutput();

					this.slots.add(output);
					this.outputSlots.add(output);
				}
			}

			syncRepairSlots();
		}

		private static RecipeSymbol repairSymbolAt(RepairDisplay repair, int x, int y) {
			if (x == 1 && y == 1) {
				return repair.getInput();
			}

			if ((x == 2 && y == 1) || (x == 1 && y == 2)) {
				return repair.getIngot();
			}

			return null;
		}

		@Override
		public void onTick() {
			super.onTick();
			syncRepairSlots();
		}

		@Override
		public void onBecomeVisible() {
			super.onBecomeVisible();
			syncRepairSlots();
		}

		private void syncRepairSlots() {
			for (RecipePage.RepairSlots repair : this.repairSlots) {
				ItemStack shown = repair.input.item;

				if (shown == null || shown == repair.damagedInput) {
					continue;
				}

				ItemStack damaged = shown.copy();

				damaged.setMetadata(damaged.getMaxDamage() * REPAIR_INGOTS / RecipeEntrySoulforgeRepair.MAX_INGOTS);
				repair.damagedInput = damaged;
				repair.input.item = damaged;
				repair.output.item = shown.copy();
			}
		}

		@Override
		protected void renderBackground(TextureManager re, int x, int y) {
			super.renderBackground(re, x, y);

			re.bindTexture(re.loadTexture(GUIDEBOOK_TEXTURE));

			for (SlotGuidebook output : this.outputSlots) {
				drawArrow(x + output.x - 25, y + output.y);
			}
		}

		@Override
		protected void renderForeground(TextureManager re, FontRenderer sr, int x, int y, int mouseX, int mouseY, float partialTicks) {
			super.renderForeground(re, sr, x, y, mouseX, mouseY, partialTicks);

			String text = I18n.getInstance().translateKey("btx.guidebook.soulforge.repair");

			for (RecipePage.RepairSlots repair : this.repairSlots) {
				this.drawStringNoShadow(sr, text, x + GRID_X, y + repair.gridY + REPAIR_TEXT_Y, TEXT_COLOR);
			}
		}

		private static final class RepairSlots {
			private final SlotGuidebook input;
			private final SlotGuidebook output;
			private final int gridY;
			private ItemStack damagedInput;

			private RepairSlots(SlotGuidebook input, SlotGuidebook output, int gridY) {
				this.input = input;
				this.output = output;
				this.gridY = gridY;
			}
		}
	}

	// guidebook-only view of the repair recipe for one item group; it is never registered as a recipe
	@Environment(EnvType.CLIENT)
	protected static class RepairDisplay extends RecipeEntryBase<RecipeSymbol, RecipeSymbol, Void> {
		private static final RecipeSymbol INGOT = new RecipeSymbol(new ItemStack(BTXItems.SOULFORGED_STEEL_INGOT));

		public RepairDisplay(RecipeEntrySoulforgeRepair source, String itemGroup) {
			super(new RecipeSymbol(itemGroup), new RecipeSymbol(itemGroup), null);
			this.parent = source.parent;
		}

		public RecipeSymbol getIngot() {
			return INGOT;
		}
	}
}
