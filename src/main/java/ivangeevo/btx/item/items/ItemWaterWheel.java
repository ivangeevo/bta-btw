package ivangeevo.btx.item.items;

import ivangeevo.btx.achievement.BTXAchievementTriggers;
import ivangeevo.btx.achievement.BTXAchievements;
import ivangeevo.btx.util.utils.MechPowerUtils;
import ivangeevo.btx.block.blocks.BlockLogicAxle;
import ivangeevo.btx.entity.entities.WaterWheelEntity;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemWaterWheel extends Item {
	private static final DyeColor[] DYES = DyeColor.values();

	public ItemWaterWheel(@NotNull String translationKey, @NotNull String namespaceId, int id) {
		super(translationKey, namespaceId, id);
	}

	public static int metaFromColor(int color) {
		return color < 0 || color >= DYES.length ? 0 : color + 1;
	}

	public static int colorFromMeta(int meta) {
		return meta <= 0 || meta > DYES.length ? WaterWheelEntity.UNPAINTED : meta - 1;
	}

	@NotNull
	@Override
	public String getLanguageKey(@NotNull ItemStack selfStack) {
		int color = colorFromMeta(selfStack.getMetadata());
		return color == WaterWheelEntity.UNPAINTED ? super.getLanguageKey(selfStack) : getKey() + "." + DYES[color].colorID;
	}

	@NotNull
	@Override
	public String getTranslatedDescription(@NotNull ItemStack selfStack) {
		return I18n.getInstance().translateKey(getKey() + ".desc");
	}

	@Override
	public boolean onUseOnBlock(@NotNull ItemStack selfStack, @NotNull World world, @Nullable Player player, @NotNull TilePosc blockPos, @NotNull Side side, double xHit, double yHit) {
		Block<?> targetBlock = world.getBlockType(blockPos);

		if (MechPowerUtils.isPlainAxle(targetBlock) && targetBlock.getLogic() instanceof BlockLogicAxle axle) {
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
							waterWheel.setColor(colorFromMeta(selfStack.getMetadata()));
							waterWheel.setRotationSpeed(waterWheel.computeRotation());
							world.entityJoinedWorld(waterWheel);
							BTXAchievementTriggers.award(player, BTXAchievements.WATERWHEEL);
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
