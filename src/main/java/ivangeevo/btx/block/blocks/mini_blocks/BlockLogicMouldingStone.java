package ivangeevo.btx.block.blocks.mini_blocks;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class BlockLogicMouldingStone extends BlockLogicMoulding {
	public BlockLogicMouldingStone(@NotNull Block<?> block, Supplier<Block<?>> matchingCorner) {
		super(block, matchingCorner, Materials.STONE);
	}

	public static BlockLogicSupplier<BlockLogicMouldingStone> stoneProperties(Block<?> source, Supplier<Block<?>> matchingCorner) {
		return block -> new BlockLogicMouldingStone(BlockLogicSidingStone.withStoneProperties(block, source), matchingCorner);
	}

	@Override
	public boolean canBePainted() {
		return false;
	}

	@Override
	public void setColor(@NotNull World world, @NotNull TilePosc tilePos, @NotNull DyeColor color) {
	}
}