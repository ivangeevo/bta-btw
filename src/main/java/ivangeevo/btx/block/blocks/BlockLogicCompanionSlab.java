package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.blocks.turntable.TurntableInteraction;
import ivangeevo.btx.item.items.ItemBlockCompanionSlab;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSlab;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public class BlockLogicCompanionSlab extends BlockLogicSlab implements TurntableInteraction {
	public BlockLogicCompanionSlab(@NotNull Block<?> block, @NotNull Block<?> cube) {
		super(block, cube, 0);
		block.setBlockItem(() -> new ItemBlockCompanionSlab<>(block));
	}

	public static BlockLogicSupplier<BlockLogicCompanionSlab> properties(@NotNull Block<?> cube) {
		return block -> new BlockLogicCompanionSlab(
			block.withSound(BlockSounds.CLOTH),
			cube
		);
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		this.onNeighborChanged(world, tilePos, Blocks.AIR);
	}

	@Override
	public boolean canRotateOnTurntable(WorldSource source, TilePosc tilePos) {
		return false;
	}

	@Override
	public boolean canTransmitRotationHorizontally(WorldSource source, TilePosc tilePos) {
		return false;
	}

	@Override
	public boolean canTransmitRotationVertically(WorldSource source, TilePosc tilePos) {
		return false;
	}
}
