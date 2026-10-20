package ivangeevo.btx.block;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.block.blocks.*;
import ivangeevo.btx.block.blocks.kiln.BlockLogicKiln;
import ivangeevo.btx.block.blocks.mini_blocks.*;
import ivangeevo.btx.block.blocks.pottery.BlockLogicPlanter;
import ivangeevo.btx.block.blocks.pottery.BlockLogicPlanterGrass;
import ivangeevo.btx.block.blocks.pottery.BlockLogicPlanterSoil;
import ivangeevo.btx.block.blocks.pottery.BlockLogicUnfiredPottery;
import ivangeevo.btx.block.blocks.pottery.BlockLogicUnfiredVase;
import ivangeevo.btx.block.blocks.pottery.BlockLogicVase;
import ivangeevo.btx.block.blocks.pottery.BlockLogicVasePainted;
import ivangeevo.btx.block.blocks.turntable.BlockLogicTurntable;
import ivangeevo.btx.config.BTXConfig;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicFire;
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
	public static Block<?> HIBACHI;
	public static Block<?> STOKED_FIRE;
	public static Block<BlockLogicTurntable> TURNTABLE;
	public static Block<BlockLogicBellows> BELLOWS;
	public static Block<?> UNFIRED_CRUCIBLE;
	public static Block<?> UNFIRED_PLANTER;
	public static Block<?> UNFIRED_VASE;
	public static Block<BlockLogicCrucible> CRUCIBLE;
	public static Block<BlockLogicCauldron> CAULDRON;
	public static Block<?> PLANTER;
	public static Block<BlockLogicPlanterSoil> PLANTER_SOIL;
	public static Block<BlockLogicPlanterGrass> PLANTER_GRASS;
	public static Block<BlockLogicPlanterGrass> PLANTER_GRASS_RETRO;
	public static Block<BlockLogicPlanterGrass> PLANTER_GRASS_SCORCHED;
	public static Block<BlockLogicVase> VASE;
	public static Block<BlockLogicVasePainted> VASE_PAINTED;
	public static Block<BlockLogicKiln> KILN;
	public static Block<BlockLogicHopper> HOPPER;
	public static Block<BlockLogicPaneBTX> GRATE;
	public static Block<BlockLogicPaneBTX> SLATS;
	public static Block<BlockLogicPaneBTX> WICKER_PANE;
	public static Block<?> WICKER_BLOCK;
	public static Block<?> WICKER_SLAB;
	public static Block<BlockLogicCompanionCube> COMPANION_CUBE;
	public static Block<BlockLogicCompanionSlab> COMPANION_SLAB;

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
		HIBACHI = registerBlock("hibachi", BlockLogicHibachi.properties());
		STOKED_FIRE = registerBlock("stoked_fire", BlockLogicStokedFire.properties());
		TURNTABLE = registerBlock("turntable", BlockLogicTurntable.properties());
		BELLOWS = registerBlock("bellows", BlockLogicBellows.properties());
		UNFIRED_CRUCIBLE = registerBlock("unfired_crucible", BlockLogicUnfiredPottery.properties());
		UNFIRED_PLANTER = registerBlock("unfired_planter", BlockLogicUnfiredPottery.properties());
		UNFIRED_VASE = registerBlock("unfired_vase", BlockLogicUnfiredVase.vaseProperties());
		KILN = registerBlock("kiln", BlockLogicKiln.properties());
		CRUCIBLE = registerBlock("crucible", BlockLogicCrucible.properties());
		CAULDRON = registerBlock("cauldron", BlockLogicCauldron.properties());
		PLANTER = registerBlock("planter", BlockLogicPlanter.properties());
		PLANTER_SOIL = registerBlock("planter_soil", BlockLogicPlanterSoil.soilProperties());
		PLANTER_GRASS = registerBlock("planter_grass", BlockLogicPlanterGrass.grassProperties(Blocks.GRASS));
		PLANTER_GRASS_RETRO = registerBlock("planter_grass_retro", BlockLogicPlanterGrass.grassProperties(Blocks.GRASS_RETRO));
		PLANTER_GRASS_SCORCHED = registerBlock("planter_grass_scorched", BlockLogicPlanterGrass.grassProperties(Blocks.GRASS_SCORCHED));
		VASE = registerBlock("vase", BlockLogicVase.properties());
		VASE_PAINTED = registerBlock("vase_painted", BlockLogicVasePainted.paintedProperties());
		HOPPER = registerBlock("hopper", BlockLogicHopper.properties());
		GRATE = registerBlock("grate", BlockLogicPaneBTX.woodProperties(2));
		SLATS = registerBlock("slats", BlockLogicPaneBTX.woodProperties(4));
		WICKER_PANE = registerBlock("wicker_pane", BlockLogicPaneBTX.wickerProperties());
		WICKER_BLOCK = registerBlock("wicker_block", BlockLogicWicker.blockProperties());
		WICKER_SLAB = registerBlock("wicker_slab", BlockLogicWicker.slabProperties(WICKER_BLOCK));
		COMPANION_CUBE = registerBlock("companion_cube", BlockLogicCompanionCube.properties());
		COMPANION_SLAB = registerBlock("companion_slab", BlockLogicCompanionSlab.properties(COMPANION_CUBE));

		// BTW CE: Flammability.PLANKS (5, 20) and Flammability.WICKER (60, 100)
		BlockLogicFire.setFlammable(GRATE, 5, 20);
		BlockLogicFire.setFlammable(SLATS, 5, 20);
		BlockLogicFire.setFlammable(WICKER_PANE, 60, 100);
		BlockLogicFire.setFlammable(WICKER_BLOCK, 60, 100);
		BlockLogicFire.setFlammable(WICKER_SLAB, 60, 100);
		BlockLogicFire.setFlammable(COMPANION_CUBE, 30, 60);
		BlockLogicFire.setFlammable(COMPANION_SLAB, 30, 60);
	}
}
