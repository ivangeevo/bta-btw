package ivangeevo.btx.block;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.block.blocks.*;
import ivangeevo.btx.block.blocks.mini_blocks.*;
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
	public static Block<?> SIDING_OAK;
	public static Block<BlockLogicSidingPainted> SIDING_OAK_PAINTED;
	public static Block<BlockLogicMoulding> MOULDING_OAK;
	public static Block<BlockLogicMouldingPainted> MOULDING_OAK_PAINTED;
	public static Block<?> CORNER_OAK;
	public static Block<BlockLogicCornerPainted> CORNER_OAK_PAINTED;

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
		SIDING_OAK = registerBlock("siding_oak", BlockLogicSiding.properties());
		SIDING_OAK_PAINTED = registerBlock("siding_oak_painted", BlockLogicSidingPainted.properties());
		MOULDING_OAK = registerBlock("moulding_oak", BlockLogicMoulding.properties());
		MOULDING_OAK_PAINTED = registerBlock("moulding_oak_painted", BlockLogicMouldingPainted.properties());
		CORNER_OAK = registerBlock("corner_oak", BlockLogicCorner.properties());
		CORNER_OAK_PAINTED = registerBlock("corner_oak_painted", BlockLogicCornerPainted.properties());
	}
}
