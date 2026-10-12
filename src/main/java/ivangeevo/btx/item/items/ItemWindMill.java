package ivangeevo.btx.item.items;

import ivangeevo.btx.BTXGlobals;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicAxle;
import ivangeevo.btx.entity.entities.WindMillEntity;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemWindMill extends Item {

	public ItemWindMill(@NotNull String translationKey, @NotNull String namespaceId, int id) {
		super(translationKey, namespaceId, id);
	}

	@Override
	public boolean onUseOnBlock(@NotNull ItemStack selfStack, @NotNull World world, @Nullable Player player, @NotNull TilePosc blockPos, @NotNull Side side, double xHit, double yHit) {
		BTXGlobals.LOGGER.info("[WindMill] onUseOnBlock called. clientSide={}, pos={}, side={}, block={}, player={}",
			world.isClientSide, blockPos, side, world.getBlockType(blockPos), player);

		Block<?> targetBlock = world.getBlockType(blockPos);

		if (targetBlock == BTXBlocks.AXLE && targetBlock.getLogic() instanceof BlockLogicAxle axle) {
			int axisAlignment = axle.getAxisAlignment(world, blockPos);
			BTXGlobals.LOGGER.info("[WindMill] Hit AXLE. axisAlignment={}", axisAlignment);

			if (axisAlignment != 0) {
				boolean isAligned = axisAlignment == 2;

				WindMillEntity windMill = new WindMillEntity(
					world,
					blockPos.x() + 0.5F,
					blockPos.y() + 0.5F,
					blockPos.z() + 0.5F,
					isAligned
				);
				boolean areaOk = windMill.validateAreaAroundDevice();
				BTXGlobals.LOGGER.info("[WindMill] validateArea={}, entityPos=({}, {}, {}), alignedToX={}",
					areaOk, windMill.x, windMill.y, windMill.z, windMill.alignedToX);

				if (windMill.validateAreaAroundDevice()) {
					boolean clear = windMill.isClearOfBlockingEntities();
					BTXGlobals.LOGGER.info("[WindMill] isClearOfBlockingEntities={}", clear);

					if (windMill.isClearOfBlockingEntities()) {
						if (!world.isClientSide) {
							BTXGlobals.LOGGER.info("[WindMill] Server: calling entityJoinedWorld");

							windMill.setRotationSpeed(windMill.computeRotation());
							//world.entityJoinedWorld(windMill);
							boolean joined = world.entityJoinedWorld(windMill);
							BTXGlobals.LOGGER.info("[WindMill] entityJoinedWorld returned {}", joined);

						}

						selfStack.stackSize--;
					} else {
						if (world.isClientSide && player != null) {
							player.sendMessageTranslated("messages.windMill.placementObstructed");
						}
					}
				} else {
					if (world.isClientSide && player != null) {
						player.sendMessageTranslated("messages.windMill.notEnoughRoom");
					}
				}
			}
		} else {
			BTXGlobals.LOGGER.info("[WindMill] Not an axle (block={}), ignoring", targetBlock);
		}


		return super.onUseOnBlock(selfStack, world, player, blockPos, side, xHit, yHit);
	}
}
