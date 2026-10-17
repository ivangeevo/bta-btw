package ivangeevo.btx.block.blocks.kiln;

import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;

public interface KilnInteraction {
    default boolean getCanBeCookedByKiln(WorldSource source, TilePosc pos) {
		return false;
	}
    default void onCookedByKiln(World world, TilePosc pos) {

	}
    default int getCookTimeMultiplierInKiln(WorldSource source, TilePosc pos) {
		return 1;
	}
}
