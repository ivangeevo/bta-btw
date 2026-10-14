package ivangeevo.btx.entity.entities;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.util.helper.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Matrix4f;

public class WaterWheelEntityRenderer extends EntityRenderer<WaterWheelEntity> {

	private static final String WHEEL_MODEL = "wheel";

	public WaterWheelEntityRenderer() {
		super(0.0F);
		setModel(WHEEL_MODEL, "geometry.water_wheel", 0.0);
	}

	@Override
	public void render(@NotNull TessellatorGeneral tessellator, @NotNull WaterWheelEntity wheel,
					   double x, double y, double z, float yaw, float partialTick
	) {
		GLRenderer.pushFrame();

		Matrix4f matrix = GLRenderer.modelM4f();
		matrix.translate((float) x, (float) y, (float) z);

		// rocking from damage
		float deltaTime = wheel.timeSinceHit - partialTick;
		float deltaDamage = Math.max(wheel.currentDamage - partialTick, 0.0F);
		float rock = deltaDamage > 0.0F
			? MathHelper.sin(deltaTime) * deltaTime * deltaDamage / 40.0F * wheel.rockDirection
			: 0.0F;

		// same smoothing as the windmill: interpolate rotation using the eased speed
		float spin = wheel.rotation + wheel.getDisplayedRotationSpeed() * partialTick;

		if (wheel.alignedToX) {
			matrix.rotateX(Math.toRadians(spin));
			matrix.rotateZ(Math.toRadians(rock));
			matrix.rotateY(Math.toRadians(90.0F));
		} else {
			matrix.rotateZ(Math.toRadians(spin));
			matrix.rotateX(Math.toRadians(rock));
		}

		// geometry is centred on its (8, 8, 8) pivot, so move that to the entity position
		matrix.scale(0.0625F, 0.0625F, -0.0625F);
		matrix.translate(-8.0F, -8.0F, -8.0F);

		TextureRegistry.worldAtlas.bind();
		this.bindTexture("/assets/btx/textures/entity/water_wheel.png");

		GLRenderer.setColor4f(1F, 1F, 1F, 1F);

		this.getModel(WHEEL_MODEL).render();

		GLRenderer.popFrame();
	}
}
