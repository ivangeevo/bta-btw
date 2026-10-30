package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.saw.RecipeEntrySaw;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.SlotGuidebook;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.core.data.registry.recipe.RecipeEntryBase;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.helper.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class GuidebookSectionSaw extends GuidebookSectionBTX<GuidebookSectionSaw.RecipeEntryDisplay> {
	private static final Pattern COLOR_TOKEN = buildColorPattern();
	private static final String DOUBLE_SLAB_NOTE = "btx.guidebook.saw.double_slab";

	private int cachedRecipeCount = -1;
	private List<RecipeEntryDisplay> displays = new ArrayList<>();

	public GuidebookSectionSaw() {
		super("btx.guidebook.section.saw", new ItemStack(BTXBlocks.SAW), 0x8B6A3E, 0xD0D0D0);
	}

	private static Pattern buildColorPattern() {
		StringBuilder colors = new StringBuilder();
		for (DyeColor color : DyeColor.values()) {
			if (!colors.isEmpty()) {
				colors.append('|');
			}
			colors.append(Pattern.quote(color.colorID));
		}

		return Pattern.compile("_(" + colors + ")_");
	}

	@Override
	protected List<RecipeEntryDisplay> getRecipes() {
		if (BTXRecipes.SAW == null) {
			return new ArrayList<>();
		}

		List<RecipeEntrySaw> cuts = BTXRecipes.SAW.getCutRecipes();
		if (cuts.size() != this.cachedRecipeCount) {
			this.cachedRecipeCount = cuts.size();

			Map<String, List<RecipeEntrySaw>> families = new LinkedHashMap<>();
			for (RecipeEntrySaw cut : cuts) {
				if (cut.getOutput().isEmpty() || DOUBLE_SLAB_NOTE.equals(cut.getNote())) {
					continue;
				}

				String family = COLOR_TOKEN.matcher(String.valueOf(cut)).replaceFirst("_*_");
				families.computeIfAbsent(family, key -> new ArrayList<>()).add(cut);
			}

			List<RecipeEntryDisplay> built = new ArrayList<>();
			for (List<RecipeEntrySaw> family : families.values()) {
				RecipeEntryDisplay display = new RecipeEntryDisplay(family);
				if (!display.getFrames().isEmpty()) {
					built.add(display);
				}
			}

			this.displays = built;
		}

		return this.displays;
	}

	@Override
	protected boolean matchesQuery(RecipeEntryDisplay recipe, SearchQuery query) {
		boolean matches = switch (query.mode) {
			case ALL -> matchesRecipe(recipe, query) || matchesUsage(recipe, query);
			case RECIPE -> matchesRecipe(recipe, query);
			case USAGE -> matchesUsage(recipe, query);
		};

		return matches && matchesScope(recipe.getRecipe(), query);
	}

	private static boolean matchesRecipe(RecipeEntryDisplay recipe, SearchQuery query) {
		for (RecipeEntryDisplay.Frame frame : recipe.getFrames()) {
			for (ItemStack output : frame.outputs()) {
				if (matchesStack(output, query)) {
					return true;
				}
			}
		}

		return false;
	}

	private static boolean matchesUsage(RecipeEntryDisplay recipe, SearchQuery query) {
		for (RecipeEntryDisplay.Frame frame : recipe.getFrames()) {
			if (matchesStack(frame.input(), query)) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected int getRecipesPerPage() {
		return RecipePage.MAX_RECIPES_PER_PAGE;
	}

	@Override
	protected List<List<RecipeEntryDisplay>> splitIntoPages(List<RecipeEntryDisplay> recipes) {
		List<List<RecipeEntryDisplay>> pages = new ArrayList<>();
		List<RecipeEntryDisplay> page = new ArrayList<>();
		int top = RecipePage.FIRST_RECIPE_Y;

		for (RecipeEntryDisplay recipe : recipes) {
			if (!page.isEmpty() && (page.size() >= getRecipesPerPage() || !RecipePage.fits(top, recipe))) {
				pages.add(page);
				page = new ArrayList<>();
				top = RecipePage.FIRST_RECIPE_Y;
			}

			page.add(recipe);
			top += RecipePage.blockHeight(recipe);
		}

		if (!page.isEmpty()) {
			pages.add(page);
		}
		return pages;
	}

	@Override
	protected GuidebookPage createPage(List<RecipeEntryDisplay> recipes) {
		return new RecipePage(this, recipes);
	}

	@Environment(EnvType.CLIENT)
	protected static class RecipePage extends RecipePageBTX<RecipeEntryDisplay> {
		public static final int MAX_RECIPES_PER_PAGE = 3;
		public static final int FIRST_RECIPE_Y = 8;
		private static final int CONTENT_BOTTOM = 200;
		private static final int RECIPE_GAP = 8;
		private static final int INPUT_X = 21;
		private static final int INPUT_Y = 10;
		private static final int ARROW_X = 44;
		private static final int ARROW_Y = 11;
		private static final int SAW_ICON_X = 47;
		private static final int SAW_ICON_Y = 30;
		private static final int OUTPUT_X = 75;
		private static final int OUTPUT_Y = 1;
		private static final int OUTPUT_COLUMNS = 3;
		private static final int OUTPUT_SLOTS = 6;
		private static final int SLOT_SPACING = 18;
		private static final int NOTE_Y = 47;
		private static final int NOTE_LINE_HEIGHT = 8;
		private static final int TEXT_COLOR = 0x404040;
		private static final int TICKS_PER_FRAME = 20;

		private final List<RecipePage.DisplaySlots> displaySlots = new ArrayList<>();
		private int frameIndex = 0;
		private int frameTicks = 0;

		public RecipePage(GuidebookSection section, List<RecipeEntryDisplay> recipes) {
			super(section, recipes);

			int id = 0;
			int top = FIRST_RECIPE_Y;

			for (RecipeEntryDisplay recipe : recipes) {
				SlotGuidebook input = new SlotGuidebook(id++, INPUT_X, top + INPUT_Y, null, false, recipe);

				this.slots.add(input);

				SlotGuidebook sawIcon = new SlotGuidebook(id++, SAW_ICON_X, top + SAW_ICON_Y, null, true, recipe);

				sawIcon.item = new ItemStack(BTXBlocks.SAW);
				this.slots.add(sawIcon);
				this.backgroundlessSlots.add(sawIcon);

				List<SlotGuidebook> outputs = new ArrayList<>();

				for (int slot = 0; slot < OUTPUT_SLOTS; slot++) {
					SlotGuidebook output = new SlotGuidebook(id++,
						OUTPUT_X + slot % OUTPUT_COLUMNS * SLOT_SPACING,
						top + OUTPUT_Y + slot / OUTPUT_COLUMNS * SLOT_SPACING, null, false, recipe).setAsOutput();

					this.slots.add(output);
					outputs.add(output);
				}

				this.displaySlots.add(new RecipePage.DisplaySlots(recipe, top, input, outputs));
				top += blockHeight(recipe);
			}

			applyFrame();
		}

		public static int blockHeight(RecipeEntryDisplay recipe) {
			return NOTE_Y + recipe.getNoteLines().size() * NOTE_LINE_HEIGHT + RECIPE_GAP;
		}

		public static boolean fits(int top, RecipeEntryDisplay recipe) {
			return top + blockHeight(recipe) - RECIPE_GAP <= CONTENT_BOTTOM;
		}

		private void applyFrame() {
			for (RecipePage.DisplaySlots display : this.displaySlots) {
				List<RecipeEntryDisplay.Frame> frames = display.recipe.getFrames();
				RecipeEntryDisplay.Frame frame = frames.get(this.frameIndex % frames.size());

				display.input.item = frame.input();

				for (int slot = 0; slot < display.outputs.size(); slot++) {
					display.outputs.get(slot).item = slot < frame.outputs().size() ? frame.outputs().get(slot) : null;
				}
			}
		}

		@Override
		public void onTick() {
			super.onTick();

			if (++this.frameTicks >= TICKS_PER_FRAME) {
				this.frameTicks = 0;
				this.frameIndex++;
				applyFrame();
			}
		}

		@Override
		public void onBecomeVisible() {
			super.onBecomeVisible();

			this.frameIndex = 0;
			this.frameTicks = 0;

			applyFrame();
		}

		@Override
		protected void renderBackground(TextureManager re, int x, int y) {
			super.renderBackground(re, x, y);

			re.bindTexture(re.loadTexture(GUIDEBOOK_TEXTURE));

			for (RecipePage.DisplaySlots display : this.displaySlots) {
				drawArrow(x + ARROW_X, y + display.top + ARROW_Y);
			}
		}

		@Override
		protected void renderForeground(TextureManager re, FontRenderer sr, int x, int y, int mouseX, int mouseY, float partialTicks) {
			super.renderForeground(re, sr, x, y, mouseX, mouseY, partialTicks);

			for (RecipePage.DisplaySlots display : this.displaySlots) {
				List<String> lines = display.recipe.getNoteLines();

				for (int line = 0; line < lines.size(); line++) {
					this.drawStringCenteredNoShadow(sr, lines.get(line), x + this.width / 2,
						y + display.top + NOTE_Y + line * NOTE_LINE_HEIGHT, TEXT_COLOR);
				}
			}
		}

		private record DisplaySlots(RecipeEntryDisplay recipe, int top, SlotGuidebook input, List<SlotGuidebook> outputs) {
		}
	}


	// guidebook-only view of one saw recipe, or of a family of saw recipes that only differ by paint color
	@Environment(EnvType.CLIENT)
	protected static class RecipeEntryDisplay extends RecipeEntryBase<List<RecipeEntrySaw>, List<ItemStack>, Void> {
		public record Frame(ItemStack input, List<ItemStack> outputs) {}

		private static final int NOTE_MAX_WIDTH = 150;

		private final List<RecipeEntryDisplay.Frame> frames = new ArrayList<>();
		private @Nullable List<String> noteLines = null;

		public RecipeEntryDisplay(List<RecipeEntrySaw> variants) {
			super(variants, List.of(), null);
			this.parent = variants.get(0).parent;

			if (variants.size() > 1) {
				for (RecipeEntrySaw variant : variants) {
					List<ItemStack> inputs = variant.getInput().resolve();
					if (inputs != null && !inputs.isEmpty()) {
						this.frames.add(new RecipeEntryDisplay.Frame(displayStack(inputs.get(0), variant.getDisplayMeta()), variant.getOutput()));
					}
				}
			} else {
				RecipeEntrySaw recipe = variants.get(0);
				List<ItemStack> inputs = recipe.getInput().resolve();
				if (inputs != null) {
					for (ItemStack input : inputs) {
						int meta = inputs.size() == 1 ? recipe.getDisplayMeta() : input.getMetadata();
						this.frames.add(new RecipeEntryDisplay.Frame(displayStack(input, meta), recipe.getOutput()));
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

		public List<RecipeEntryDisplay.Frame> getFrames() {
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
}
