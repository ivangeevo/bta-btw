package ivangeevo.btx;

import ivangeevo.btx.api.guidebook.GuidebookBook;
import ivangeevo.btx.api.guidebook.GuidebookIndexSection;
import ivangeevo.btx.api.guidebook.Guidebooks;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.render.*;
import ivangeevo.btx.entity.entities.lift.BlockLiftedByPlatformEntity;
import ivangeevo.btx.entity.entities.lift.BlockLiftedByPlatformEntityRenderer;
import ivangeevo.btx.entity.entities.lift.MovingAnchorEntity;
import ivangeevo.btx.entity.entities.lift.MovingAnchorEntityRenderer;
import ivangeevo.btx.entity.entities.lift.MovingPlatformEntity;
import ivangeevo.btx.entity.entities.lift.MovingPlatformEntityRenderer;
import ivangeevo.btx.gui.guidebook.*;
import ivangeevo.btx.particle.BTXParticles;
import ivangeevo.btx.entity.entities.EntityBroadheadArrow;
import ivangeevo.btx.entity.entities.EntityRendererBroadheadArrow;
import ivangeevo.btx.entity.entities.WaterWheelEntity;
import ivangeevo.btx.entity.entities.WaterWheelEntityRenderer;
import ivangeevo.btx.entity.entities.WindMillEntity;
import ivangeevo.btx.entity.entities.WindMillEntityRenderer;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.item.render.ItemModelWaterWheel;
import ivangeevo.btx.recipe.BTXRecipes;
import net.fabricmc.api.ClientModInitializer;
import ivangeevo.btx.recipe.impl.RecipeEntryCraftingShapelessConsuming;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.guidebook.GuidebookSections;
import net.minecraft.client.gui.guidebook.crafting.RecipePageCrafting;
import net.minecraft.client.gui.guidebook.crafting.displays.DisplayAdapterShapeless;
import net.minecraft.client.render.EntityRendererDispatcher;
import net.minecraft.client.render.block.color.BlockColorCustom;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.BlockModelEmpty;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.block.model.generic.BlockModelGenericPressurePlate;
import net.minecraft.client.render.block.model.generic.BlockModelGenericSlab;
import net.minecraft.client.render.block.model.generic.BlockModelGenericVeryRotatable;
import net.minecraft.client.render.colorizer.Colorizers;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.item.model.ItemModelStandard;
import net.minecraft.client.render.texture.stitcher.AtlasStitcher;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
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
		Guidebooks.register(registerModGuidebook());
	}

	public static void afterClientStart() {
		RecipePageCrafting.recipeToDisplayAdapterMap.put(RecipeEntryCraftingShapelessConsuming.class, new DisplayAdapterShapeless());
		BTXParticles.init();
	}

	private static GuidebookBook registerModGuidebook() {
		return GuidebookBook.builder("btx:manual")
			.name("btx.guidebook.book.btx")
			.cover(cover -> cover
				.color(0x306333)
				.title("btx.guidebook.cover.title")
				.subtitle("btx.guidebook.cover.subtitle")
			)
			.sections(s -> {
				s.add(new GuidebookIndexSection());
				s.add(GuidebookSections.SEARCH);
				s.add(new GuidebookSectionMillstone());
				s.add(new GuidebookSectionTurntable());
				s.add(new GuidebookSectionSaw());
				s.add(new GuidebookSectionKiln());
				s.add(new GuidebookSectionHopper());
				s.add(new GuidebookSectionHopperFilter());
				s.add(new GuidebookSectionBulk("btx.guidebook.section.cauldron", new ItemStack(BTXBlocks.CAULDRON), 0x404040, 0xC06030,
					() -> BTXRecipes.CAULDRON, () -> BTXRecipes.CAULDRON_STOKED)
				);
				s.add(new GuidebookSectionBulk("btx.guidebook.section.crucible", new ItemStack(BTXBlocks.CRUCIBLE), 0x5A3A2A, 0xE0A040,
					() -> BTXRecipes.CRUCIBLE, () -> BTXRecipes.CRUCIBLE_STOKED)
				);
				s.add(new GuidebookSectionSoulforge());
			}).build();
	}

	public static void initItemModels(ItemModelDispatcher dispatcher) {
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.HEMP_SEEDS).setIcon(item("hemp_seeds")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.HEMP).setIcon(item("hemp")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.GEAR).setIcon(item("gear")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.FLOUR).setIcon(item("flour")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.HEMP_FIBERS).setIcon(item("hemp_fibers")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.SCOURED_LEATHER).setIcon(item("scoured_leather")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.DUNG).setIcon(item("dung")));
		dispatcher.addDispatch(new ItemModelWaterWheel(BTXItems.WATERWHEEL, item("water_wheel"), item("water_wheel_paintable")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.SAIL).setIcon(item("sail")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.WINDMILL).setIcon(item("windmill")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.FABRIC).setIcon(item("fabric")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.WOOD_BLADE).setIcon(item("wood_blade")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.FOUL_FOOD).setIcon(item("foul_food")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.KIBBLE).setIcon(item("kibble")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.COAL_DUST).setIcon(item("coal_dust")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.GROUND_NETHERRACK).setIcon(item("ground_netherrack")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.HELLFIRE_DUST).setIcon(item("hellfire_dust")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.CONCENTRATED_HELLFIRE).setIcon(item("concentrated_hellfire")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.WICKER).setIcon(item("wicker")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.FOOD_WOLF_CHOP_RAW).setIcon(item("food_wolf_chop_raw")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.FOOD_WOLF_CHOP_COOKED).setIcon(item("food_wolf_chop_cooked")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.DONUT).setIcon(item("donut")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.SOULFORGED_STEEL_INGOT).setIcon(item("soulforged_steel_ingot")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.STEEL_ARMOR_PLATE).setIcon(item("steel_armor_plate")));
		dispatcher.addDispatch(handheld(BTXItems.SOULFORGED_STEEL_PICKAXE, "soulforged_steel_pickaxe"));
		dispatcher.addDispatch(handheld(BTXItems.SOULFORGED_STEEL_SHOVEL, "soulforged_steel_shovel"));
		dispatcher.addDispatch(handheld(BTXItems.SOULFORGED_STEEL_MATTOCK, "soulforged_steel_mattock"));
		dispatcher.addDispatch(handheld(BTXItems.SOULFORGED_STEEL_HOE, "soulforged_steel_hoe"));
		dispatcher.addDispatch(handheld(BTXItems.SOULFORGED_STEEL_AXE, "soulforged_steel_axe"));
		dispatcher.addDispatch(handheld(BTXItems.SOULFORGED_STEEL_BATTLEAXE, "soulforged_steel_battleaxe"));
		dispatcher.addDispatch(handheld(BTXItems.SOULFORGED_STEEL_SWORD, "soulforged_steel_sword"));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.SOULFORGED_STEEL_HELMET).setIcon(item("soulforged_steel_helmet")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.SOULFORGED_STEEL_CHESTPLATE).setIcon(item("soulforged_steel_chestplate")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.SOULFORGED_STEEL_LEGGINGS).setIcon(item("soulforged_steel_leggings")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.SOULFORGED_STEEL_BOOTS).setIcon(item("soulforged_steel_boots")));
		dispatcher.addDispatch(new ItemModelStandard(BTXBlocks.GRATE.asItem()).setIcon(item("grate")));
		dispatcher.addDispatch(new ItemModelStandard(BTXBlocks.SLATS.asItem()).setIcon(item("slats")));
		dispatcher.addDispatch(new ItemModelStandard(BTXBlocks.WICKER_PANE.asItem()).setIcon(item("wicker_pane")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.TANNED_LEATHER).setIcon(item("tanned_leather")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.STRAP).setIcon(item("strap")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.BELT).setIcon(item("belt")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.GLUE).setIcon(item("glue")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.COMPOSITE_BOW).setIcon(item("composite_bow")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.BROADHEAD).setIcon(item("broadhead")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.BROADHEAD_ARROW).setIcon(item("broadhead_arrow")));
		dispatcher.addDispatch(new ItemModelStandard(BTXItems.HAFT).setIcon(item("haft")));
	}

	public static void initBlockModels(BlockModelDispatcher dispatcher) {
		dispatcher.addDispatch(new BlockModelHemp<>(BTXBlocks.HEMP));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.MILLSTONE, loadDataModel(block("millstone"))));
		dispatcher.addDispatch(new BlockModelHandCrank<>(BTXBlocks.HAND_CRANK));
		dispatcher.addDispatch(new BlockModelAxle<>(BTXBlocks.AXLE));
		dispatcher.addDispatch(new BlockModelAxle<>(BTXBlocks.AXLE_POWER_SOURCE));
		dispatcher.addDispatch(new BlockModelGearBox<>(BTXBlocks.GEARBOX));
		dispatcher.addDispatch(new BlockModelAxle<>(BTXBlocks.AXLE_PAINTED, true));
		dispatcher.addDispatch(new BlockModelAxle<>(BTXBlocks.AXLE_POWER_SOURCE_PAINTED, true));
		dispatcher.addDispatch(new BlockModelGearBox<>(BTXBlocks.GEARBOX_PAINTED, true));
		dispatcher.addDispatch(new BlockModelGenericSiding<>(BTXBlocks.SIDING_OAK, loadDataModel(block("siding/siding_oak"))));
		for (BTXBlocks.StonePieces pieces : BTXBlocks.STONE_PIECES) {
			String sideTexture = pieces.type().equals("slate") ? "minecraft:block/slate_side" : "minecraft:block/" + pieces.type();
			dispatcher.addDispatch(new BlockModelGenericSiding<>(pieces.siding(), loadDataModel(block("siding/siding_" + pieces.type()))));
			dispatcher.addDispatch(new BlockModelGenericMoulding<>(pieces.moulding(), sideTexture, pieces.type()));
			dispatcher.addDispatch(new BlockModelGenericCorner<>(pieces.corner(), loadDataModel(block("corner/corner_" + pieces.type()))));
		}
		dispatcher.addDispatch(new BlockModelSidingPainted<>(BTXBlocks.SIDING_OAK_PAINTED));
		dispatcher.addDispatch(new BlockModelGenericMoulding<>(BTXBlocks.MOULDING_OAK, "minecraft:block/planks/oak", "oak"));
		dispatcher.addDispatch(new BlockModelMouldingPainted<>(BTXBlocks.MOULDING_OAK_PAINTED));
		dispatcher.addDispatch(new BlockModelGenericCorner<>(BTXBlocks.CORNER_OAK, loadDataModel(block("corner/corner_oak"))));
		dispatcher.addDispatch(new BlockModelCornerPainted<>(BTXBlocks.CORNER_OAK_PAINTED));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.HIBACHI, loadDataModel(block("hibachi"))));
		dispatcher.addDispatch(new BlockModelStokedFire<>(BTXBlocks.STOKED_FIRE));
		dispatcher.addDispatch(new BlockModelBellows<>(BTXBlocks.BELLOWS));
		dispatcher.addDispatch(new BlockModelTurntable(BTXBlocks.TURNTABLE));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.UNFIRED_CRUCIBLE, loadDataModel(block("pottery/unfired_crucible"))));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.UNFIRED_PLANTER, loadDataModel(block("pottery/unfired_planter"))));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.UNFIRED_VASE, loadDataModel(block("pottery/unfired_vase"))));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.VASE, loadDataModel(block("vase/vase"))));
		dispatcher.addDispatch(new BlockModelVasePainted<>(BTXBlocks.VASE_PAINTED));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.KILN, loadDataModel(block("kiln"))));
		dispatcher.addDispatch(new BlockModelCrucible<>(BTXBlocks.CRUCIBLE));
		dispatcher.addDispatch(new BlockModelCauldron<>(BTXBlocks.CAULDRON));
		dispatcher.addDispatch(new BlockModelHopper<>(BTXBlocks.HOPPER));
		dispatcher.addDispatch(new BlockModelGenericPane<>(BTXBlocks.GRATE, block("grate/grate_half")));
		dispatcher.addDispatch(new BlockModelGenericPane<>(BTXBlocks.SLATS, block("slats/slats_half")));
		dispatcher.addDispatch(new BlockModelGenericPane<>(BTXBlocks.WICKER_PANE, block("wicker_pane/wicker_pane_half")));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.WICKER_BLOCK, loadDataModel(block("wicker/wicker_block"))));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.CONCENTRATED_HELLFIRE_BLOCK, loadDataModel(block("concentrated_hellfire_block"))));
		dispatcher.addDispatch(new BlockModelSoulforge<>(BTXBlocks.SOULFORGE));
		dispatcher.addDispatch(new BlockModelSaw<>(BTXBlocks.SAW));
		dispatcher.addDispatch(new BlockModelGenericVeryRotatable<>(BTXBlocks.BLOCK_DISPENSER, loadDataModel(block("block_dispenser/block_dispenser"))));
		dispatcher.addDispatch(new BlockModelDetector<>(BTXBlocks.DETECTOR_BLOCK));
		dispatcher.addDispatch(new BlockModelEmpty<>(BTXBlocks.DETECTOR_LOGIC));
		dispatcher.addDispatch(new BlockModelSpotlight<>(BTXBlocks.DETECTOR_LOGIC_GLOWING));
		dispatcher.addDispatch(new BlockModelLens<>(BTXBlocks.LENS));
		dispatcher.addDispatch(new BlockModelCement<>(BTXBlocks.CEMENT));
		dispatcher.addDispatch(new BlockModelBuddy<>(BTXBlocks.BUDDY_BLOCK));
		dispatcher.addDispatch(new BlockModelGenericPressurePlate<>(BTXBlocks.SOULFORGED_STEEL_PRESSURE_PLATE, "btx:block/pressure_plate/soulforged_steel"));
		dispatcher.addDispatch(new BlockModelRailDetectorTiered<>(BTXBlocks.DETECTOR_RAIL_STONE, "btx:block/rail_detector_stone"));
		dispatcher.addDispatch(new BlockModelRailDetectorTiered<>(BTXBlocks.DETECTOR_RAIL_SOULFORGED_STEEL, "btx:block/rail_detector_soulforged_steel"));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.LIGHT_BULB, loadDataModel(block("light_bulb/light_bulb"))));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.LIGHT_BULB_ON, loadDataModel(block("light_bulb/light_bulb_on"))));
		dispatcher.addDispatch(new BlockModelAnchor<>(BTXBlocks.ANCHOR));
		dispatcher.addDispatch(new BlockModelGeneric<>(BTXBlocks.PULLEY, loadDataModel(block("pulley"))));
		dispatcher.addDispatch(new BlockModelPlatform<>(BTXBlocks.PLATFORM));
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
		dispatcher.assignRenderer(MovingAnchorEntity.class, new MovingAnchorEntityRenderer());
		dispatcher.assignRenderer(MovingPlatformEntity.class, new MovingPlatformEntityRenderer());
		dispatcher.assignRenderer(BlockLiftedByPlatformEntity.class, new BlockLiftedByPlatformEntityRenderer());
		dispatcher.assignRenderer(EntityBroadheadArrow.class, new EntityRendererBroadheadArrow());
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

	private static String item(String name) {
		return BTXMod.MOD_ID + ":item/" + name;
	}

	private static ItemModelStandard handheld(Item item, String name) {
		return new ItemModelStandard(item).setIcon(item(name))
			.setDisplayPos("firstperson_righthand", ItemModelDispatcher.HANDHELD_FIRST_PERSON_RIGHT_HAND)
			.setDisplayPos("firstperson_lefthand", ItemModelDispatcher.HANDHELD_FIRST_PERSON_LEFT_HAND)
			.setDisplayPos("thirdperson_righthand", ItemModelDispatcher.HANDHELD_THIRD_PERSON_RIGHT_HAND)
			.setDisplayPos("thirdperson_lefthand", ItemModelDispatcher.HANDHELD_THIRD_PERSON_LEFT_HAND);
	}
}
