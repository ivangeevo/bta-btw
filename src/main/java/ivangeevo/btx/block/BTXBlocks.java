package ivangeevo.btx.block;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.block.blocks.BlockLogicCropsHemp;
import ivangeevo.btx.config.BTXConfig;
import ivangeevo.btx.item.BTXItems;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.sound.BlockSounds;

public final class BTXBlocks {
	private BTXBlocks() {}

	public static Block<?> HEMP;

	private static <T extends BlockLogic> Block<T> registerBlock(String name, BlockLogicSupplier<T> logicSupplier) {
		String namespaceId = BTXMod.MOD_ID + ":block/" + name;
		String key = BTXMod.MOD_ID + "." + name.replace("_", ".");

		return Blocks.register(key, namespaceId, BTXConfig.blockID(), logicSupplier);
	}

	public static void afterBlockInit() {
		HEMP = registerBlock("hemp",
			b -> new BlockLogicCropsHemp(b))
			.withHardness(0.0f)
			.setStatParent(() -> BTXItems.HEMP_SEEDS)
			.withTags(BlockTags.BROKEN_BY_FLUIDS, BlockTags.NOT_IN_CREATIVE_MENU)
			.withSound(BlockSounds.GRASS);
	}
}
