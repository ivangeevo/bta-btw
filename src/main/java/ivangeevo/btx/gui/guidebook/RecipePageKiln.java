package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.recipe.kiln.RecipeEntryKiln;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.SlotGuidebook;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.client.render.block.model.BlockModel;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.renderer.BlendFactor;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.block.ItemBlock;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.LightIndexHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.useless.dragonfly.DisplayPos;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class RecipePageKiln extends RecipePageBTX<RecipeEntryKiln> {
	public static final int RECIPES_PER_PAGE = 2;
	private static final int ORIGIN_X = 23;
	private static final int FIRST_RECIPE_Y = 10;
	private static final int RECIPE_SPACING = 94;
	private static final float SCENE_SCALE = 1.45F;
	private static final float SCENE_Z = 100.0F;
	private static final int SCENE_CENTER_X = 24;
	private static final int SCENE_CENTER_Y = 25;
	private static final byte FULL_BRIGHT = LightIndexHelper.lightIndex2i(15, 15);
	private static final int ARROW_X = 52;
	private static final int ARROW_Y = 26;
	private static final int OUTPUT_X = 87;
	private static final int OUTPUT_Y = 26;
	private static final int OUTPUT_SPACING = 27;
	private static final int BUTTON_LEFT_X = 50;
	private static final int BUTTON_RIGHT_X = 64;
	private static final int BUTTON_Y = 50;
	private static final int BUTTON_SIZE = 12;
	private static final String CYCLE_LEFT_SPRITE = "btx:gui/guidebook/cycle_left";
	private static final String CYCLE_RIGHT_SPRITE = "btx:gui/guidebook/cycle_right";
	private static final String HOVER_SUFFIX = "_hover";
	private static final KilnLayout layout = new KilnLayout();

	private final List<SlotGuidebook> inputSlots = new ArrayList<>();

	public RecipePageKiln(GuidebookSection section, List<RecipeEntryKiln> recipes) {
		super(section, recipes);

		int id = 0;
		for (int i = 0; i < recipes.size(); i++) {
			RecipeEntryKiln recipe = recipes.get(i);
			int top = recipeTop(i);

			SlotGuidebook input = createInputSlot(id++, ORIGIN_X + SCENE_CENTER_X - 8, top + SCENE_CENTER_Y - 8, recipe.getInput(), recipe);
			this.slots.add(input);
			this.hiddenSlots.add(input);
			this.inputSlots.add(input);

			List<ItemStack> outputs = mergeOutputs(recipe.getOutputs());
			for (int slot = 0; slot < outputs.size(); slot++) {
				ItemStack output = outputs.get(slot);
				SlotGuidebook outputSlot = new SlotGuidebook(id++, ORIGIN_X + OUTPUT_X + slot / 2 * OUTPUT_SPACING, top + OUTPUT_Y + slot % 2 * OUTPUT_SPACING, new RecipeSymbol(output), false, recipe).setAsOutput();
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
			drawArrow(x + ORIGIN_X + ARROW_X, y + recipeTop(i) + ARROW_Y);
		}
	}

	@Override
	protected void renderForeground(TextureManager re, FontRenderer sr, int x, int y, int mouseX, int mouseY, float partialTicks) {
		for (int i = 0; i < this.recipes.size(); i++) {
			int top = y + recipeTop(i);

			renderKiln(x + ORIGIN_X + SCENE_CENTER_X, top + SCENE_CENTER_Y, this.inputSlots.get(i).getItemStack());

			GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			drawButton(x + ORIGIN_X + BUTTON_LEFT_X, top + BUTTON_Y, CYCLE_LEFT_SPRITE, mouseX, mouseY);
			drawButton(x + ORIGIN_X + BUTTON_RIGHT_X, top + BUTTON_Y, CYCLE_RIGHT_SPRITE, mouseX, mouseY);
		}

		super.renderForeground(re, sr, x, y, mouseX, mouseY, partialTicks);
	}

	private void renderKiln(int centerX, int centerY, @Nullable ItemStack input) {
		BlockModel<?> brickModel = BlockModelDispatcher.getInstance().getDispatch(Blocks.BRICK_CLAY);
		DisplayPos view = brickModel.getItemDisplayPos(DisplayPos.GUI);
		Matrix4f rotation = new Matrix4f()
			.rotateX((float) Math.toRadians(view.rx))
			.rotateY((float) Math.toRadians(view.ry))
			.rotateZ((float) Math.toRadians(view.rz));

		GLRenderer.pushFrame();
		GLRenderer.setShader(Shaders.ITEM);
		GLRenderer.enableState(State.BLEND);
		GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
		GLRenderer.enableState(State.CULL_FACE);
		GLRenderer.enableState(State.DEPTH_TEST);
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		Matrix4f model = GLRenderer.modelM4f();
		model.translate(centerX, centerY, SCENE_Z);
		model.scale(16.0F * SCENE_SCALE, -16.0F * SCENE_SCALE, 16.0F * SCENE_SCALE);
		model.mul(rotation);
		model.scale(view.sx, view.sy, view.sz);

		renderBlock(BTXBlocks.HIBACHI, 0, 0, -3, 0);
		renderBlock(BTXBlocks.STOKED_FIRE, 0, 0, -2, 0);
		renderBlock(Blocks.BRICK_CLAY, 0, 0, -1, 0);

		if (input != null && input.getItem() instanceof ItemBlock<?> itemBlock) {
			renderBlock(itemBlock.getBlock(), input.getMetadata(), 0, 0, 0);
		}

		if (layout.top) {
			renderBlock(Blocks.BRICK_CLAY, 0, 0, 1, 0);
		}

		for (Direction side : Direction.horizontal) {
			Vector3f screen = rotation.transformDirection(new Vector3f(side.offsetX(), 0.0F, side.offsetZ()));
			boolean behind = screen.y > 0.0F;
			boolean onLeft = screen.x < 0.0F;
			boolean shown = behind ? (onLeft ? layout.left : layout.back) : (onLeft ? layout.front : layout.right);
			if (shown) {
				renderBlock(Blocks.BRICK_CLAY, 0, side.offsetX(), 0, side.offsetZ());
			}
		}

		GLRenderer.popFrame();

		if (input != null && !(input.getItem() instanceof ItemBlock<?>)) {
			this.itemElement.render(input, centerX - 8, centerY - 8);
		}
	}

	private static void renderBlock(@Nullable Block<?> block, int metadata, float x, float y, float z) {
		if (block == null) {
			return;
		}

		BlockModel<?> blockModel = BlockModelDispatcher.getInstance().getDispatch(block);

		if (blockModel == null) {
			return;
		}

		Matrix4f model = GLRenderer.modelM4f();

		model.translate(x, y, z);
		TextureRegistry.worldAtlas.bind();
		blockModel.renderStandalone(GLRenderer.getTessellator(), metadata, FULL_BRIGHT);
		model.translate(-x, -y, -z);
	}

	private void drawButton(int x, int y, String sprite, int mouseX, int mouseY) {
		String texture = isInside(mouseX, mouseY, x, y, BUTTON_SIZE, BUTTON_SIZE) ? sprite + HOVER_SUFFIX : sprite;
		this.drawGuiIcon(x, y, BUTTON_SIZE, BUTTON_SIZE, TextureRegistry.getTexture(texture));
	}

	@Override
	public void onMouseDown(int x, int y, int mouseX, int mouseY, int button) {
		super.onMouseDown(x, y, mouseX, mouseY, button);

		if (button != 0) {
			return;
		}

		for (int i = 0; i < this.recipes.size(); i++) {
			int top = y + recipeTop(i) + BUTTON_Y;

			if (isInside(mouseX, mouseY, x + ORIGIN_X + BUTTON_LEFT_X, top, BUTTON_SIZE, BUTTON_SIZE)) {
				layout.cycle(-1);
				return;
			}

			if (isInside(mouseX, mouseY, x + ORIGIN_X + BUTTON_RIGHT_X, top, BUTTON_SIZE, BUTTON_SIZE)) {
				layout.cycle(1);
				return;
			}
		}
	}

	@Override
	protected @Nullable String getExtraTooltip(int x, int y, int mouseX, int mouseY) {
		for (int i = 0; i < this.recipes.size(); i++) {
			int top = y + recipeTop(i) + BUTTON_Y;
			if (isInside(mouseX, mouseY, x + ORIGIN_X + BUTTON_LEFT_X, top, BUTTON_RIGHT_X - BUTTON_LEFT_X + BUTTON_SIZE, BUTTON_SIZE)) {
				return I18n.getInstance().translateKey("btx.guidebook.kiln.cycle");
			}
		}

		return null;
	}

	private static final class KilnLayout {
		private static final int LAYOUT_COUNT = 6;
		private boolean left = true;
		private boolean back = true;
		private boolean right = true;
		private boolean front;
		private boolean top;
		private int index;

		private void cycle(int delta) {
			this.index = Math.floorMod(this.index + delta, LAYOUT_COUNT);
			this.left = this.index == 0 || this.index == 3 || this.index == 4 || this.index == 5;
			this.back = this.index == 0 || this.index == 1 || this.index == 2 || this.index == 4 || this.index == 5;
			this.right = this.index == 0 || this.index == 2;
			this.front = this.index == 1 || this.index == 3 || this.index == 5;
			this.top = this.index != 0;
		}
	}
}
