package ivangeevo.btx.block.blocks.mini_blocks;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public class BlockLogicCornerStone extends BlockLogicCorner {
	public BlockLogicCornerStone(@NotNull Block<?> block) {
		super(block, Materials.STONE);
	}

	public static BlockLogicSupplier<BlockLogicCornerStone> stoneProperties(Block<?> source) {
		return block -> new BlockLogicCornerStone(BlockLogicSidingStone.withStoneProperties(block, source));
	}

	@Override
	public boolean canBePainted() {
		return false;
	}

	@Override
	public void setColor(@NotNull World world, @NotNull TilePosc tilePos, @NotNull DyeColor color) {
	}
}