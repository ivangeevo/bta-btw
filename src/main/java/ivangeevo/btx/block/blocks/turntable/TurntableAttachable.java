package ivangeevo.btx.block.blocks.turntable;

import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public interface TurntableAttachable {
	/** Is this block attached to the block at the opposite side (the column block)? */
	default boolean canRotateAroundBlockOnTurntableToFacing(World world, TilePosc pos, Direction facing) { return false; }

	/** Called when the rotation happens. Return false to veto (or after handling it yourself, e.g. dropping). */
	default boolean onRotatedAroundBlockOnTurntableToFacing(World world, TilePosc pos, Direction facing) { return true; }

	/** Data the block has after moving from facing to newFacing. */
	default int getNewMetadataRotatedAroundBlockOnTurntableToFacing(World world, TilePosc pos, Direction facing, Direction newFacing) {
		return world.getBlockData(pos);
	}
}
