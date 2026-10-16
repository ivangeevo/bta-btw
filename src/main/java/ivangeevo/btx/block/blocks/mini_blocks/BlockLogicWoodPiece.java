package ivangeevo.btx.block.blocks.mini_blocks;

import ivangeevo.btx.util.TurntableAware;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.material.Materials;
import org.jetbrains.annotations.NotNull;

public abstract class BlockLogicWoodPiece extends BlockLogic implements TurntableAware {
	protected BlockLogicWoodPiece(@NotNull Block<?> block) {
		super(block, Materials.WOOD);
	}

	@Override
	public boolean isSolidRender() {
		return false;
	}

	@Override
	public boolean isCubeShaped() {
		return false;
	}

	protected static boolean isClickOffsetOnAxis(double click) {
		return click >= 0.5D;
	}
}
