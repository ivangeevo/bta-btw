package ivangeevo.btx.recipe;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.recipe.impl.RecipeGroupMillstone;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.data.DataLoader;
import net.minecraft.core.data.registry.recipe.RecipeNamespace;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import turniplabs.halplibe.helper.RecipeBuilder;
import turniplabs.halplibe.helper.recipeBuilders.RecipeBuilderShaped;
import turniplabs.halplibe.helper.recipeBuilders.RecipeBuilderShapeless;

public class BTXRecipes {
	public static RecipeNamespace BTX;
	public static RecipeGroupMillstone MILLSTONE;

	public void onRecipesReady() {
		BTXRecipes.workbenchRecipes();
		BTXRecipes.modifyVanillaRecipes();
		BTXRecipes.modCustomRecipes();
	}

	// TODO: Make some of these recipes handle tags(multiple types of item/block entries) instead of just single ones
	public static void workbenchRecipes() {
		// Blocks
		RecipeBuilderShaped millStone = new RecipeBuilderShaped(BTXMod.MOD_ID, "YYY", "YYY", "YXY");
		millStone
			.addInput('Y', Blocks.STONE)
			.addInput('X', BTXItems.GEAR)
			.create("millstone", new ItemStack(BTXBlocks.MILLSTONE, 1));

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
	}

	public void initNamespaces() {
		RecipeBuilder.initNameSpace(BTXMod.MOD_ID);
		BTX = RecipeBuilder.getRecipeNamespace(BTXMod.MOD_ID);
		RecipeBuilder.getRecipeNamespace(BTXMod.MOD_ID);

		MILLSTONE = new RecipeGroupMillstone(new RecipeSymbol(new ItemStack(BTXBlocks.MILLSTONE.getDefaultStack())));

		BTX.register("millstone", MILLSTONE);
	}

}
