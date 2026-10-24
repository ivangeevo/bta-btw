package ivangeevo.btx.entity.entities.lift;

import ivangeevo.btx.block.BTXBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.block.Block;
import net.minecraft.core.world.BlocksContainer;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;

@Environment(EnvType.CLIENT)
public class BlockLiftedByPlatformEntityRenderer extends MovingBlockEntityRenderer<BlockLiftedByPlatformEntity> {
	@Override
	protected @Nullable Block<?> getBlock(BlockLiftedByPlatformEntity entity) {
		return entity.getBlock();
	}

	@Override
	protected int getBlockData(BlockLiftedByPlatformEntity entity) {
		return entity.getBlockData();
	}

	@Override
	protected void addNeighborBlocks(BlocksContainer container, BlockLiftedByPlatformEntity entity, TilePos pos) {
		container.setBlock(pos.x, pos.y - 1, pos.z, BTXBlocks.PLATFORM.id(), 0, null);

		AABBd area = new AABBd(entity.x - 1.25D, entity.y - 0.25D, entity.z - 1.25D, entity.x + 1.25D, entity.y + 0.25D, entity.z + 1.25D);

		for (BlockLiftedByPlatformEntity neighbor : entity.world.getEntitiesWithinAABB(BlockLiftedByPlatformEntity.class, area)) {
			if (neighbor == entity || neighbor.removed || neighbor.getBlock() == null) {
				continue;
			}

			int dx = (int) Math.round(neighbor.x - entity.x);
			int dz = (int) Math.round(neighbor.z - entity.z);

			if (Math.abs(dx) + Math.abs(dz) == 1) {
				container.setBlock(pos.x + dx, pos.y, pos.z + dz, neighbor.getBlockId(), neighbor.getBlockData(), null);
				container.setBlock(pos.x + dx, pos.y - 1, pos.z + dz, BTXBlocks.PLATFORM.id(), 0, null);
			}
		}
	}
}