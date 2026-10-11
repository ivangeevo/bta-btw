package ivangeevo.btx.util;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicAxle;
import ivangeevo.btx.block.blocks.BlockLogicGearBox;
import ivangeevo.btx.block.blocks.MechanicalDevice;
import net.minecraft.core.block.Block;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;

/** Subset of BTW's MechPowerUtils. Facings use BTW numbering (see BlockLogicAxle). */
public final class MechPowerUtils {
	private MechPowerUtils() {}

	public static boolean isBlockPoweredByAxleToSide(World world, TilePosc tilePos, int side) {
		TilePos targetPos = new TilePos();
		BlockLogicAxle.addFacingAsOffset(tilePos, side, targetPos);

		Block<?> targetBlock = world.getBlockType(targetPos);

		if (BlockLogicAxle.isBlockAxle(targetBlock) && targetBlock.getLogic() instanceof BlockLogicAxle axle) {
			if (axle.isAxleOrientedTowardsFacing(world, targetPos, side)) {
				return axle.getPowerLevel(world, targetPos) > 0;
			}
		}

		return false;
	}

	public static boolean isBlockPoweredByAxle(World world, TilePosc tilePos, MechanicalDevice block) {
		for (int facing = 0; facing <= 5; facing++) {
			if (block.canInputAxlePowerToFacing(world, tilePos, Direction.fromId(facing))) {
				if (isBlockPoweredByAxleToSide(world, tilePos, facing)) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean doesBlockHaveFacingAxleToSide(WorldSource worldSource, TilePosc tilePos, int side) {
		TilePos targetPos = new TilePos();
		BlockLogicAxle.addFacingAsOffset(tilePos, side, targetPos);

		Block<?> targetBlock = worldSource.getBlockType(targetPos);

		return BlockLogicAxle.isBlockAxle(targetBlock)
			&& targetBlock.getLogic() instanceof BlockLogicAxle axle
			&& axle.isAxleOrientedTowardsFacing(worldSource, targetPos, side);
	}

	public static void destroyHorizontallyAttachedAxles(World world, TilePosc tilePos) {
		TilePos tempPos = new TilePos();

		for (int facing = 2; facing <= 5; facing++) {
			BlockLogicAxle.addFacingAsOffset(tilePos, facing, tempPos);

			Block<?> tempBlock = world.getBlockType(tempPos);

			if (BlockLogicAxle.isBlockAxle(tempBlock) && tempBlock.getLogic() instanceof BlockLogicAxle axle) {
				if (axle.getPowerLevel(world, tempPos) > 0 && axle.isAxleOrientedTowardsFacing(world, tempPos, facing)) {
					axle.breakAxle(world, tempPos);
				}
			}
		}
	}

	public static boolean isPoweredGearBox(WorldSource source, TilePosc tilePos) {
		Block<?> tempBlock = source.getBlockType(tilePos);

		if (tempBlock == BTXBlocks.GEARBOX) {
			if (tempBlock.getLogic() instanceof BlockLogicGearBox g) {
				return g.isGearBoxOn(source, tilePos);
			}
		}

		return false;
	}
}
