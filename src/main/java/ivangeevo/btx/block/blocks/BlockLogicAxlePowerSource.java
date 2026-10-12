package ivangeevo.btx.block.blocks;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BlockLogicAxlePowerSource extends BlockLogicAxle {

	public BlockLogicAxlePowerSource(@NotNull Block<?> block) {
		super(block);
	}

	public static BlockLogicSupplier<BlockLogicAxlePowerSource> powerSourceProperties() {
		return block -> new BlockLogicAxlePowerSource(
			block
				.withSound(BlockSounds.WOOD)
				.withHardness(2.0F)
				.withTags(BlockTags.MINEABLE_BY_AXE)
		);
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		// override of super to prevent power level validation
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		// override of super to prevent power level validation
	}

	@Override
	public int getMechanicalPowerLevelProvidedToAxleAtFacing(World world, TilePosc tilePos, Direction facing) {
		int alignment = getAxisAlignment(world, tilePos);

		if ((facing.id >> 1) == alignment) {
			return 4;
		}

		return 0;
	}

	@Override
	protected void validatePowerLevel(World world, TilePosc tilePos) {
		// power source axles are validated externally by wht entity is providing power
	}

	@Override
	public int getPowerLevel(WorldSource source, TilePosc tilePos) {
		return 4;
	}

	@Override
	public int getPowerLevelFromBlockData(int data) {
		return 4;
	}

	@Override
	public void setPowerLevel(World world, TilePosc tilePos, int powerLevel) {}

	@Override
	public int setPowerLevelInBlockData(int data, int powerLevel) {
		return data;
	}

	@Override
	public void setPowerLevelWithoutNotify(World world, TilePosc tilePos, int powerLevel) {}


	@Override
	public boolean clientCheckIfPowered(WorldSource worldSource, TilePosc tilePos) {
		return true;
	}
}
