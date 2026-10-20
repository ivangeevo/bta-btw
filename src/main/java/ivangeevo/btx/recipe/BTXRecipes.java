package ivangeevo.btx.recipe;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.recipe.cooking.RecipeEntryCauldron;
import ivangeevo.btx.recipe.cooking.RecipeEntryCauldronStoked;
import ivangeevo.btx.recipe.cooking.RecipeEntryCrucible;
import ivangeevo.btx.recipe.cooking.RecipeEntryCrucibleStoked;
import ivangeevo.btx.recipe.cooking.RecipeGroupBulk;
import ivangeevo.btx.recipe.hopper.RecipeEntryHopper;
import ivangeevo.btx.recipe.hopper.RecipeEntryHopperFilter;
import ivangeevo.btx.recipe.hopper.RecipeGroupHopper;
import ivangeevo.btx.recipe.hopper.RecipeGroupHopperFilter;
import ivangeevo.btx.recipe.impl.RecipeEntryMillstone;
import ivangeevo.btx.recipe.impl.RecipeGroupMillstone;
import ivangeevo.btx.recipe.kiln.RecipeEntryKiln;
import ivangeevo.btx.recipe.kiln.RecipeGroupKiln;
import ivangeevo.btx.recipe.turntable.RecipeEntryTurntable;
import ivangeevo.btx.recipe.turntable.RecipeGroupTurntable;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.data.DataLoader;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.RecipeNamespace;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.helper.DyeColor;
import turniplabs.halplibe.helper.RecipeBuilder;
import turniplabs.halplibe.helper.recipeBuilders.RecipeBuilderFurnace;
import turniplabs.halplibe.helper.recipeBuilders.RecipeBuilderShaped;
import turniplabs.halplibe.helper.recipeBuilders.RecipeBuilderShapeless;

public class BTXRecipes {
	public static RecipeNamespace BTX;
	public static RecipeGroupMillstone MILLSTONE;
	public static RecipeGroupKiln KILN;
	public static RecipeGroupTurntable TURNTABLE;
	public static RecipeGroupBulk CRUCIBLE;
	public static RecipeGroupBulk CRUCIBLE_STOKED;
	public static RecipeGroupBulk CAULDRON;
	public static RecipeGroupBulk CAULDRON_STOKED;
	public static RecipeGroupHopperFilter HOPPER_FILTER;
	public static RecipeGroupHopper HOPPER;

	public static void init() {
		registerNewRecipeTypes();
	}

	public static void registerNewRecipeTypes() {
		Registries.RECIPE_TYPES.register("btx:millstone", RecipeEntryMillstone.class);
		Registries.RECIPE_TYPES.register("btx:kiln", RecipeEntryKiln.class);
		Registries.RECIPE_TYPES.register("btx:turntable", RecipeEntryTurntable.class);
		Registries.RECIPE_TYPES.register("btx:crucible", RecipeEntryCrucible.class);
		Registries.RECIPE_TYPES.register("btx:crucible_stoked", RecipeEntryCrucibleStoked.class);
		Registries.RECIPE_TYPES.register("btx:cauldron", RecipeEntryCauldron.class);
		Registries.RECIPE_TYPES.register("btx:cauldron_stoked", RecipeEntryCauldronStoked.class);
		Registries.RECIPE_TYPES.register("btx:hopper_filter", RecipeEntryHopperFilter.class);
		Registries.RECIPE_TYPES.register("btx:hopper", RecipeEntryHopper.class);
	}

	public void onRecipesReady() {
		BTXRecipes.workbenchRecipes();
		BTXRecipes.modifyVanillaRecipes();
		BTXRecipes.modCustomRecipes();
	}

	// TODO: Make some of these recipes handle tags(multiple types of item/block entries) instead of just single ones
	public static void workbenchRecipes() {
		// Blocks
		RecipeBuilderFurnace wolfChopCooked = new RecipeBuilderFurnace(BTXMod.MOD_ID).setInput(BTXItems.FOOD_WOLF_CHOP_RAW);
		wolfChopCooked
			.create("food_wolf_chop_cooked", new ItemStack(BTXItems.FOOD_WOLF_CHOP_COOKED));

		RecipeBuilderShaped millStone = new RecipeBuilderShaped(BTXMod.MOD_ID, "YYY", "YYY", "YXY");
		millStone
			.addInput('Y', Blocks.STONE)
			.addInput('X', BTXItems.GEAR)
			.create("millstone", new ItemStack(BTXBlocks.MILLSTONE, 1));

		RecipeBuilderShaped hopper = new RecipeBuilderShaped(BTXMod.MOD_ID, "# #", "XYX", " Z ");
		hopper
			.addInput('#', BTXBlocks.SIDING_OAK)
			.addInput('X', BTXItems.GEAR)
			.addInput('Y', Blocks.PRESSURE_PLATE_PLANKS_OAK)
			.addInput('Z', BTXBlocks.CORNER_OAK)
			.create("hopper", new ItemStack(BTXBlocks.HOPPER, 1));

		// BTW CE: addPaneRecipes / addWickerRecipes (no wicker weaving, basket or loom)
		RecipeBuilderShaped grate = new RecipeBuilderShaped(BTXMod.MOD_ID, "S#S", "###", "S#S");
		grate
			.addInput('#', Items.STICK)
			.addInput('S', Items.STRING)
			.create("grate", new ItemStack(BTXBlocks.GRATE, 1));

		RecipeBuilderShaped slats = new RecipeBuilderShaped(BTXMod.MOD_ID, "##", "##");
		slats
			.addInput('#', BTXBlocks.MOULDING_OAK)
			.create("slats", new ItemStack(BTXBlocks.SLATS, 1));

		RecipeBuilderShaped wicker = new RecipeBuilderShaped(BTXMod.MOD_ID, "##", "##");
		wicker
			.addInput('#', Items.SUGARCANE)
			.create("wicker", new ItemStack(BTXItems.WICKER, 1));

		RecipeBuilderShapeless wickerPane = new RecipeBuilderShapeless(BTXMod.MOD_ID);
		wickerPane
			.addInput(BTXBlocks.GRATE)
			.addInput(BTXItems.WICKER)
			.create("wicker_pane", new ItemStack(BTXBlocks.WICKER_PANE, 1));

		RecipeBuilderShaped wickerBlock = new RecipeBuilderShaped(BTXMod.MOD_ID, "##", "##");
		wickerBlock
			.addInput('#', BTXBlocks.WICKER_PANE)
			.create("wicker_block", new ItemStack(BTXBlocks.WICKER_BLOCK, 1));

		RecipeBuilderShaped wickerSlabFromPanes = new RecipeBuilderShaped(BTXMod.MOD_ID, "##");
		wickerSlabFromPanes
			.addInput('#', BTXBlocks.WICKER_PANE)
			.create("wicker_slab_from_panes", new ItemStack(BTXBlocks.WICKER_SLAB, 1));

		RecipeBuilderShaped wickerSlabFromBlocks = new RecipeBuilderShaped(BTXMod.MOD_ID, "##");
		wickerSlabFromBlocks
			.addInput('#', BTXBlocks.WICKER_BLOCK)
			.create("wicker_slab_from_blocks", new ItemStack(BTXBlocks.WICKER_SLAB, 4));

		RecipeBuilderShapeless wickerPaneFromBlock = new RecipeBuilderShapeless(BTXMod.MOD_ID);
		wickerPaneFromBlock
			.addInput(BTXBlocks.WICKER_BLOCK)
			.create("wicker_pane_from_block", new ItemStack(BTXBlocks.WICKER_PANE, 4));

		RecipeBuilderShapeless wickerPaneFromSlab = new RecipeBuilderShapeless(BTXMod.MOD_ID);
		wickerPaneFromSlab
			.addInput(BTXBlocks.WICKER_SLAB)
			.create("wicker_pane_from_slab", new ItemStack(BTXBlocks.WICKER_PANE, 2));

		RecipeBuilderShapeless wickerBlockFromSlabs = new RecipeBuilderShapeless(BTXMod.MOD_ID);
		wickerBlockFromSlabs
			.addInput(BTXBlocks.WICKER_SLAB)
			.addInput(BTXBlocks.WICKER_SLAB)
			.create("wicker_block_from_slabs", new ItemStack(BTXBlocks.WICKER_BLOCK, 1));

		RecipeBuilderShaped handCrank = new RecipeBuilderShaped(BTXMod.MOD_ID, "  Y", " Y ", "#X#");
		handCrank.addInput('#', Blocks.COBBLE_STONE)
			.addInput('X', BTXItems.GEAR)
			.addInput('Y', Items.STICK)
			.create("hand_crank", new ItemStack(BTXBlocks.HAND_CRANK, 1));

		// TODO: Add moulding recipe for axles
		RecipeBuilderShaped axle = new RecipeBuilderShaped(BTXMod.MOD_ID, "#", "X", "#");
		axle
			.addInput('#', Blocks.PLANKS_OAK)
			.addInput('X', Items.ROPE)
			.create("axle", new ItemStack(BTXBlocks.AXLE, 1));

		RecipeBuilderShaped gearBox = new RecipeBuilderShaped(BTXMod.MOD_ID, "#X#", "XYX", "#X#");
		gearBox
			.addInput('#', Blocks.PLANKS_OAK)
			.addInput('X', BTXItems.GEAR)
			.addInput('Y', Items.DUST_REDSTONE)
			.create("gearbox", new ItemStack(BTXBlocks.GEARBOX, 1));

		RecipeBuilderShaped fabric = new RecipeBuilderShaped(BTXMod.MOD_ID, "###", "###", "###");
		fabric
			.addInput('#', BTXItems.HEMP_FIBERS)
			.create("fabric", new ItemStack(BTXItems.FABRIC, 1));


		RecipeBuilderShapeless hempFibersFromFabric = new RecipeBuilderShapeless(BTXMod.MOD_ID);
		hempFibersFromFabric
			.addInput(BTXItems.FABRIC)
			.create("hemp_fibers_from_fabric", new ItemStack(BTXItems.HEMP_FIBERS, 9));

		RecipeBuilderShapeless hempFibersFromRope = new RecipeBuilderShapeless(BTXMod.MOD_ID);
		hempFibersFromRope
			.addInput(Items.ROPE)
			.create("hemp_fibers_from_rope", new ItemStack(BTXItems.HEMP_FIBERS, 6));

		RecipeBuilderShapeless string = new RecipeBuilderShapeless(BTXMod.MOD_ID);
		string
			.addInput(BTXItems.HEMP_FIBERS).addInput(BTXItems.HEMP_FIBERS)
			.create("string", new ItemStack(Items.STRING, 1));

		// TODO: Add recipe for crafting sail with different types of woo
		RecipeBuilderShaped sail = new RecipeBuilderShaped(BTXMod.MOD_ID, "###", "###", "XXX");
		sail
			.addInput('#', BTXItems.FABRIC)
			.addInput('X', Blocks.PLANKS_OAK)
			.create("sail", new ItemStack(BTXItems.SAIL, 1));

		// TODO: Add recipe for crafting sail with moulding
		/*sail
			.addInput('#', BTXItems.FABRIC)
			.addInput('X', BTXBlocks.MOULDING)
			.create("sail", new ItemStack(BTXItems.SAIL, 1));
*/

		RecipeBuilderShaped windMill = new RecipeBuilderShaped(BTXMod.MOD_ID, " # ", "# #", " # ");
		windMill
			.addInput('#', BTXItems.SAIL)
			.create("windmill", new ItemStack(BTXItems.WINDMILL, 1));

		RecipeBuilderShaped hibachi = new RecipeBuilderShaped(BTXMod.MOD_ID, "XXX", "#Z#", "#Y#");
		hibachi
			.addInput('#', Blocks.STONE)
			.addInput('X', BTXItems.CONCENTRATED_HELLFIRE)
			.addInput('Y', Items.DUST_REDSTONE)
			.addInput('Z', Items.DUST_GLOWSTONE)
			.create("hibachi", new ItemStack(BTXBlocks.HIBACHI, 1));

		// TODO: Add colored planks support
		RecipeBuilderShaped bellows = new RecipeBuilderShaped(BTXMod.MOD_ID, "###", "XXX", "YZY");
		bellows
			.addInput('#', BTXBlocks.CORNER_OAK)
			.addInput('X', BTXItems.TANNED_LEATHER)
			.addInput('Y', BTXItems.GEAR)
			.addInput('Z', BTXItems.BELT)
			.create("bellows", new ItemStack(BTXBlocks.BELLOWS, 1));

		// TODO: Add colored planks support
		RecipeBuilderShaped turntable = new RecipeBuilderShaped(BTXMod.MOD_ID, "###", "ZXZ", "ZYZ");
		turntable
			.addInput('#', BTXBlocks.CORNER_OAK)
			.addInput('X', Items.DUST_REDSTONE)
			.addInput('Y', BTXItems.GEAR)
			.addInput('Z', Blocks.STONE)
			.create("turntable", new ItemStack(BTXBlocks.TURNTABLE, 1));

		RecipeBuilderShaped cauldron = new RecipeBuilderShaped(BTXMod.MOD_ID, "#Y#", "#X#", "###");
		cauldron
			.addInput('#', Items.INGOT_IRON)
			.addInput('X', Items.BONE)
			.addInput('Y', "minecraft:water_buckets")
			.create("cauldron", new ItemStack(BTXBlocks.CAULDRON, 1));

		RecipeBuilderShaped planterSoil = new RecipeBuilderShaped(BTXMod.MOD_ID, "Y", "X", "#");
		planterSoil
			.addInput('#', BTXBlocks.PLANTER)
			.addInput('X', "minecraft:water_buckets")
			.addInput('Y', Blocks.DIRT)
			.create("planter_soil", new ItemStack(BTXBlocks.PLANTER_SOIL, 1));

		RecipeBuilderShaped planterGrass = new RecipeBuilderShaped(BTXMod.MOD_ID, "Y", "X", "#");
		planterGrass
			.addInput('#', BTXBlocks.PLANTER)
			.addInput('X', "minecraft:water_buckets")
			.addInput('Y', Blocks.GRASS)
			.create("planter_grass", new ItemStack(BTXBlocks.PLANTER_GRASS, 1));

		RecipeBuilderShaped planterGrassRetro = new RecipeBuilderShaped(BTXMod.MOD_ID, "Y", "X", "#");
		planterGrassRetro
			.addInput('#', BTXBlocks.PLANTER)
			.addInput('X', "minecraft:water_buckets")
			.addInput('Y', Blocks.GRASS_RETRO)
			.create("planter_grass_retro", new ItemStack(BTXBlocks.PLANTER_GRASS_RETRO, 1));

		RecipeBuilderShaped planterGrassScorched = new RecipeBuilderShaped(BTXMod.MOD_ID, "Y", "X", "#");
		planterGrassScorched
			.addInput('#', BTXBlocks.PLANTER)
			.addInput('X', "minecraft:water_buckets")
			.addInput('Y', Blocks.GRASS_SCORCHED)
			.create("planter_grass_scorched", new ItemStack(BTXBlocks.PLANTER_GRASS_SCORCHED, 1));

		// Two of a wood piece combine back into the next size up (BTW: siding -> planks, moulding -> siding, corner -> moulding)
		RecipeBuilderShapeless planksFromSidings = new RecipeBuilderShapeless(BTXMod.MOD_ID);
		planksFromSidings
			.addInput(BTXBlocks.SIDING_OAK)
			.addInput(BTXBlocks.SIDING_OAK)
			.create("planks_from_sidings", new ItemStack(Blocks.PLANKS_OAK, 1));

		RecipeBuilderShapeless sidingFromMouldings = new RecipeBuilderShapeless(BTXMod.MOD_ID);
		sidingFromMouldings
			.addInput(BTXBlocks.MOULDING_OAK)
			.addInput(BTXBlocks.MOULDING_OAK)
			.create("siding_from_mouldings", new ItemStack(BTXBlocks.SIDING_OAK, 1));

		RecipeBuilderShapeless mouldingFromCorners = new RecipeBuilderShapeless(BTXMod.MOD_ID);
		mouldingFromCorners
			.addInput(BTXBlocks.CORNER_OAK)
			.addInput(BTXBlocks.CORNER_OAK)
			.create("moulding_from_corners", new ItemStack(BTXBlocks.MOULDING_OAK, 1));

		// Painted pieces keep their color: the pieces store it as blockMeta << 4, BTA's painted planks as blockMeta
		for (DyeColor color : DyeColor.values()) {
			int pieceMeta = color.blockMeta << 4;

			RecipeBuilderShapeless paintedPlanksFromSidings = new RecipeBuilderShapeless(BTXMod.MOD_ID);
			paintedPlanksFromSidings
				.addInput(BTXBlocks.SIDING_OAK_PAINTED, pieceMeta)
				.addInput(BTXBlocks.SIDING_OAK_PAINTED, pieceMeta)
				.create("planks_painted_from_sidings_" + color.colorID, new ItemStack(Blocks.PLANKS_OAK_PAINTED, 1, color.blockMeta));

			RecipeBuilderShapeless paintedSidingFromMouldings = new RecipeBuilderShapeless(BTXMod.MOD_ID);
			paintedSidingFromMouldings
				.addInput(BTXBlocks.MOULDING_OAK_PAINTED, pieceMeta)
				.addInput(BTXBlocks.MOULDING_OAK_PAINTED, pieceMeta)
				.create("siding_painted_from_mouldings_" + color.colorID, new ItemStack(BTXBlocks.SIDING_OAK_PAINTED, 1, pieceMeta));

			RecipeBuilderShapeless paintedMouldingFromCorners = new RecipeBuilderShapeless(BTXMod.MOD_ID);
			paintedMouldingFromCorners
				.addInput(BTXBlocks.CORNER_OAK_PAINTED, pieceMeta)
				.addInput(BTXBlocks.CORNER_OAK_PAINTED, pieceMeta)
				.create("moulding_painted_from_corners_" + color.colorID, new ItemStack(BTXBlocks.MOULDING_OAK_PAINTED, 1, pieceMeta));
		}

		// Vases are colored in the world with BTA's paint brush (BlockLogicVase / BlockLogicVasePainted), not by crafting
	}

	public static void modifyVanillaRecipes() {
		RecipeBuilder.ModifyWorkbench("minecraft").removeRecipe("rope");

		RecipeBuilderShaped rope = new RecipeBuilderShaped("btx","##", "##", "##");
		rope
			.addInput('#', BTXItems.HEMP_FIBERS)
			.create("rope", new ItemStack(Items.ROPE, 1));
	}

	public static void modCustomRecipes() {
		DataLoader.loadRecipesFromFile("/assets/btx/recipes/millstone.json");
		DataLoader.loadRecipesFromFile("/assets/btx/recipes/kiln.json");
		DataLoader.loadRecipesFromFile("/assets/btx/recipes/turntable.json");
		DataLoader.loadRecipesFromFile("/assets/btx/recipes/crucible.json");
		DataLoader.loadRecipesFromFile("/assets/btx/recipes/crucible_stoked.json");
		DataLoader.loadRecipesFromFile("/assets/btx/recipes/cauldron.json");
		DataLoader.loadRecipesFromFile("/assets/btx/recipes/cauldron_stoked.json");
		DataLoader.loadRecipesFromFile("/assets/btx/recipes/hopper_filter.json");
		DataLoader.loadRecipesFromFile("/assets/btx/recipes/hopper.json");
	}

	public void initNamespaces() {
		RecipeBuilder.initNameSpace(BTXMod.MOD_ID);
		BTX = RecipeBuilder.getRecipeNamespace(BTXMod.MOD_ID);
		RecipeBuilder.getRecipeNamespace(BTXMod.MOD_ID);

		MILLSTONE = new RecipeGroupMillstone(new RecipeSymbol(new ItemStack(BTXBlocks.MILLSTONE.getDefaultStack())));
		KILN = new RecipeGroupKiln(new RecipeSymbol(new ItemStack(BTXBlocks.KILN.getDefaultStack())));
		TURNTABLE = new RecipeGroupTurntable(new RecipeSymbol(new ItemStack(BTXBlocks.TURNTABLE.getDefaultStack())));

		BTX.register("millstone", MILLSTONE);
		BTX.register("kiln", KILN);
		BTX.register("turntable", TURNTABLE);

		HOPPER_FILTER = new RecipeGroupHopperFilter(new RecipeSymbol(new ItemStack(BTXBlocks.HOPPER.getDefaultStack())));
		HOPPER = new RecipeGroupHopper(new RecipeSymbol(new ItemStack(BTXBlocks.HOPPER.getDefaultStack())));

		BTX.register("hopper_filter", HOPPER_FILTER);
		BTX.register("hopper", HOPPER);

		// CRUCIBLE/CAULDRON aren't registered blocks yet (BlockLogicCrucible/BlockLogicCauldron
		// are still stubs), so these groups wire up once BTXBlocks.afterBlockInit() registers them.
		if (BTXBlocks.CRUCIBLE != null) {
			CRUCIBLE = new RecipeGroupBulk(new RecipeSymbol(new ItemStack(BTXBlocks.CRUCIBLE.getDefaultStack())));
			CRUCIBLE_STOKED = new RecipeGroupBulk(new RecipeSymbol(new ItemStack(BTXBlocks.CRUCIBLE.getDefaultStack())));

			BTX.register("crucible", CRUCIBLE);
			BTX.register("crucible_stoked", CRUCIBLE_STOKED);
		}

		if (BTXBlocks.CAULDRON != null) {
			CAULDRON = new RecipeGroupBulk(new RecipeSymbol(new ItemStack(BTXBlocks.CAULDRON.getDefaultStack())));
			CAULDRON_STOKED = new RecipeGroupBulk(new RecipeSymbol(new ItemStack(BTXBlocks.CAULDRON.getDefaultStack())));

			BTX.register("cauldron", CAULDRON);
			BTX.register("cauldron_stoked", CAULDRON_STOKED);
		}
	}

}
