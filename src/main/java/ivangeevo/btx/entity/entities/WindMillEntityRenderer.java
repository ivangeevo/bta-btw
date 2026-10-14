package ivangeevo.btx.entity.entities;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Matrix4f;

public class WindMillEntityRenderer extends EntityRenderer<WindMillEntity> {

	private static final String[] BLADE_MODELS = {"blade0", "blade90", "blade180", "blade270"};
	private static final DyeColor[] DYES = DyeColor.values();

	public WindMillEntityRenderer() {
		super(0.0F);
		setModel("shaft", "geometry.windmill_shaft", 0.0);
		setModel("blade0", "geometry.windmill_blade_0", 0.0);
		setModel("blade90", "geometry.windmill_blade_90", 0.0);
		setModel("blade180", "geometry.windmill_blade_180", 0.0);
		setModel("blade270", "geometry.windmill_blade_270", 0.0);
	}

	@Override
	public void render(@NotNull TessellatorGeneral tessellator, @NotNull WindMillEntity mill,
					   double x, double y, double z, float yaw, float partialTick
	) {
		GLRenderer.pushFrame();

		Matrix4f matrix = GLRenderer.modelM4f();
		matrix.translate((float) x, (float) y, (float) z);

		// rocking from damage
		float deltaTime = mill.timeSinceHit - partialTick;
		float deltaDamage = Math.max(mill.currentDamage - partialTick, 0.0F);
		float rock = deltaDamage > 0.0F
			? MathHelper.sin(deltaTime) * deltaTime * deltaDamage / 40.0F * mill.rockDirection
			: 0.0F;

		// Unlike the original implementation, we apply interpolation to the rotation and also liner easing of the speed

		// Only rotation interpolation (smooth spin between ticks)
		//float spin = mill.rotation + mill.getRotationSpeed() * partialTick;

		// Rotation interpolation and speed linear easing
		float spin = mill.rotation + mill.getDisplayedRotationSpeed() * partialTick;

		if (mill.alignedToX) {
			matrix.rotateX(Math.toRadians(spin));
			matrix.rotateZ(Math.toRadians(rock));
			matrix.rotateY(Math.toRadians(90.0F));
		} else {
			matrix.rotateZ(Math.toRadians(spin));
			matrix.rotateX(Math.toRadians(rock));
		}

		matrix.scale(0.0625F, 0.0625F, -0.0625F);
		matrix.translate(-8.0F, -8.0F, -8.0F);

		TextureRegistry.worldAtlas.bind();
		this.bindTexture("/assets/btx/textures/entity/windmill.png");

		GLRenderer.setColor4f(1F, 1F, 1F, 1F);

		this.getModel("shaft").render();

		for (int i = 0; i < 4; i++) {
			int idx = Math.max(0, Math.min(DYES.length - 1, mill.getBladeColor(i)));
			GLRenderer.setColor(DYES[idx].color);
			this.getModel(BLADE_MODELS[i]).render();
		}

		GLRenderer.setColor4f(1F, 1F, 1F, 1F);

		GLRenderer.popFrame();
	}
}
