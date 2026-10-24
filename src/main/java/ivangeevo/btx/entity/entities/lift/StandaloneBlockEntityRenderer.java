package ivangeevo.btx.entity.entities.lift;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.renderer.BlendFactor;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.Texture;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.entity.Entity;
import org.lwjgl.opengl.GL41;

@Environment(EnvType.CLIENT)
public abstract class StandaloneBlockEntityRenderer<T extends Entity> extends EntityRenderer<T> {
	protected StandaloneBlockEntityRenderer() {
		super(0.0F);
	}

	protected abstract void renderBlock(TessellatorGeneral tessellator, T entity, byte lightIndex, float partialTick);

	@Override
	public void render(TessellatorGeneral tessellator, T entity, double x, double y, double z, float yaw, float partialTick) {
		byte lightIndex = entity.getLightIndex(partialTick);

		GLRenderer.pushFrame();
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GLRenderer.setLightmapCoord1i(lightIndex);
		GLRenderer.modelM4f().translate((float) x, (float) y, (float) z);
		GLRenderer.setShader(Shaders.ITEM);
		TextureRegistry.worldAtlas.bind();
		GL41.glActiveTexture(GL41.GL_TEXTURE2);
		((Texture) TextureRegistry.worldAtlas.layerTextureMap.get("emissive")).bind();
		GL41.glActiveTexture(GL41.GL_TEXTURE3);
		((Texture) TextureRegistry.worldAtlas.layerTextureMap.get("maskColor")).bind();
		GL41.glActiveTexture(GL41.GL_TEXTURE0);
		GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
		GLRenderer.enableState(State.BLEND);

		renderBlock(tessellator, entity, lightIndex, partialTick);

		GLRenderer.popFrame();
	}
}