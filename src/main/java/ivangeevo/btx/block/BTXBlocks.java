package ivangeevo.btx.block;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.block.blocks.*;
import ivangeevo.btx.config.BTXConfig;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;

public final class BTXBlocks {
	private BTXBlocks() {}

	public static Block<?> HEMP;
	public static Block<?> MILLSTONE;
	public static Block<?> HAND_CRANK;
	public static Block<?> AXLE;
	public static Block<?> AXLE_POWER_SOURCE;
	public static Block<?> GEARBOX;

	private static <T extends BlockLogic> Block<T> registerBlock(String name, BlockLogicSupplier<T> logicSupplier) {
		String namespaceId = BTXMod.MOD_ID + ":block/" + name;
		String key = BTXMod.MOD_ID + "." + name.replace("_", ".");
		return Blocks.register(key, namespaceId, BTXConfig.blockID(), logicSupplier);
	}

	public static void afterBlockInit() {
		HEMP = registerBlock("hemp", BlockLogicCropsHemp.properties());
		MILLSTONE = registerBlock("millstone", BlockLogicMillStone.properties());
		HAND_CRANK = registerBlock("hand_crank", BlockLogicHandCrank.properties());
		AXLE = registerBlock("axle", BlockLogicAxle.properties());
		AXLE_POWER_SOURCE = registerBlock("axle_power_source", BlockLogicAxlePowerSource.powerSourceProperties());
		GEARBOX = registerBlock("gearbox", BlockLogicGearBox.properties());
	}
}
