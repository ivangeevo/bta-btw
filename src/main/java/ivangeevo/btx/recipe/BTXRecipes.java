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
import turniplabs.halplibe.helper.RecipeBuilder;
import turniplabs.halplibe.helper.recipeBuilders.RecipeBuilderShaped;

public class BTXRecipes {
	public static RecipeNamespace BTX;
	public static RecipeGroupMillstone MILLSTONE;

	public void onRecipesReady() {
		BTXRecipes.workbenchRecipes();
		BTXRecipes.modCustomRecipes();
	}

	public static void workbenchRecipes() {
		RecipeBuilderShaped millStone = new RecipeBuilderShaped(BTXMod.MOD_ID, "SSS", "SSS", "SGS");
		millStone.addInput('S', Blocks.STONE).addInput('G', BTXItems.GEAR).create("millstone", new ItemStack(BTXBlocks.MILLSTONE, 1));
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
