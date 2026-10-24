package ivangeevo.btx.entity.entities.lift;

import ivangeevo.btx.block.BTXBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.BlockModel;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import org.joml.primitives.AABBd;

@Environment(EnvType.CLIENT)
public class MovingPlatformEntityRenderer extends StandaloneBlockEntityRenderer<MovingPlatformEntity> {
	private static final int[][] SIDE_OFFSETS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

	@Override
	protected void renderBlock(TessellatorGeneral tessellator, MovingPlatformEntity entity, byte lightIndex, float partialTick) {
		BlockModel<?> model = (BlockModel<?>) BlockModelDispatcher.getInstance().getDispatch(BTXBlocks.PLATFORM);
		model.renderStandalone(tessellator, getHiddenSides(entity), lightIndex);
	}

	private static int getHiddenSides(MovingPlatformEntity entity) {
		int hidden = 0;
		AABBd area = new AABBd(entity.x - 1.25D, entity.y - 0.25D, entity.z - 1.25D, entity.x + 1.25D, entity.y + 0.25D, entity.z + 1.25D);

		for (MovingPlatformEntity neighbor : entity.world.getEntitiesWithinAABB(MovingPlatformEntity.class, area)) {
			if (neighbor == entity || neighbor.removed || Math.abs(neighbor.y - entity.y) > 0.001D) {
				continue;
			}

			int dx = (int) Math.round(neighbor.x - entity.x);
			int dz = (int) Math.round(neighbor.z - entity.z);

			for (int i = 0; i < SIDE_OFFSETS.length; i++) {
				if (SIDE_OFFSETS[i][0] == dx && SIDE_OFFSETS[i][1] == dz) {
					hidden |= 1 << i;
				}
			}
		}

		return hidden;
	}
}