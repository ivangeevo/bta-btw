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
import ivangeevo.btx.recipe.saw.RecipeEntrySaw;
import ivangeevo.btx.recipe.saw.RecipeEntrySawBreaks;
import ivangeevo.btx.recipe.saw.RecipeGroupSaw;
import ivangeevo.btx.recipe.soulforge.RecipeEntrySoulforge;
import ivangeevo.btx.recipe.soulforge.RecipeEntrySoulforgeRepair;
import ivangeevo.btx.recipe.soulforge.RecipeGroupSoulforge;
import ivangeevo.btx.recipe.kiln.RecipeEntryKiln;
import ivangeevo.btx.recipe.kiln.RecipeGroupKiln;
import ivangeevo.btx.recipe.turntable.RecipeEntryTurntable;
import ivangeevo.btx.recipe.turntable.RecipeGroupTurntable;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.data.DataLoader;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.RecipeNamespace;
import ivangeevo.btx.recipe.impl.RecipeEntryCraftingShapelessConsuming;
import ivangeevo.btx.recipe.impl.RecipeEntryDyeingWaterWheel;
import net.minecraft.core.data.registry.recipe.entry.RecipeEntryDyeing;
import net.minecraft.core.data.registry.recipe.entry.RecipeEntryUndyeing;
import net.minecraft.core.data.registry.recipe.RecipeGroup;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import turniplabs.halplibe.helper.RecipeBuilder;
import turniplabs.halplibe.helper.recipeBuilders.RecipeBuilderBlastFurnace;
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
	public static RecipeGroupSoulforge SOULFORGE;
	public static RecipeGroupSaw SAW;

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
		Registries.RECIPE_TYPES.register("btx:soulforge", RecipeEntrySoulforge.class);
		Registries.RECIPE_TYPES.register("btx:soulforge_repair", RecipeEntrySoulforgeRepair.class);
		Registries.RECIPE_TYPES.register("btx:saw", RecipeEntrySaw.class);
		Registries.RECIPE_TYPES.register("btx:saw_breaks", RecipeEntrySawBreaks.class);
		Registries.RECIPE_TYPES.register("btx:crafting/shapeless_consuming", RecipeEntryCraftingShapelessConsuming.class);
		Registries.RECIPE_TYPES.register("btx:crafting/dyeing_waterwheel", RecipeEntryDyeingWaterWheel.class);
	}

	private static void registerWaterWheelDyeing() {
		RecipeSymbol anyWaterWheel = new RecipeSymbol(new ItemStack(BTXItems.WATERWHEEL, 1, -1));
		RecipeGroup workbench = RecipeBuilder.getRecipeGroup(BTXMod.MOD_ID, "workbench", new RecipeSymbol(Blocks.WORKBENCH.getDefaultStack()));
		workbench.register("waterwheel_dyeing", new RecipeEntryDyeingWaterWheel(anyWaterWheel, new ItemStack(BTXItems.WATERWHEEL)));
		workbench.register("waterwheel_undyeing", new RecipeEntryUndyeing(anyWaterWheel, new ItemStack(BTXItems.WATERWHEEL)));
	}

	private static void registerMechanicalDyeing() {
		RecipeGroup workbench = RecipeBuilder.getRecipeGroup(BTXMod.MOD_ID, "workbench", new RecipeSymbol(Blocks.WORKBENCH.getDefaultStack()));
		workbench.register("axle_dyeing", new RecipeEntryDyeing(new RecipeSymbol(BTXItemGroups.AXLES), new ItemStack(BTXBlocks.AXLE_PAINTED), true, false));
		workbench.register("axle_undyeing", new RecipeEntryUndyeing(new RecipeSymbol(BTXItemGroups.AXLES), new ItemStack(BTXBlocks.AXLE)));
		workbench.register("gearbox_dyeing", new RecipeEntryDyeing(new RecipeSymbol(BTXItemGroups.GEARBOXES), new ItemStack(BTXBlocks.GEARBOX_PAINTED), true, false));
		workbench.register("gearbox_undyeing", new RecipeEntryUndyeing(new RecipeSymbol(BTXItemGroups.GEARBOXES), new ItemStack(BTXBlocks.GEARBOX)));
	}

	public void onRecipesReady() {
		BTXWorkbenchRecipes.register();
		BTXRecipes.furnaceRecipes();
		BTXRecipes.modifyVanillaRecipes();
		BTXRecipes.modCustomRecipes();
	}

	public static void furnaceRecipes() {
		RecipeBuilderFurnace wolfChopCooked = new RecipeBuilderFurnace(BTXMod.MOD_ID);
		wolfChopCooked
			.setInput(BTXItems.FOOD_WOLF_CHOP_RAW)
			.create("food_wolf_chop_cooked", new ItemStack(BTXItems.FOOD_WOLF_CHOP_COOKED));
	}

	public static void modifyVanillaRecipes() {
		// Change rope recipe to require hemp fibers
		RecipeBuilder.ModifyWorkbench("minecraft").removeRecipe("rope");
		RecipeBuilderShaped rope = new RecipeBuilderShaped("btx","##", "##", "##");
		rope
			.addInput('#', BTXItems.HEMP_FIBERS)
			.create("rope", new ItemStack(Items.ROPE, 1));

		// Change dough recipe to require flour
		RecipeBuilder.ModifyWorkbench("minecraft").removeRecipe("dough");
		RecipeBuilderShapeless dough = new RecipeBuilderShapeless(BTXMod.MOD_ID);
		dough
			.addInput(BTXItems.FLOUR)
			.addInput("minecraft:water_buckets")
			.create("dough", new ItemStack(Items.DOUGH, 1));

		// Change crude steel recipe to require coal dust
		RecipeBuilder.ModifyBlastFurnace("minecraft").removeRecipe("iron_ingot_and_coal_to_crude_steel");
		RecipeBuilder.ModifyBlastFurnace("minecraft").removeRecipe("iron_ingot_and_charcoal_to_crude_steel");
		RecipeBuilderBlastFurnace crudeSteelCooking = new RecipeBuilderBlastFurnace(BTXMod.MOD_ID);
		crudeSteelCooking
			.setInput(0, Items.INGOT_IRON)
			.setInput(1, BTXItems.COAL_DUST)
			.create("iron_ingot_and_coaldust_to_crude_steel", Items.INGOT_STEEL_CRUDE);
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
		DataLoader.loadRecipesFromFile("/assets/btx/recipes/soulforge.json");
		DataLoader.loadRecipesFromFile("/assets/btx/recipes/saw.json");
		SOULFORGE.register("soulforged_repair", new RecipeEntrySoulforgeRepair());
		registerWaterWheelDyeing();
		registerMechanicalDyeing();
	}

	public void initNamespaces() {
		BTXItemGroups.register();

		RecipeBuilder.initNameSpace(BTXMod.MOD_ID);
		BTX = RecipeBuilder.getRecipeNamespace(BTXMod.MOD_ID);
		RecipeBuilder.getRecipeNamespace(BTXMod.MOD_ID);

		MILLSTONE = new RecipeGroupMillstone(new RecipeSymbol(new ItemStack(BTXBlocks.MILLSTONE.getDefaultStack())));
		KILN = new RecipeGroupKiln(new RecipeSymbol(new ItemStack(BTXBlocks.KILN.getDefaultStack())));
		TURNTABLE = new RecipeGroupTurntable(new RecipeSymbol(new ItemStack(BTXBlocks.TURNTABLE.getDefaultStack())));
		HOPPER_FILTER = new RecipeGroupHopperFilter(new RecipeSymbol(new ItemStack(BTXBlocks.HOPPER.getDefaultStack())));
		HOPPER = new RecipeGroupHopper(new RecipeSymbol(new ItemStack(BTXBlocks.HOPPER.getDefaultStack())));
		CRUCIBLE = new RecipeGroupBulk(new RecipeSymbol(new ItemStack(BTXBlocks.CRUCIBLE.getDefaultStack())));
		CRUCIBLE_STOKED = new RecipeGroupBulk(new RecipeSymbol(new ItemStack(BTXBlocks.CRUCIBLE.getDefaultStack())));
		CAULDRON = new RecipeGroupBulk(new RecipeSymbol(new ItemStack(BTXBlocks.CAULDRON.getDefaultStack())));
		CAULDRON_STOKED = new RecipeGroupBulk(new RecipeSymbol(new ItemStack(BTXBlocks.CAULDRON.getDefaultStack())));
		SOULFORGE = new RecipeGroupSoulforge(new RecipeSymbol(new ItemStack(BTXBlocks.SOULFORGE.getDefaultStack())));
		SAW = new RecipeGroupSaw(new RecipeSymbol(new ItemStack(BTXBlocks.SAW.getDefaultStack())));

		BTX.register("millstone", MILLSTONE);
		BTX.register("kiln", KILN);
		BTX.register("turntable", TURNTABLE);
		BTX.register("hopper_filter", HOPPER_FILTER);
		BTX.register("hopper", HOPPER);
		BTX.register("crucible", CRUCIBLE);
		BTX.register("crucible_stoked", CRUCIBLE_STOKED);
		BTX.register("cauldron", CAULDRON);
		BTX.register("cauldron_stoked", CAULDRON_STOKED);
		BTX.register("soulforge", SOULFORGE);
		BTX.register("saw", SAW);
	}
}
