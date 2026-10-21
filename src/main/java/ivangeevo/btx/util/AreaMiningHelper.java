package ivangeevo.btx.util;

import ivangeevo.btx.item.items.AreaMiningTool;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;

import java.util.ArrayList;
import java.util.List;

public class AreaMiningHelper {
	public static final float AREA_SPEED_FACTOR = 0.33F;
	private static final String AREA_MODE_KEY = "btxAreaMining";
	private static final int BLOCK_BREAK_EVENT = 2001;

	public static boolean isAreaMode(ItemStack stack) {
		return !stack.getData().containsKey(AREA_MODE_KEY) || stack.getData().getBoolean(AREA_MODE_KEY);
	}

	public static void toggleAreaMode(ItemStack stack, World world, Player player) {
		boolean areaMode = !isAreaMode(stack);
		stack.getData().putBoolean(AREA_MODE_KEY, areaMode);

		if (!world.isClientSide) {
			if (areaMode) {
				SoulforgedSounds.playSoulActivate(world, player);
			} else {
				SoulforgedSounds.playSoulDeactivate(world, player);
			}
			player.sendStatusMessageTranslated(areaMode ? "messages.area.mining.on" : "messages.area.mining.off");
		}
	}

	public static float getStrVsBlock(ItemStack stack, float baseStr) {
		return isAreaMode(stack) && baseStr > 1.0F ? baseStr * AREA_SPEED_FACTOR : baseStr;
	}

	public static void breakArea(AreaMiningTool tool, ItemStack stack, World world, Player player, Block<?> centerBlock, TilePosc center, Side side) {
		if (world.isClientSide || !isAreaMode(stack) || SoulforgedToolHelper.isBrokenTool(stack)) {
			return;
		}

		for (TilePos target : getExtraTargets(tool, stack, world, player, centerBlock.getHardness(), center, side)) {
			breakBlock(stack, world, player, target, side);

			stack.damageItem(1, player);
			if (stack.stackSize <= 0) {
				player.destroyCurrentEquippedItem();
				return;
			}
		}
	}

	public static List<TilePos> getExtraTargets(AreaMiningTool tool, ItemStack stack, World world, Player player, float centerHardness, TilePosc center, Side side) {
		List<TilePos> targets = new ArrayList<>(8);

		for (int a = -1; a <= 1; a++) {
			for (int b = -1; b <= 1; b++) {
				if (a == 0 && b == 0) {
					continue;
				}

				TilePos target;
				if (side.offsetX() != 0) {
					target = new TilePos(center.x(), center.y() + a, center.z() + b);
				} else if (side.offsetY() != 0) {
					target = new TilePos(center.x() + a, center.y(), center.z() + b);
				} else {
					target = new TilePos(center.x() + a, center.y() + b, center.z());
				}

				if (canAreaMine(tool, stack, world, player, target, centerHardness)) {
					targets.add(target);
				}
			}
		}

		return targets;
	}

	private static boolean canAreaMine(AreaMiningTool tool, ItemStack stack, World world, Player player, TilePos pos, float centerHardness) {
		Block<?> block = world.getBlockType(pos);
		if (block == Blocks.AIR) {
			return false;
		}

		float hardness = block.getHardness();
		if (hardness < 0.0F || hardness > centerHardness) {
			return false;
		}

		return tool.getBaseStrVsBlock(stack, block) > 1.0F && tool.canHarvestBlock(stack, player, block);
	}

	private static void breakBlock(ItemStack stack, World world, Player player, TilePos pos, Side side) {
		Block<?> block = world.getBlockType(pos);
		int data = world.getBlockData(pos);
		TileEntity tileEntity = world.getTileEntity(pos);

		world.playBlockEvent(pos, BLOCK_BREAK_EVENT, block.id());
		world.setBlockTypeNotify(pos, Blocks.AIR);
		block.onDestroyedByPlayer(world, pos, side, data, player, stack.getItem());
		block.dropWithCause(world, EnumDropCause.PROPER_TOOL, pos, data, tileEntity, player);
	}
}
