package ivangeevo.btx.block.blocks.mini_blocks;

import ivangeevo.btx.block.blocks.turntable.TurntableInteraction;
import ivangeevo.btx.util.utils.SupportUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.support.ISupport;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public abstract class BlockLogicWoodPiece extends BlockLogic implements TurntableInteraction {
	protected BlockLogicWoodPiece(@NotNull Block<?> block) {
		this(block, Materials.WOOD);
	}

	protected BlockLogicWoodPiece(@NotNull Block<?> block, @NotNull Material material) {
		super(block, material);
	}

	@Override
	public boolean isSolidRender() {
		return false;
	}

	@Override
	public boolean isCubeShaped() {
		return false;
	}

	@Override
	public @NotNull ISupport getSupport(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side) {
		return SupportUtils.fromBounds(this.getBoundsFromState(world, tilePos), side);
	}

	protected static boolean isClickOffsetOnAxis(double click) {
		return click >= 0.5D;
	}
}
