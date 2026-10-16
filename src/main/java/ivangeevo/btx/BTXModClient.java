package ivangeevo.btx;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.render.*;
import ivangeevo.btx.entity.entities.WaterWheelEntity;
import ivangeevo.btx.entity.entities.WaterWheelEntityRenderer;
import ivangeevo.btx.entity.entities.WindMillEntity;
import ivangeevo.btx.entity.entities.WindMillEntityRenderer;
import ivangeevo.btx.item.BTXItems;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.EntityRendererDispatcher;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.item.model.ItemModelStandard;
import net.minecraft.client.render.texture.stitcher.AtlasStitcher;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import turniplabs.halplibe.event.defs.ClientEvents;
import turniplabs.halplibe.helper.TextureHelper;
import turniplabs.halplibe.util.dependency.Key;

import static net.minecraft.client.render.block.model.BlockModelDispatcher.loadDataModel;

public class BTXModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientEvents.BEFORE_CLIENT_START.listen(Key.of(BTXMod.MOD_ID), BTXModClient::beforeClientStart);
		ClientEvents.BLOCK_MODEL_RELOAD.listen(Key.of(BTXMod.MOD_ID), BTXModClient::initBlockModels);
		ClientEvents.ITEM_MODEL_RELOAD.listen(Key.of(BTXMod.MOD_ID), BTXModClient::initItemModels);
		ClientEvents.ENTITY_RENDERER_RELOAD.listen(Key.of(BTXMod.MOD_ID), BTXModClient::initEntityRenderers);
		BTXGlobals.LOGGER.info("Better Than [X] client initialized.");
	}

	public static void beforeClientStart() {
		BTXModClient.registerTextures();
	}

	public static void initItemModels(ItemModelDispatcher dispatcher) {
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.HEMP_SEEDS).setIcon(icon("hemp_seeds")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.HEMP).setIcon(icon("hemp")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.GEAR).setIcon(icon("gear")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.FLOUR).setIcon(icon("flour")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.HEMP_FIBERS).setIcon(icon("hemp_fibers")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.SCOURED_LEATHER).setIcon(icon("scoured_leather")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.DUNG).setIcon(icon("dung")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.WATERWHEEL).setIcon(icon("water_wheel")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.SAIL).setIcon(icon("sail")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.WINDMILL).setIcon(icon("windmill")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.FABRIC).setIcon(icon("fabric")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.WOOD_BLADE).setIcon(icon("wood_blade")));
	}

	public static void initBlockModels(BlockModelDispatcher dispatcher) {
		dispatcher.addDispatch(new BlockModelGenericCropsHemp<>(BTXBlocks.HEMP));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.MILLSTONE, loadDataModel(block("millstone"))));
		dispatcher.addDispatch(new BlockModelGenericHandCrank<>(BTXBlocks.HAND_CRANK));
		dispatcher.addDispatch(new BlockModelGenericAxle<>(BTXBlocks.AXLE));
		dispatcher.addDispatch(new BlockModelGenericAxle<>(BTXBlocks.AXLE_POWER_SOURCE));
		dispatcher.addDispatch(new BlockModelGenericGearBox<>(BTXBlocks.GEARBOX));
		dispatcher.addDispatch(new BlockModelGenericSiding<>(BTXBlocks.SIDING_OAK, loadDataModel(block("siding/siding_oak"))));
		dispatcher.addDispatch(new BlockModelGenericSidingPainted<>(BTXBlocks.SIDING_OAK_PAINTED));
		dispatcher.addDispatch(new BlockModelGenericMoulding<>(BTXBlocks.MOULDING_OAK, "minecraft:block/planks/oak", "oak"));
		dispatcher.addDispatch(new BlockModelGenericMouldingPainted<>(BTXBlocks.MOULDING_OAK_PAINTED));
		dispatcher.addDispatch(new BlockModelGenericCorner<>(BTXBlocks.CORNER_OAK, loadDataModel(block("corner/corner_oak"))));
		dispatcher.addDispatch(new BlockModelGenericCornerPainted<>(BTXBlocks.CORNER_OAK_PAINTED));
	}

	public static void initEntityRenderers(EntityRendererDispatcher dispatcher) {
		dispatcher.assignRenderer(WindMillEntity.class, new WindMillEntityRenderer());
		dispatcher.assignRenderer(WaterWheelEntity.class, new WaterWheelEntityRenderer());
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
