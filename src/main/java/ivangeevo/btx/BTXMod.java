package ivangeevo.btx;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.config.BTXConfig;
import ivangeevo.btx.entity.BTXEntities;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.networking.NetEntryWindMill;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.impl.RecipeEntryMillstone;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.net.entity.NetEntityHandler;
import turniplabs.halplibe.HalpLibe;
import turniplabs.halplibe.event.defs.CommonEvents;
import turniplabs.halplibe.util.dependency.Key;

public class BTXMod implements ModInitializer {
	public static final String MOD_ID = HalpLibe.registerMod("btx", true);

	private final BTXRecipes recipes = new BTXRecipes();

	@Override
	public void onInitialize() {
		CommonEvents.BEFORE_GAME_START.listen(Key.of(MOD_ID), this::beforeGameStart);
		CommonEvents.AFTER_GAME_START.listen(Key.of(MOD_ID), this::afterGameStart);
		CommonEvents.AFTER_BLOCK_INIT.listen(Key.of(MOD_ID), BTXBlocks::afterBlockInit);
		CommonEvents.AFTER_ITEM_INIT.listen(Key.of(MOD_ID), BTXItems::afterItemInit);
		CommonEvents.RECIPES_NAMESPACE_INIT.listen(Key.of(MOD_ID), recipes::initNamespaces);
		CommonEvents.RECIPES_READY.listen(Key.of(MOD_ID), recipes::onRecipesReady);

		BTXGlobals.LOGGER.info("Better Than [X] initialized.");
	}

	public void beforeGameStart() {
		BTXConfig.init();
		registerNewRecipeTypes();
		BTXEntities.init();
		NetEntityHandler.registerNetworkEntry(new NetEntryWindMill(), BTXConfig.netEntityTypeID());
	}

	public void afterGameStart() {
		BTXCreativeContents.init();
	}

	public static void registerNewRecipeTypes() {
		Registries.RECIPE_TYPES.register("btx:millstone", RecipeEntryMillstone.class);
	}
}
