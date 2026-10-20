package ivangeevo.btx.util.utils;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.util.FireInteraction;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicFire;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;

public final class FireUtils {
	private FireUtils() {}

	public static boolean prefersSideDisplay(WorldSource source, TilePosc firePos) {
		TilePos below = firePos.down(new TilePos());
		Block<?> blockBelow = source.getBlockType(below);
		if (BlockLogicFire.canBurn(source, below) || blockBelow.hasTag(BlockTags.INFINITE_BURN) || blockBelow == BTXBlocks.HIBACHI) {
			return false;
		}

		TilePos neighbor = new TilePos();
		for (Direction direction : new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.UP}) {
			neighbor.set(firePos.x(), firePos.y(), firePos.z());
			neighbor.add(direction);
			if (source.getBlockType(neighbor).getLogic() instanceof FireInteraction fi && fi.canFireCling(source, neighbor)) {
				return true;
			}
		}
		return false;
	}

	/** BTW: hasInfiniteBurnNeighbor. True if any neighbor says fire at pos burns forever against its face. */
	public static boolean hasInfiniteBurnNeighbor(World world, TilePosc pos) {
		TilePos target = new TilePos();
		for (int facing = 0; facing < 6; facing++) {
			target.set(pos.x(), pos.y(), pos.z());
			target.add(Direction.fromId(facing));
			if (world.getBlockType(target).getLogic() instanceof FireInteraction fi
				&& fi.doesInfiniteBurnToFacing(world, target, Direction.fromId(facing ^ 1))) {
				return true;
			}
		}
		return false;
	}

	/** BTW: doesBlockBelowExtinguishFire. Asks the block under the fire whether it extinguishes fire above it. */
	public static boolean doesBlockBelowExtinguishFire(World world, TilePosc firePos) {
		TilePos below = new TilePos(firePos.x(), firePos.y() - 1, firePos.z());
		return world.getBlockType(below).getLogic() instanceof FireInteraction fi
			&& fi.doesExtinguishFireAbove(world, below);
	}

	/** BTW's base Block rule: flammable, or not movement-blocking (plants, snow layers, etc). */
	public static boolean defaultCanBeIncinerated(World world, TilePosc tilePos) {
		Block<?> block = world.getBlockType(tilePos);
		return BlockLogicFire.canBurn(world, tilePos)
			|| !block.getMaterial().blocksMotion();
	}

	/** What the hibachi calls on the block above it. */
	public static boolean canBlockBeIncinerated(World world, TilePosc tilePos) {
		if (world.getBlockType(tilePos).getLogic() instanceof FireInteraction fi) {
			return fi.canBeIncinerated(world, tilePos);
		}
		return defaultCanBeIncinerated(world, tilePos);
	}
}
