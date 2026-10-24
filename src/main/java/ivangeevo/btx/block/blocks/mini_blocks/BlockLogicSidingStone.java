package ivangeevo.btx.block.blocks.mini_blocks;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public class BlockLogicSidingStone extends BlockLogicSiding {
	public BlockLogicSidingStone(@NotNull Block<?> block) {
		super(block, Materials.STONE);
	}

	public static BlockLogicSupplier<BlockLogicSidingStone> stoneProperties(Block<?> source) {
		return block -> new BlockLogicSidingStone(withStoneProperties(block, source));
	}

	static Block<?> withStoneProperties(Block<?> block, Block<?> source) {
		return block
			.withSound(source.getSound())
			.withHardness(source.getHardness())
			.withBlastResistance(10.0F)
			.withTags(BlockTags.MINEABLE_BY_PICKAXE);
	}

	@Override
	public boolean canBePainted() {
		return false;
	}

	@Override
	public void setColor(@NotNull World world, @NotNull TilePosc tilePos, @NotNull DyeColor color) {
	}
}