package ivangeevo.btx.entity.entities.lift;

import ivangeevo.btx.block.BTXBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.BlockModel;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.pos.TilePos;
import org.useless.dragonfly.models.block.StaticBlockModel;

import static net.minecraft.client.render.block.model.BlockModelDispatcher.loadDataModel;

@Environment(EnvType.CLIENT)
public class MovingAnchorEntityRenderer extends StandaloneBlockEntityRenderer<MovingAnchorEntity> {
	private static final double LOOP_TOP = 10.0D / 16.0D;

	private final StaticBlockModel ropeSegment = loadDataModel("btx:block/anchor/anchor_rope_segment").asModel();
	private final StaticBlockModel ropeSegmentInner = loadDataModel("btx:block/anchor/anchor_rope_segment_inner").asModel();

	@Override
	protected void renderBlock(TessellatorGeneral tessellator, MovingAnchorEntity entity, byte lightIndex, float partialTick) {
		BlockModel<?> model = (BlockModel<?>) BlockModelDispatcher.getInstance().getDispatch(BTXBlocks.ANCHOR);
		model.renderStandalone(tessellator, 1, lightIndex);

		double bottom = entity.yo + (entity.y - entity.yo) * partialTick - entity.heightOffset;
		int bottomBlock = MathHelper.floor(bottom);
		int x = MathHelper.floor(entity.x);
		int z = MathHelper.floor(entity.z);

		if (!isRopeOrPulley(entity.world.getBlockType(new TilePos(x, bottomBlock + 1, z)))
			&& !isRopeOrPulley(entity.world.getBlockType(new TilePos(x, bottomBlock + 2, z)))
		) {
			return;
		}

		BlockModelGeneric<?> owner = (BlockModelGeneric<?>) model;
		double top = bottomBlock + 2.0D;

		renderSegment(this.ropeSegment, owner, tessellator, lightIndex, top - 1.0D - bottom);

		if (top - 1.0D > bottom + LOOP_TOP) {
			renderSegment(this.ropeSegmentInner, owner, tessellator, lightIndex, LOOP_TOP);
		}
	}

	private static boolean isRopeOrPulley(Block<?> block) {
		return block == Blocks.ROPE || block == BTXBlocks.PULLEY;
	}

	private static void renderSegment(StaticBlockModel segment, BlockModelGeneric<?> owner, TessellatorGeneral tessellator, byte lightIndex, double offsetY) {
		GLRenderer.pushFrame();
		GLRenderer.modelM4f().translate(0.0F, (float) offsetY, 0.0F);
		segment.renderStandalone(owner, tessellator, 0.0D, 0.0D, 0.0D, 0, lightIndex, BlockColorDispatcher.getInstance().getDispatch(BTXBlocks.ANCHOR));
		GLRenderer.popFrame();
	}
}
