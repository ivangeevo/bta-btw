package ivangeevo.btx.item.items;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicAxle;
import ivangeevo.btx.entity.entities.WaterWheelEntity;
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

public class ItemWaterWheel extends Item {

	public ItemWaterWheel(@NotNull String translationKey, @NotNull String namespaceId, int id) {
		super(translationKey, namespaceId, id);
	}

	@Override
	public boolean onUseOnBlock(@NotNull ItemStack selfStack, @NotNull World world, @Nullable Player player, @NotNull TilePosc blockPos, @NotNull Side side, double xHit, double yHit) {
		Block<?> targetBlock = world.getBlockType(blockPos);

		if (targetBlock == BTXBlocks.AXLE && targetBlock.getLogic() instanceof BlockLogicAxle axle) {
			int axisAlignment = axle.getAxisAlignment(world, blockPos);

			if (axisAlignment != 0) {
				boolean isAligned = axisAlignment == 2;

				WaterWheelEntity waterWheel = new WaterWheelEntity(
					world,
					blockPos.x() + 0.5F,
					blockPos.y() + 0.5F,
					blockPos.z() + 0.5F,
					isAligned
				);

				if (waterWheel.validateAreaAroundDevice()) {
					if (waterWheel.isClearOfBlockingEntities()) {
						if (!world.isClientSide) {
							waterWheel.setRotationSpeed(waterWheel.computeRotation());
							world.entityJoinedWorld(waterWheel);
						}

						selfStack.stackSize--;
					} else {
						if (player != null) {
							player.sendMessageTranslated("messages.waterwheel.placement.obstructed");
						}
					}
				} else {
					if (player != null) {
						player.sendMessageTranslated("messages.waterwheel.not.enough.room");
					}
				}
			}
		}

		return super.onUseOnBlock(selfStack, world, player, blockPos, side, xHit, yHit);
	}

}
