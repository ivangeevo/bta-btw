package ivangeevo.btx.block.blocks.turntable;

import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;

public interface TurntableInteraction {
	default boolean canRotateOnTurntable(WorldSource source, TilePosc tilePos) {
		return source.isBlockNormalCube(tilePos);
	}
	default boolean canTransmitRotationHorizontally(WorldSource source, TilePosc tilePos) {
		return source.isBlockNormalCube(tilePos);
	}
	default boolean canTransmitRotationVertically(WorldSource source, TilePosc tilePos) {
		return source.isBlockNormalCube(tilePos);
	}

	/** Returns the new crafting counter (used by clay and pottery). Default: no rotation happens. */
	default int rotateOnTurntable(World world, TilePosc pos, boolean reverse, int craftingCounter) {
		return craftingCounter;
	}
}
