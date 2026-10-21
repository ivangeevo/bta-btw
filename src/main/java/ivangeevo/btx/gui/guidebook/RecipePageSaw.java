package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.recipe.saw.RecipeEntrySaw;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.SlotGuidebook;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class RecipePageSaw extends RecipePageBTX<RecipeEntrySawDisplay> {
	public static final int RECIPES_PER_PAGE = 3;
	private static final int FIRST_RECIPE_Y = 8;
	private static final int RECIPE_SPACING = 62;
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
	private static final int NOTE_Y = 50;
	private static final int TEXT_COLOR = 0x404040;
	private static final int TICKS_PER_FRAME = 20;

	private final List<DisplaySlots> displaySlots = new ArrayList<>();
	private int frameIndex = 0;
	private int frameTicks = 0;

	public RecipePageSaw(GuidebookSection section, List<RecipeEntrySawDisplay> recipes) {
		super(section, recipes);

		int id = 0;

		for (int i = 0; i < recipes.size(); i++) {
			RecipeEntrySawDisplay recipe = recipes.get(i);
			int top = recipeTop(i);

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

			this.displaySlots.add(new DisplaySlots(recipe, input, outputs, buildNote(recipe.getRecipe())));
		}

		applyFrame();
	}

	private static int recipeTop(int index) {
		return FIRST_RECIPE_Y + index * RECIPE_SPACING;
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

	private void applyFrame() {
		for (DisplaySlots display : this.displaySlots) {
			List<RecipeEntrySawDisplay.Frame> frames = display.recipe.getFrames();
			RecipeEntrySawDisplay.Frame frame = frames.get(this.frameIndex % frames.size());

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

		for (int i = 0; i < this.recipes.size(); i++) {
			drawArrow(x + ARROW_X, y + recipeTop(i) + ARROW_Y);
		}
	}

	@Override
	protected void renderForeground(TextureManager re, FontRenderer sr, int x, int y, int mouseX, int mouseY, float partialTicks) {
		super.renderForeground(re, sr, x, y, mouseX, mouseY, partialTicks);

		for (int i = 0; i < this.displaySlots.size(); i++) {
			String note = this.displaySlots.get(i).note;
			if (!note.isEmpty()) {
				this.drawStringCenteredNoShadow(sr, note, x + this.width / 2, y + recipeTop(i) + NOTE_Y, TEXT_COLOR);
			}
		}
	}

	private record DisplaySlots(RecipeEntrySawDisplay recipe, SlotGuidebook input, List<SlotGuidebook> outputs, String note) {
	}
}
