package ivangeevo.btx.util;

import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;

public interface TurntableAware {
	default boolean canRotateOnTurntable(WorldSource source, TilePosc tilePos) {
		return source.isBlockNormalCube(tilePos);
	}
	default boolean canTransmitRotationHorizontally(WorldSource source, TilePosc tilePos) {
		return source.isBlockNormalCube(tilePos);
	}
	default boolean canTransmitRotationVertically(WorldSource source, TilePosc tilePos) {
		return source.isBlockNormalCube(tilePos);
	}
}
