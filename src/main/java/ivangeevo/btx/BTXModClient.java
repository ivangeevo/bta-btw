package ivangeevo.btx;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.render.BlockModelGenericCropsHemp;
import ivangeevo.btx.item.BTXItems;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.item.model.ItemModelStandard;
import net.minecraft.client.render.texture.stitcher.AtlasStitcher;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import turniplabs.halplibe.event.defs.ClientEvents;
import turniplabs.halplibe.helper.TextureHelper;
import turniplabs.halplibe.util.dependency.Key;

public class BTXModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientEvents.BEFORE_CLIENT_START.listen(Key.of(BTXMod.MOD_ID), BTXModClient::beforeClientStart);
		ClientEvents.BLOCK_MODEL_RELOAD.listen(Key.of(BTXMod.MOD_ID), BTXModClient::initBlockModels);
		ClientEvents.ITEM_MODEL_RELOAD.listen(Key.of(BTXMod.MOD_ID), BTXModClient::initItemModels);
		BTXGlobals.LOGGER.info("Better Than [X] client initialized.");
	}

	public static void beforeClientStart() {
		BTXModClient.registerTextures();
	}

	public static void initItemModels(ItemModelDispatcher dispatcher) {
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.HEMP).setIcon(icon("hemp")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.HEMP_FIBERS).setIcon(icon("hemp_fibers")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.HEMP_SEEDS).setIcon(icon("hemp_seeds")));
	}

	public static void initBlockModels(BlockModelDispatcher dispatcher) {
		dispatcher.addDispatch(new BlockModelGenericCropsHemp<>(BTXBlocks.HEMP));
	}

	public static void registerTextures() {
		for (final AtlasStitcher stitcher : TextureRegistry.stitcherMap.values()) {
			try {
				TextureHelper.initializeAllFiles(BTXMod.MOD_ID, stitcher, true);
			} catch (Exception e) {
				BTXGlobals.LOGGER.error("Failed to initialize texture files!", e);
			}
		}
	}

	private static String block(String name) {
		return BTXMod.MOD_ID + ":block/" + name;
	}

	private static String icon(String name) {
		return BTXMod.MOD_ID + ":item/" + name;
	}
}
