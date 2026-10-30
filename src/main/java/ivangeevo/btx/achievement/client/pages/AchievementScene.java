package ivangeevo.btx.achievement.client.pages;

import net.minecraft.client.render.EntityRendererDispatcher;
import net.minecraft.client.render.Lighting;
import net.minecraft.client.render.block.model.BlockModel;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.renderer.BlendFactor;
import net.minecraft.client.render.renderer.CompareFunc;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.client.world.WorldEmpty;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.helper.LightIndexHelper;
import net.minecraft.core.world.World;
import org.joml.Math;
import org.lwjgl.opengl.GL41;

public final class AchievementScene {
	private static final float BACKDROP_Z = -700.0F;
	private static final byte FULL_BRIGHT = LightIndexHelper.lightIndex2i(15, 15);

	private final int left;
	private final int top;
	private final int width;
	private final int height;
	private final double zoom;
	private final double originX;
	private final double originY;
	private boolean lit = true;
	private boolean drawn;

	private AchievementScene(int left, int top, int width, int height, double zoom, double shiftX, double shiftY) {
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.zoom = zoom;
		this.originX = left + width / 2.0 - zoom * (shiftX + 288.0);
		this.originY = top + height / 2.0 - zoom * (shiftY + 288.0);
	}

	public static void render(BTXAchievementPage page, int left, int top, int width, int height, double zoom, double shiftX, double shiftY) {
		AchievementScene scene = new AchievementScene(left, top, width, height, zoom, shiftX, shiftY);

		GLRenderer.pushFrame();
		GLRenderer.enableState(State.DEPTH_TEST);
		GLRenderer.disableState(State.CULL_FACE);
		GLRenderer.setDepthMask(true);
		GLRenderer.setDepthFunc(CompareFunc.LESS_EQUAL);

		// scissored to the viewport, so only the achievement map is cleared
		GL41.glClear(GL41.GL_DEPTH_BUFFER_BIT);

		page.scene(scene);

		if (scene.drawn) {
			scene.shadeLikeBackground();
			GL41.glClear(GL41.GL_DEPTH_BUFFER_BIT);
		}

		GLRenderer.popFrame();
	}

	public float seconds() {
		return (System.currentTimeMillis() % 3_600_000L) / 1000.0F;
	}

	public World world() {
		return WorldEmpty.INSTANCE;
	}

	public void setLit(boolean lit) {
		this.lit = lit;
	}

	public void block(Block<?> block, int data, double x, double y) {
		block(block, data, x, y, 0.0F, 0.0F, 0.0F);
	}

	public void block(Block<?> block, int data, double x, double y, float pitch, float yaw, float roll) {
		BlockModel<?> model = BlockModelDispatcher.getInstance().getDispatch(block);

		if (model == null) {
			return;
		}

		begin(x, y, pitch, yaw, roll);
		TextureRegistry.worldAtlas.bind();
		model.renderStandalone(GLRenderer.getTessellator(), data, FULL_BRIGHT);
		end();
	}

	public void entity(Entity entity, double x, double y) {
		entity(entity, x, y, 0.0F, 0.0F, 0.0F);
	}

	public void entity(Entity entity, double x, double y, float pitch, float yaw, float roll) {
		begin(x, y, pitch, yaw, roll);
		entity.entityBrightness = 1.0F;
		GLRenderer.setLightmapCoord1i(FULL_BRIGHT);
		EntityRendererDispatcher.instance.renderEntityPreviewWithPosYaw(GLRenderer.getTessellator(), entity, 0.0, 0.0, 0.0, 0.0F, 0.0F);
		end();
	}

	private void begin(double x, double y, float pitch, float yaw, float roll) {
		this.drawn = true;

		GLRenderer.pushFrame();
		GLRenderer.setShader(Shaders.WORLD);
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		float scale = (float) (16.0 * this.zoom);

		GLRenderer.modelM4f()
			.translate((float) (this.originX + (x + 0.5) * scale), (float) (this.originY + (y + 0.5) * scale), 0.0F)
			.scale(scale, -scale, scale)
			.rotateZ(Math.toRadians(roll))
			.rotateY(Math.toRadians(yaw))
			.rotateX(Math.toRadians(pitch));

		if (this.lit) {
			Lighting.enableLight();
			GLRenderer.globalGetNormalTransformMatrix().scale(1.0F, -1.0F, 1.0F);
		} else {
			Lighting.disable();
		}
	}

	private void end() {
		Lighting.disable();
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GLRenderer.popFrame();
	}

	// multiplies only the scene's pixels by the background's per-row brightness: the backdrop sits behind
	// everything, so GREATER passes where the scene wrote depth and fails on the cleared background
	private void shadeLikeBackground() {
		GLRenderer.pushFrame();
		GLRenderer.setShader(Shaders.COLOR);
		GLRenderer.setDepthMask(false);
		GLRenderer.setDepthFunc(CompareFunc.GREATER);
		GLRenderer.enableState(State.BLEND);
		GLRenderer.setBlendFunc(BlendFactor.DST_COLOR, BlendFactor.ZERO);

		double tileSize = 16.0 * this.zoom;
		int firstRow = (int) Math.floor((this.top - this.originY) / tileSize);
		int lastRow = (int) Math.floor((this.top + this.height - this.originY) / tileSize);

		TessellatorGeneral t = GLRenderer.getTessellator();

		t.startDrawingQuads();

		for (int row = firstRow; row <= lastRow; row++) {
			float brightness = 0.6F - row / 25.0F * 0.3F;
			double rowTop = this.originY + row * tileSize;
			t.setColor4f(brightness, brightness, brightness, 1.0F);
			t.addVertex(this.left, rowTop + tileSize, BACKDROP_Z);
			t.addVertex(this.left + this.width, rowTop + tileSize, BACKDROP_Z);
			t.addVertex(this.left + this.width, rowTop, BACKDROP_Z);
			t.addVertex(this.left, rowTop, BACKDROP_Z);
		}

		t.draw();

		GLRenderer.popFrame();
	}
}
