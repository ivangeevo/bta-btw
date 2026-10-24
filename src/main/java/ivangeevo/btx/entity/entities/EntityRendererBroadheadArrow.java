package ivangeevo.btx.entity.entities;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.util.helper.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

@Environment(EnvType.CLIENT)
public class EntityRendererBroadheadArrow extends EntityRenderer<EntityBroadheadArrow> {
	private static final String TEXTURE = "/assets/btx/textures/entity/broadhead_arrow.png";
	private static final float IMAGE_SIZE = 32.0F;
	private static final int BLINK_START = 900;

	@Override
	public void render(@NotNull TessellatorGeneral tessellator, @NotNull EntityBroadheadArrow arrow, double x, double y, double z, float yaw, float partialTick) {
		if (arrow.ticksInGround + partialTick > BLINK_START) {
			int pastPrime = arrow.ticksInGround - BLINK_START;
			double toDeath = 1.0D - (pastPrime + partialTick) * 4.0D / 1200.0D;

			if (java.lang.Math.cos((pastPrime + partialTick) * java.lang.Math.PI / (2.5D + 5.5D * toDeath)) > 0.0D) {
				return;
			}
		}

		this.bindTexture(TEXTURE);
		GLRenderer.pushFrame();
		GLRenderer.modelM4f().translate((float) x, (float) y, (float) z);
		GLRenderer.modelM4f().rotateY(Math.toRadians(Math.lerp(arrow.yRotO, arrow.yRot, partialTick) - 90.0F));
		GLRenderer.modelM4f().rotateZ(Math.toRadians(Math.lerp(arrow.xRotO, arrow.xRot, partialTick)));

		float bodyMinU = 0.0F;
		float bodyMaxU = 16.0F / IMAGE_SIZE;
		float bodyMinV = 0.0F;
		float bodyMaxV = 5.0F / IMAGE_SIZE;
		float tailMinU = 0.0F;
		float tailMaxU = 5.0F / IMAGE_SIZE;
		float tailMinV = 5.0F / IMAGE_SIZE;
		float tailMaxV = 10.0F / IMAGE_SIZE;
		float scale = 0.05625F;
		float shakeAmount = arrow.shake - partialTick;

		if (shakeAmount > 0.0F) {
			float shakeAngle = -MathHelper.sin(shakeAmount * 3.0F) * shakeAmount;
			GLRenderer.modelM4f().rotateZ(Math.toRadians(shakeAngle));
		}

		GLRenderer.modelM4f().rotateX(Math.toRadians(45.0F));
		GLRenderer.modelM4f().scale(scale, scale, scale);
		GLRenderer.modelM4f().translate(-4.0F, 0.0F, 0.0F);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, tailMinU, tailMinV);
		tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, tailMaxU, tailMinV);
		tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, tailMaxU, tailMaxV);
		tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, tailMinU, tailMaxV);
		tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, tailMinU, tailMinV);
		tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, tailMaxU, tailMinV);
		tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, tailMaxU, tailMaxV);
		tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, tailMinU, tailMaxV);
		tessellator.addVertexWithUV(-8.0D, -2.0D, 0.0D, bodyMinU, bodyMinV);
		tessellator.addVertexWithUV(-8.0D, 2.0D, 0.0D, bodyMinU, bodyMaxV);
		tessellator.addVertexWithUV(8.0D, 2.0D, 0.0D, bodyMaxU, bodyMaxV);
		tessellator.addVertexWithUV(8.0D, -2.0D, 0.0D, bodyMaxU, bodyMinV);
		tessellator.addVertexWithUV(-8.0D, -2.0D, 0.0D, bodyMinU, bodyMinV);
		tessellator.addVertexWithUV(8.0D, -2.0D, 0.0D, bodyMaxU, bodyMinV);
		tessellator.addVertexWithUV(8.0D, 2.0D, 0.0D, bodyMaxU, bodyMaxV);
		tessellator.addVertexWithUV(-8.0D, 2.0D, 0.0D, bodyMinU, bodyMaxV);
		tessellator.addVertexWithUV(-8.0D, 0.0D, -2.0D, bodyMinU, bodyMinV);
		tessellator.addVertexWithUV(-8.0D, 0.0D, 2.0D, bodyMinU, bodyMaxV);
		tessellator.addVertexWithUV(8.0D, 0.0D, 2.0D, bodyMaxU, bodyMaxV);
		tessellator.addVertexWithUV(8.0D, 0.0D, -2.0D, bodyMaxU, bodyMinV);
		tessellator.addVertexWithUV(-8.0D, 0.0D, -2.0D, bodyMinU, bodyMinV);
		tessellator.addVertexWithUV(8.0D, 0.0D, -2.0D, bodyMaxU, bodyMinV);
		tessellator.addVertexWithUV(8.0D, 0.0D, 2.0D, bodyMaxU, bodyMaxV);
		tessellator.addVertexWithUV(-8.0D, 0.0D, 2.0D, bodyMinU, bodyMaxV);
		tessellator.draw();
		GLRenderer.popFrame();
	}
}