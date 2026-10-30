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
import net.minecraft.core.block.BlockLogicPressurePlate;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.BlockSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class BTXBlocks {
	private BTXBlocks() {}

	public static Block<?> HEMP;
	public static Block<?> MILLSTONE;
	public static Block<?> HAND_CRANK;
	public static Block<?> AXLE;
	public static Block<?> AXLE_POWER_SOURCE;
	public static Block<?> GEARBOX;
	public static Block<?> AXLE_PAINTED;
	public static Block<?> AXLE_POWER_SOURCE_PAINTED;
	public static Block<?> GEARBOX_PAINTED;
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
	public static Block<BlockLogicConcentratedHellfire> CONCENTRATED_HELLFIRE_BLOCK;
	public static Block<BlockLogicSoulforge> SOULFORGE;
	public static Block<BlockLogicSaw> SAW;
	public static Block<BlockLogicSidingStone> SIDING_STONE;
	public static Block<BlockLogicBlockDispenser> BLOCK_DISPENSER;
	public static Block<BlockLogicAnchor> ANCHOR;
	public static Block<BlockLogicPulley> PULLEY;
	public static Block<BlockLogicPlatform> PLATFORM;
	public static Block<BlockLogicDetector> DETECTOR_BLOCK;
	public static Block<BlockLogicDetectorLogic> DETECTOR_LOGIC;
	public static Block<BlockLogicDetectorLogicGlowing> DETECTOR_LOGIC_GLOWING;
	public static Block<BlockLogicLens> LENS;
	public static Block<BlockLogicLightBulb> LIGHT_BULB;
	public static Block<BlockLogicLightBulb> LIGHT_BULB_ON;
	public static Block<BlockLogicCement> CEMENT;
	public static Block<BlockLogicBuddy> BUDDY_BLOCK;
	public static Block<BlockLogicPressurePlate<Player>> SOULFORGED_STEEL_PRESSURE_PLATE;
	public static Block<BlockLogicRailDetectorTiered> DETECTOR_RAIL_STONE;
	public static Block<BlockLogicRailDetectorTiered> DETECTOR_RAIL_SOULFORGED_STEEL;

	public record StonePieces(String type, Block<?> source, Block<BlockLogicSidingStone> siding,
							  Block<BlockLogicMouldingStone> moulding, Block<BlockLogicCornerStone> corner) {
	}

	public static final List<StonePieces> STONE_PIECES = new ArrayList<>();

	private static <T extends BlockLogic> Block<T> registerBlock(String name, BlockLogicSupplier<T> logicSupplier) {
		String namespaceId = BTXMod.MOD_ID + ":block/" + name;
		String key = BTXMod.MOD_ID + "." + name.replace("_", ".");
		return Blocks.register(key, namespaceId, BTXConfig.blockID(), logicSupplier);
	}

	private static void registerStonePieces(String type, Block<?> source, @Nullable Block<BlockLogicSidingStone> siding) {
		if (siding == null) {
			siding = registerBlock("siding_" + type, BlockLogicSidingStone.stoneProperties(source));
		}

		List<Block<BlockLogicCornerStone>> corner = new ArrayList<>(1);
		Block<BlockLogicMouldingStone> moulding = registerBlock("moulding_" + type, BlockLogicMouldingStone.stoneProperties(source, () -> corner.get(0)));
		corner.add(registerBlock("corner_" + type, BlockLogicCornerStone.stoneProperties(source)));

		STONE_PIECES.add(new StonePieces(type, source, siding, moulding, corner.get(0)));
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
		CONCENTRATED_HELLFIRE_BLOCK = registerBlock("concentrated_hellfire_block", BlockLogicConcentratedHellfire.properties());
		SOULFORGE = registerBlock("soulforge", BlockLogicSoulforge.properties());
		SAW = registerBlock("saw", BlockLogicSaw.properties());
		SIDING_STONE = registerBlock("siding_stone", BlockLogicSidingStone.stoneProperties(Blocks.STONE));
		registerStonePieces("stone", Blocks.STONE, SIDING_STONE);
		registerStonePieces("basalt", Blocks.BASALT, null);
		registerStonePieces("limestone", Blocks.LIMESTONE, null);
		registerStonePieces("granite", Blocks.GRANITE, null);
		registerStonePieces("marble", Blocks.MARBLE, null);
		registerStonePieces("slate", Blocks.SLATE, null);
		registerStonePieces("permafrost", Blocks.PERMAFROST, null);
		BLOCK_DISPENSER = registerBlock("block_dispenser", BlockLogicBlockDispenser.properties());
		ANCHOR = registerBlock("anchor", BlockLogicAnchor.properties());
		PULLEY = registerBlock("pulley", BlockLogicPulley.properties());
		PLATFORM = registerBlock("platform", BlockLogicPlatform.properties());
		DETECTOR_BLOCK = registerBlock("detector_block", BlockLogicDetector.properties());
		DETECTOR_LOGIC = registerBlock("detector_logic", BlockLogicDetectorLogic.properties());
		DETECTOR_LOGIC_GLOWING = registerBlock("detector_logic_glowing", BlockLogicDetectorLogicGlowing.glowingProperties());
		LENS = registerBlock("lens", BlockLogicLens.properties());
		LIGHT_BULB = registerBlock("light_bulb", BlockLogicLightBulb.properties(false));
		LIGHT_BULB_ON = registerBlock("light_bulb_on", BlockLogicLightBulb.properties(true));
		CEMENT = registerBlock("cement", BlockLogicCement.properties());
		BUDDY_BLOCK = registerBlock("buddy_block", BlockLogicBuddy.properties());
		SOULFORGED_STEEL_PRESSURE_PLATE = registerBlock("pressure_plate_soulforged_steel", block -> new BlockLogicPressurePlate<>(
			block
				.withSound(BlockSounds.METAL)
				.withHardness(1.0F)
				.withBlastResistance(2000.0F)
				.withTags(BlockTags.MINEABLE_BY_PICKAXE, BlockTags.PREVENT_MOB_SPAWNS),
			Player.class, Materials.METAL));
		DETECTOR_RAIL_STONE = registerBlock("detector_rail_stone", BlockLogicRailDetectorTiered.properties(BlockLogicRailDetectorTiered.Tier.STONE));
		DETECTOR_RAIL_SOULFORGED_STEEL = registerBlock("detector_rail_soulforged_steel", BlockLogicRailDetectorTiered.properties(BlockLogicRailDetectorTiered.Tier.SOULFORGED_STEEL));
		AXLE_PAINTED = registerBlock("axle_painted", BlockLogicAxlePainted.paintedProperties());
		AXLE_POWER_SOURCE_PAINTED = registerBlock("axle_power_source_painted", BlockLogicAxlePowerSourcePainted.paintedPowerSourceProperties());
		GEARBOX_PAINTED = registerBlock("gearbox_painted", BlockLogicGearBoxPainted.paintedProperties());

		// BTW CE: Flammability.PLANKS (5, 20) and Flammability.WICKER (60, 100)
		BlockLogicFire.setFlammable(GRATE, 5, 20);
		BlockLogicFire.setFlammable(SLATS, 5, 20);
		BlockLogicFire.setFlammable(WICKER_PANE, 60, 100);
		BlockLogicFire.setFlammable(WICKER_BLOCK, 60, 100);
		BlockLogicFire.setFlammable(WICKER_SLAB, 60, 100);
		BlockLogicFire.setFlammable(COMPANION_CUBE, 30, 60);
		BlockLogicFire.setFlammable(COMPANION_SLAB, 30, 60);
		BlockLogicFire.setFlammable(SAW, 5, 20);
		BlockLogicFire.setFlammable(PULLEY, 5, 20);
		BlockLogicFire.setFlammable(PLATFORM, 60, 100);
	}
}
