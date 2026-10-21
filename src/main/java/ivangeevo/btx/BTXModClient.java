package ivangeevo.btx;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.render.*;
import ivangeevo.btx.particle.ParticleWhiteSmoke;
import ivangeevo.btx.entity.entities.WaterWheelEntity;
import ivangeevo.btx.entity.entities.WaterWheelEntityRenderer;
import ivangeevo.btx.entity.entities.WindMillEntity;
import ivangeevo.btx.entity.entities.WindMillEntityRenderer;
import ivangeevo.btx.item.BTXItems;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.EntityRendererDispatcher;
import net.minecraft.client.render.block.color.BlockColorCustom;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.block.model.generic.BlockModelGenericSlab;
import net.minecraft.client.render.colorizer.Colorizers;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.item.model.ItemModelStandard;
import net.minecraft.client.render.particle.ParticleDispatcher;
import net.minecraft.client.render.texture.stitcher.AtlasStitcher;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.item.Item;
import turniplabs.halplibe.event.defs.ClientEvents;
import turniplabs.halplibe.helper.TextureHelper;
import turniplabs.halplibe.util.dependency.Key;

import static net.minecraft.client.render.block.model.BlockModelDispatcher.loadDataModel;

public class BTXModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientEvents.BEFORE_CLIENT_START.listen(Key.of(BTXMod.MOD_ID), BTXModClient::beforeClientStart);
		ClientEvents.AFTER_CLIENT_START.listen(Key.of(BTXMod.MOD_ID), BTXModClient::afterClientStart);
		ClientEvents.BLOCK_MODEL_RELOAD.listen(Key.of(BTXMod.MOD_ID), BTXModClient::initBlockModels);
		ClientEvents.ITEM_MODEL_RELOAD.listen(Key.of(BTXMod.MOD_ID), BTXModClient::initItemModels);
		ClientEvents.ENTITY_RENDERER_RELOAD.listen(Key.of(BTXMod.MOD_ID), BTXModClient::initEntityRenderers);
		ClientEvents.BLOCK_COLOR_RELOAD.listen(Key.of(BTXMod.MOD_ID), BTXModClient::initBlockColors);
		BTXGlobals.localPlayerInstantBreaks = () -> {
			Minecraft mc = Minecraft.getMinecraft();
			return mc.thePlayer != null && !mc.thePlayer.getGamemode().hasBlockBreakingAnimation();
		};
		BTXGlobals.LOGGER.info("Better Than [X] client initialized.");
	}

	public static void beforeClientStart() {
		BTXModClient.registerTextures();
	}

	// after start: ParticleDispatcher grabs the Minecraft instance when it's first created
	public static void afterClientStart() {
		ParticleDispatcher.getInstance().addDispatch("whitesmoke",
			(world, x, y, z, motionX, motionY, motionZ, data) -> new ParticleWhiteSmoke(world, x, y, z, motionX, motionY, motionZ));
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
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.FOUL_FOOD).setIcon(icon("foul_food")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.KIBBLE).setIcon(icon("kibble")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.COAL_DUST).setIcon(icon("coal_dust")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.GROUND_NETHERRACK).setIcon(icon("ground_netherrack")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.HELLFIRE_DUST).setIcon(icon("hellfire_dust")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.CONCENTRATED_HELLFIRE).setIcon(icon("concentrated_hellfire")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.WICKER).setIcon(icon("wicker")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.FOOD_WOLF_CHOP_RAW).setIcon(icon("food_wolf_chop_raw")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.FOOD_WOLF_CHOP_COOKED).setIcon(icon("food_wolf_chop_cooked")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.DONUT).setIcon(icon("donut")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.SOULFORGED_STEEL_INGOT).setIcon(icon("soulforged_steel_ingot")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.STEEL_ARMOR_PLATE).setIcon(icon("steel_armor_plate")));
		dispatcher.addDispatch(handheld(BTXItems.SOULFORGED_STEEL_PICKAXE, "soulforged_steel_pickaxe"));
		dispatcher.addDispatch(handheld(BTXItems.SOULFORGED_STEEL_SHOVEL, "soulforged_steel_shovel"));
		dispatcher.addDispatch(handheld(BTXItems.SOULFORGED_STEEL_MATTOCK, "soulforged_steel_mattock"));
		dispatcher.addDispatch(handheld(BTXItems.SOULFORGED_STEEL_HOE, "soulforged_steel_hoe"));
		dispatcher.addDispatch(handheld(BTXItems.SOULFORGED_STEEL_AXE, "soulforged_steel_axe"));
		dispatcher.addDispatch(handheld(BTXItems.SOULFORGED_STEEL_BATTLEAXE, "soulforged_steel_battleaxe"));
		dispatcher.addDispatch(handheld(BTXItems.SOULFORGED_STEEL_SWORD, "soulforged_steel_sword"));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.SOULFORGED_STEEL_HELMET).setIcon(icon("soulforged_steel_helmet")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.SOULFORGED_STEEL_CHESTPLATE).setIcon(icon("soulforged_steel_chestplate")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.SOULFORGED_STEEL_LEGGINGS).setIcon(icon("soulforged_steel_leggings")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.SOULFORGED_STEEL_BOOTS).setIcon(icon("soulforged_steel_boots")));
		dispatcher.addDispatch(new ItemModelStandard(BTXBlocks.GRATE.asItem()).setIcon(icon("grate")));
		dispatcher.addDispatch(new ItemModelStandard(BTXBlocks.SLATS.asItem()).setIcon(icon("slats")));
		dispatcher.addDispatch(new ItemModelStandard(BTXBlocks.WICKER_PANE.asItem()).setIcon(icon("wicker_pane")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.TANNED_LEATHER).setIcon(icon("tanned_leather")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.STRAP).setIcon(icon("strap")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.BELT).setIcon(icon("belt")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.GLUE).setIcon(icon("glue")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.HAFT).setIcon(icon("haft")));
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
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.HIBACHI, loadDataModel(block("hibachi"))));
		dispatcher.addDispatch(new BlockModelStokedFire<>(BTXBlocks.STOKED_FIRE));
		dispatcher.addDispatch(new BlockModelGenericBellows<>(BTXBlocks.BELLOWS));
		dispatcher.addDispatch(new BlockModelGenericTurntable(BTXBlocks.TURNTABLE));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.UNFIRED_CRUCIBLE, loadDataModel(block("pottery/unfired_crucible"))));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.UNFIRED_PLANTER, loadDataModel(block("pottery/unfired_planter"))));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.UNFIRED_VASE, loadDataModel(block("pottery/unfired_vase"))));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.VASE, loadDataModel(block("vase/vase"))));
		dispatcher.addDispatch(new BlockModelGenericVasePainted<>(BTXBlocks.VASE_PAINTED));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.KILN, loadDataModel(block("kiln"))));
		dispatcher.addDispatch(new BlockModelGenericCrucible<>(BTXBlocks.CRUCIBLE));
		dispatcher.addDispatch(new BlockModelGenericCauldron<>(BTXBlocks.CAULDRON));
		dispatcher.addDispatch(new BlockModelGenericHopper<>(BTXBlocks.HOPPER));
		dispatcher.addDispatch(new BlockModelGenericPane<>(BTXBlocks.GRATE, block("grate/grate_half")));
		dispatcher.addDispatch(new BlockModelGenericPane<>(BTXBlocks.SLATS, block("slats/slats_half")));
		dispatcher.addDispatch(new BlockModelGenericPane<>(BTXBlocks.WICKER_PANE, block("wicker_pane/wicker_pane_half")));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.WICKER_BLOCK, loadDataModel(block("wicker/wicker_block"))));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.CONCENTRATED_HELLFIRE_BLOCK, loadDataModel(block("concentrated_hellfire_block"))));
		dispatcher.addDispatch(new BlockModelGenericSoulforge<>(BTXBlocks.SOULFORGE));
		dispatcher.addDispatch(new BlockModelGenericSaw<>(BTXBlocks.SAW));
		dispatcher.addDispatch(new BlockModelGenericSlab<>(BTXBlocks.WICKER_SLAB,
			loadDataModel(block("wicker/wicker_slab_lower")),
			loadDataModel(block("wicker/wicker_slab_upper")),
			loadDataModel(block("wicker/wicker_slab_full")))
		);
		dispatcher.addDispatch(new BlockModelGenericCompanionCube<>(BTXBlocks.COMPANION_CUBE));
		dispatcher.addDispatch(new BlockModelGenericSlab<>(BTXBlocks.COMPANION_SLAB,
			loadDataModel(block("companion_cube/companion_cube_slab")),
			loadDataModel(block("companion_cube/companion_cube_slab_upper")),
			loadDataModel(block("companion_cube/companion_cube")))
		);
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.PLANTER, loadDataModel(block("planter/planter"))));
		dispatcher.addDispatch(new BlockModelGenericPlanterSoil<>(BTXBlocks.PLANTER_SOIL));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.PLANTER_GRASS, loadDataModel(block("planter/planter_grass"))));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.PLANTER_GRASS_RETRO, loadDataModel(block("planter/planter_grass_retro"))));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.PLANTER_GRASS_SCORCHED, loadDataModel(block("planter/planter_grass_scorched"))));
	}

	// grass planter top is tinted by biome, the same as BTA grass
	public static void initBlockColors(BlockColorDispatcher dispatcher) {
		dispatcher.addDispatch(BTXBlocks.PLANTER_GRASS, new BlockColorCustom(Colorizers.grass));
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

	private static ItemModelStandard handheld(Item item, String name) {
		return new ItemModelStandard(item).setIcon(icon(name))
			.setDisplayPos("firstperson_righthand", ItemModelDispatcher.HANDHELD_FIRST_PERSON_RIGHT_HAND)
			.setDisplayPos("firstperson_lefthand", ItemModelDispatcher.HANDHELD_FIRST_PERSON_LEFT_HAND)
			.setDisplayPos("thirdperson_righthand", ItemModelDispatcher.HANDHELD_THIRD_PERSON_RIGHT_HAND)
			.setDisplayPos("thirdperson_lefthand", ItemModelDispatcher.HANDHELD_THIRD_PERSON_LEFT_HAND);
	}

	private static String icon(String name) {
		return BTXMod.MOD_ID + ":item/" + name;
	}
}
