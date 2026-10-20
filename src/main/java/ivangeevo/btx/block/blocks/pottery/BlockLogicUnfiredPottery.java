package ivangeevo.btx.block.blocks.pottery;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

public class BlockLogicUnfiredPottery extends BlockLogic {
	protected BlockLogicUnfiredPottery(@NotNull Block<?> block) {
		super(block, Materials.CLAY);
	}

	/** Shared settings for the pottery blocks **/
	public static BlockLogicSupplier<BlockLogicUnfiredPottery> properties() {
		return block -> new BlockLogicUnfiredPottery(applyCommon(block));
	}

	protected static Block<?> applyCommon(Block<?> block) {
		return block
			.withSound(BlockSounds.GRAVEL)
			.withHardness(0.6F)
			.withTags(BlockTags.MINEABLE_BY_SHOVEL);
	}

	@Override public boolean isSolidRender() {
		return false;
	}

	@Override public boolean isCubeShaped() {
		return false;
	}

	@Override
	public @NotNull AABBdc getBoundsFromState(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return new AABBd(0, 0, 0, 1, 1, 1);
	}
}
