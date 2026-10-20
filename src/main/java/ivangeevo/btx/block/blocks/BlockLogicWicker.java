package ivangeevo.btx.block.blocks;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSlab;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.sound.BlockSounds;
import org.jetbrains.annotations.NotNull;

public final class BlockLogicWicker {
	private BlockLogicWicker() {}

	public static BlockLogicSupplier<BlockLogic> blockProperties() {
		return block -> new BlockLogic(
			block
				.withSound(BlockSounds.GRASS)
				.withHardness(0.5F)
				.withTags(BlockTags.MINEABLE_BY_AXE),
			Materials.WOOD
		);
	}

	public static BlockLogicSupplier<BlockLogicSlab> slabProperties(@NotNull Block<?> fullBlock) {
		return block -> new BlockLogicSlab(
			block
				.withSound(BlockSounds.GRASS)
				.withHardness(0.5F)
				.withTags(BlockTags.MINEABLE_BY_AXE),
			fullBlock
		);
	}
}
