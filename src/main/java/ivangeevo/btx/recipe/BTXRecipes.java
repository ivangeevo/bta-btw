package ivangeevo.btx.recipe;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.recipe.impl.RecipeGroupBTXMachine;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.data.DataLoader;
import net.minecraft.core.data.registry.recipe.RecipeNamespace;
import net.minecraft.core.item.ItemStack;
import turniplabs.halplibe.helper.RecipeBuilder;
import turniplabs.halplibe.helper.recipeBuilders.RecipeBuilderShaped;

public class BTXRecipes {
	public static RecipeNamespace BTX;
	public static RecipeGroupBTXMachine MILLSTONE;


	public void onRecipesReady() {
		BTXRecipes.workbenchRecipes();
		//BTXRecipes.btxMachinesRecipes();
	}

	public static void workbenchRecipes() {
		RecipeBuilderShaped millStone = new RecipeBuilderShaped(BTXMod.MOD_ID, "SSS", "SSS", "SGS");
		millStone.addInput('S', Blocks.STONE).addInput('G', BTXItems.GEAR).create("millstone", new ItemStack(BTXBlocks.MILLSTONE, 1));
	}

	public static void btxMachinesRecipes() {
		//DataLoader.loadRecipesFromFile("/assets/btx/recipes/millstone.json");
	}

	public void initNamespaces() {
		RecipeBuilder.initNameSpace(BTXMod.MOD_ID);
		BTX = RecipeBuilder.getRecipeNamespace(BTXMod.MOD_ID);
		RecipeBuilder.getRecipeNamespace(BTXMod.MOD_ID);
	}
}
