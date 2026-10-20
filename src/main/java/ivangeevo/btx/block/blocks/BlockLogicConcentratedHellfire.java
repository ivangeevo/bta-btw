package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.blocks.util.FireInteraction;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public class BlockLogicConcentratedHellfire extends BlockLogic implements FireInteraction {
	public BlockLogicConcentratedHellfire(@NotNull Block<?> block) {
		super(block, Materials.STONE);
	}

	public static BlockLogicSupplier<BlockLogicConcentratedHellfire> properties() {
		return block -> new BlockLogicConcentratedHellfire(
			block
				.withSound(BlockSounds.STONE)
				.withHardness(2.0F)
				.withTags(BlockTags.MINEABLE_BY_PICKAXE, BlockTags.MINEABLE_BY_AXE)
		);
	}

	@Override
	public boolean doesInfiniteBurnToFacing(World world, TilePosc pos, Direction side) {
		return true;
	}

	@Override
	public boolean canBeIncinerated(World world, TilePosc pos) {
		return false;
	}

	@Override
	public boolean canFireCling(WorldSource source, TilePosc pos) {
		return true;
	}
}