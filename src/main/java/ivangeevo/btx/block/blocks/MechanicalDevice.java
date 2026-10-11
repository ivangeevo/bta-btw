package ivangeevo.btx.block.blocks;

import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public interface MechanicalDevice {
	boolean canOutputMechanicalPower();
	boolean canInputMechanicalPower();
	boolean isInputtingMechanicalPower(World world, TilePosc tilePos);
	boolean isOutputtingMechanicalPower(World world, TilePosc tilePos);
	default int getMechanicalPowerLevelProvidedToAxleAtFacing(World world, TilePosc tilePos, Direction facing) { return 0; }
	default boolean canInputAxlePowerToFacing(World world, TilePosc tilePos, Direction facing) { return false; }
	default void overpower(World world, TilePosc tilePos) {}
}
