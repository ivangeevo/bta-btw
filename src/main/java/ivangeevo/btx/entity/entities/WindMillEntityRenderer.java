package ivangeevo.btx.entity.entities;

import ivangeevo.btx.BTXGlobals;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.util.helper.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Matrix4f;

public class WindMillEntityRenderer extends EntityRenderer<WindMillEntity> {

	public WindMillEntityRenderer() {
		super(0.0F);
		setModel("main", "geometry.windmill", 0.0);
		BTXGlobals.LOGGER.info("[WindMill] WindMillEntityRenderer constructed");
	}

	private static int renderLogCount = 0;


	@Override
	public void render(@NotNull TessellatorGeneral tessellator, @NotNull WindMillEntity mill,
					   double x, double y, double z, float yaw, float partialTick) {

		if (renderLogCount++ < 5) {
			BTXGlobals.LOGGER.info("[WindMill] render called: rel=({}, {}, {}), rotation={}, alignedToX={}",
				x, y, z, mill.rotation, mill.alignedToX);
		}

		GLRenderer.pushFrame();
		Matrix4f m = GLRenderer.modelM4f();
		m.translate((float) x, (float) y, (float) z);

		// rocking from damage
		float deltaTime = mill.timeSinceHit - partialTick;
		float deltaDamage = Math.max(mill.currentDamage - partialTick, 0.0F);
		float rock = deltaDamage > 0.0F
			? MathHelper.sin(deltaTime) * deltaTime * deltaDamage / 40.0F * mill.rockDirection
			: 0.0F;

		// smooth spin between ticks
		float spin = mill.rotation + mill.getRotationSpeed() * partialTick;

		if (mill.alignedToX) {
			m.rotateX(Math.toRadians(spin));
			m.rotateZ(Math.toRadians(rock));
			m.rotateY(Math.toRadians(90.0F));
		} else {
			m.rotateZ(Math.toRadians(spin));
			m.rotateX(Math.toRadians(rock));
		}

		// the geometry is authored in pixels, centered on (8, 8, 8)
		// m.scale(0.0625F);
		m.scale(0.0625F, 0.0625F, -0.0625F);
		m.translate(-8.0F, -8.0F, -8.0F);

		TextureRegistry.worldAtlas.bind();
		this.bindTexture("/assets/btx/textures/entity/windmill.png");
		this.getModel("main").render();

		GLRenderer.popFrame();
	}
}
