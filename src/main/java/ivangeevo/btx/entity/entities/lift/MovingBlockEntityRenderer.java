package ivangeevo.btx.entity.entities.lift;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.BlockModel;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.renderer.BlendFactor;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.Texture;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.BlocksContainer;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL41;

@Environment(EnvType.CLIENT)
public abstract class MovingBlockEntityRenderer<T extends Entity> extends EntityRenderer<T> {
	private @Nullable BlocksContainer container;

	protected MovingBlockEntityRenderer() {
		super(0.0F);
	}

	protected abstract @Nullable Block<?> getBlock(T entity);

	protected abstract int getBlockData(T entity);

	protected void addNeighborBlocks(BlocksContainer container, T entity, TilePos pos) {}

	protected void renderExtra(TessellatorGeneral tessellator, BlocksContainer container, T entity, TilePos pos, float partialTick) {}

	@Override
	public void render(TessellatorGeneral tessellator, T entity, double x, double y, double z, float yaw, float partialTick) {
		Block<?> block = getBlock(entity);

		if (block == null) {
			return;
		}

		if (this.container == null || this.container.world != entity.world) {
			this.container = new BlocksContainer(entity.world);
		}

		GLRenderer.pushFrame();
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GLRenderer.setLightmapCoord2f(15.0F, 15.0F);
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

		TilePos pos = new TilePos(entity);
		tessellator.startDrawingQuads();
		tessellator.setTranslation(-pos.x - 0.5D, -pos.y - 0.5D, -pos.z - 0.5D);
		this.container.setLightReferenceEntity(entity);
		this.container.setBlock(pos.x, pos.y, pos.z, block.id(), getBlockData(entity), null);

		addNeighborBlocks(this.container, entity, pos);

		BlockModel<?> model = (BlockModel<?>) BlockModelDispatcher.getInstance().getDispatch(block);
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		GLRenderer.getTessellator().setNormal(0.0F, 1.0F, 0.0F);
		model.renderNoCulling(GLRenderer.getTessellator(), this.container, pos);
		renderExtra(tessellator, this.container, entity, pos, partialTick);

		this.container.setLightReferenceEntity(null);
		this.container.clear();
		tessellator.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		GLRenderer.popFrame();
	}
}