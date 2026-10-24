package ivangeevo.btx.block.blocks;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSupplier;
import org.jetbrains.annotations.NotNull;

public class BlockLogicDetectorLogicGlowing extends BlockLogicDetectorLogic {
	public BlockLogicDetectorLogicGlowing(@NotNull Block<?> block) {
		super(block);
	}

	public static BlockLogicSupplier<BlockLogicDetectorLogicGlowing> glowingProperties() {
		return block -> new BlockLogicDetectorLogicGlowing(logicProperties(block).withLightEmission(1.0F));
	}
}