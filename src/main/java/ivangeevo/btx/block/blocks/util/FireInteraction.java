package ivangeevo.btx.block.blocks.util;

import ivangeevo.btx.util.utils.FireUtils;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public interface FireInteraction {
    /** pos = this block; side = the face of this block touching the fire. */
    default boolean doesInfiniteBurnToFacing(World world, TilePosc pos, Direction side) {
		return false;
	}
    default boolean doesExtinguishFireAbove(World world, TilePosc pos) {
		return false;
	}
	/** BTW: getCanBlockBeIncinerated. Override to change the default rule for this block. */
	default boolean canBeIncinerated(World world, TilePosc pos) {
		return FireUtils.defaultCanBeIncinerated(world, pos);
	}
	default void onBurned(World world, TilePosc pos) {
	}
}
