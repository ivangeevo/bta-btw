package ivangeevo.btx.achievement;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.BTXItems;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.IItemConvertible;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.collection.NamespaceID;
import org.jetbrains.annotations.Nullable;

import static ivangeevo.btx.achievement.BTXAchievementTriggers.obtain;

public final class BTXAchievements {
	private static final Achievement.Type NORMAL = Achievement.TYPE_NORMAL;
	private static final Achievement.Type SPECIAL = Achievement.TYPE_SPECIAL;
	private static final Achievement.Type SECRET = Achievement.TYPE_SECRET;

	//------------- Mechanical Age -------------//

	public static final Achievement GEAR = create("gear", BTXItems.GEAR, null, NORMAL);
	public static final Achievement HAND_CRANK = create("hand_crank", BTXBlocks.HAND_CRANK, GEAR, NORMAL);
	public static final Achievement CRANK_OVERPOWER = create("crank_overpower", BTXBlocks.HAND_CRANK, HAND_CRANK, SECRET);
	public static final Achievement MILLSTONE = create("millstone", BTXBlocks.MILLSTONE, GEAR, NORMAL);
	public static final Achievement HEMP_SEEDS = create("hemp_seeds", BTXItems.HEMP_SEEDS, null, NORMAL);
	public static final Achievement HEMP = create("hemp", BTXItems.HEMP, HEMP_SEEDS, NORMAL);
	public static final Achievement HEMP_FIBERS = create("hemp_fibers", BTXItems.HEMP_FIBERS, MILLSTONE, NORMAL);
	public static final Achievement FLOUR = create("flour", BTXItems.FLOUR, MILLSTONE, NORMAL);
	public static final Achievement FABRIC = create("fabric", BTXItems.FABRIC, HEMP_FIBERS, NORMAL);
	public static final Achievement SAIL = create("sail", BTXItems.SAIL, FABRIC, NORMAL);
	public static final Achievement WINDMILL = create("windmill", BTXItems.WINDMILL, SAIL, SPECIAL);
	public static final Achievement WINDMILL_STORM = create("windmill_storm", BTXItems.SAIL, WINDMILL, SECRET);
	public static final Achievement WATERWHEEL = create("waterwheel", BTXItems.WATERWHEEL, GEAR, SPECIAL);
	public static final Achievement GEARBOX = create("gearbox", BTXBlocks.GEARBOX, GEAR, NORMAL);
	public static final Achievement AXLE_NETWORK = create("axle_network", BTXBlocks.AXLE, GEARBOX, NORMAL);
	public static final Achievement AXLE_SNAP = create("axle_snap", BTXBlocks.AXLE, GEARBOX, NORMAL);
	public static final Achievement SAW = create("saw", BTXBlocks.SAW, GEARBOX, SPECIAL);
	public static final Achievement SAW_CUT = create("saw_cut", Blocks.PLANKS_OAK, SAW, NORMAL);
	public static final Achievement SAW_BREAK = create("saw_break", BTXItems.GEAR, SAW, SECRET);
	public static final Achievement SAW_HURT = create("saw_hurt", BTXBlocks.SAW, SAW, SECRET);
	public static final Achievement TURNTABLE = create("turntable", BTXBlocks.TURNTABLE, SAW, NORMAL);
	public static final Achievement TURNTABLE_SPIN = create("turntable_spin", BTXBlocks.UNFIRED_VASE, TURNTABLE, NORMAL);
	public static final Achievement BELLOWS = create("bellows", BTXBlocks.BELLOWS, TURNTABLE, NORMAL);
	public static final Achievement HOPPER = create("hopper", BTXBlocks.HOPPER, SAW, NORMAL);
	public static final Achievement BLOCK_DISPENSER = create("block_dispenser", BTXBlocks.BLOCK_DISPENSER, HOPPER, NORMAL);
	public static final Achievement DETECTOR_BLOCK = create("detector_block", BTXBlocks.DETECTOR_BLOCK, BLOCK_DISPENSER, NORMAL);
	public static final Achievement LENS = create("lens", BTXBlocks.LENS, DETECTOR_BLOCK, NORMAL);
	public static final Achievement BUDDY_BLOCK = create("buddy_block", BTXBlocks.BUDDY_BLOCK, DETECTOR_BLOCK, NORMAL);
	public static final Achievement LIGHT_BULB = create("light_bulb", BTXBlocks.LIGHT_BULB, DETECTOR_BLOCK, NORMAL);
	public static final Achievement ANCHOR = create("anchor", BTXBlocks.ANCHOR, GEAR, NORMAL);
	public static final Achievement PULLEY = create("pulley", BTXBlocks.PULLEY, ANCHOR, NORMAL);
	public static final Achievement PLATFORM = create("platform", BTXBlocks.PLATFORM, PULLEY, NORMAL);
	public static final Achievement LIFT = create("lift", BTXBlocks.PLATFORM, PLATFORM, SPECIAL);

	//------------- Hearth & Kiln -------------//

	public static final Achievement CAULDRON = create("cauldron", BTXBlocks.CAULDRON, null, NORMAL);
	public static final Achievement SCOURED_LEATHER = create("scoured_leather", BTXItems.SCOURED_LEATHER, CAULDRON, NORMAL);
	public static final Achievement TANNED_LEATHER = create("tanned_leather", BTXItems.TANNED_LEATHER, SCOURED_LEATHER, NORMAL);
	public static final Achievement STRAP = create("strap", BTXItems.STRAP, TANNED_LEATHER, NORMAL);
	public static final Achievement BELT = create("belt", BTXItems.BELT, STRAP, NORMAL);
	public static final Achievement GLUE = create("glue", BTXItems.GLUE, CAULDRON, NORMAL);
	public static final Achievement VESSEL_BOOM = create("vessel_boom", Blocks.TNT, CAULDRON, SECRET);
	public static final Achievement HIBACHI = create("hibachi", BTXBlocks.HIBACHI, null, NORMAL);
	public static final Achievement STOKE_FIRE = create("stoke_fire", BTXBlocks.BELLOWS, HIBACHI, SPECIAL);
	public static final Achievement KILN = create("kiln", Blocks.BRICK_CLAY, STOKE_FIRE, NORMAL);
	public static final Achievement CRUCIBLE = create("crucible", BTXBlocks.CRUCIBLE, KILN, SPECIAL);
	public static final Achievement RECLAIM = create("reclaim", Items.INGOT_IRON, CRUCIBLE, NORMAL);
	public static final Achievement INCINERATE = create("incinerate", Items.COAL, CRUCIBLE, NORMAL);
	public static final Achievement PLANTER = create("planter", BTXBlocks.PLANTER_SOIL, KILN, NORMAL);
	public static final Achievement VASE = create("vase", BTXBlocks.VASE, KILN, NORMAL);
	public static final Achievement PAINTED_VASE = create("painted_vase", BTXBlocks.VASE_PAINTED, VASE, NORMAL);
	public static final Achievement DONUT = create("donut", BTXItems.DONUT, FLOUR, NORMAL);
	public static final Achievement FOUL_FOOD = create("foul_food", BTXItems.FOUL_FOOD, null, SECRET);

	//------------- Nether & Souls -------------//

	public static final Achievement GROUND_NETHERRACK = create("ground_netherrack", BTXItems.GROUND_NETHERRACK, MILLSTONE, NORMAL);
	public static final Achievement SOUL_FILTER = create("soul_filter", Blocks.SOULSAND, GROUND_NETHERRACK, NORMAL);
	public static final Achievement CEMENT = create("cement", Items.BUCKET_IRON, GROUND_NETHERRACK, NORMAL);
	public static final Achievement HELLFIRE_DUST = create("hellfire_dust", BTXItems.HELLFIRE_DUST, SOUL_FILTER, NORMAL);
	public static final Achievement SOUL_OVERLOAD = create("soul_overload", BTXBlocks.HOPPER, SOUL_FILTER, SECRET);
	public static final Achievement COAL_DUST = create("coal_dust", BTXItems.COAL_DUST, MILLSTONE, NORMAL);
	public static final Achievement NETHERCOAL = create("nethercoal", Items.NETHERCOAL, HELLFIRE_DUST, NORMAL);
	public static final Achievement CONCENTRATED_HELLFIRE = create("concentrated_hellfire", BTXItems.CONCENTRATED_HELLFIRE, HELLFIRE_DUST, NORMAL);
	public static final Achievement HELLFIRE_BLOCK = create("hellfire_block", BTXBlocks.CONCENTRATED_HELLFIRE_BLOCK, CONCENTRATED_HELLFIRE, SPECIAL);

	//------------- Soulforge -------------//

	public static final Achievement SFS_INGOT = create("sfs_ingot", BTXItems.SOULFORGED_STEEL_INGOT, CONCENTRATED_HELLFIRE, SPECIAL);
	public static final Achievement SOULFORGE = create("soulforge", BTXBlocks.SOULFORGE, SFS_INGOT, NORMAL);
	public static final Achievement SF_REPAIR = create("sf_repair", BTXItems.SOULFORGED_STEEL_INGOT, SOULFORGE, NORMAL);
	public static final Achievement HAFT = create("haft", BTXItems.HAFT, SOULFORGE, NORMAL);
	public static final Achievement ARMOR_PLATE = create("armor_plate", BTXItems.STEEL_ARMOR_PLATE, SOULFORGE, NORMAL);
	public static final Achievement SF_PICKAXE = create("sf_pickaxe", BTXItems.SOULFORGED_STEEL_PICKAXE, HAFT, NORMAL);
	public static final Achievement AREA_MINE = create("area_mine", BTXItems.SOULFORGED_STEEL_PICKAXE, SF_PICKAXE, NORMAL);
	public static final Achievement AREA_TOGGLE = create("area_toggle", BTXItems.SOULFORGED_STEEL_PICKAXE, SF_PICKAXE, NORMAL);
	public static final Achievement SF_SHOVEL = create("sf_shovel", BTXItems.SOULFORGED_STEEL_SHOVEL, HAFT, NORMAL);
	public static final Achievement MATTOCK = create("mattock", BTXItems.SOULFORGED_STEEL_MATTOCK, SF_SHOVEL, SPECIAL);
	public static final Achievement SF_AXE = create("sf_axe", BTXItems.SOULFORGED_STEEL_AXE, HAFT, NORMAL);
	public static final Achievement BATTLEAXE = create("battleaxe", BTXItems.SOULFORGED_STEEL_BATTLEAXE, SF_AXE, NORMAL);
	public static final Achievement SF_SWORD = create("sf_sword", BTXItems.SOULFORGED_STEEL_SWORD, HAFT, NORMAL);
	public static final Achievement SF_HOE = create("sf_hoe", BTXItems.SOULFORGED_STEEL_HOE, HAFT, SECRET);
	public static final Achievement SF_ARMOR = create("sf_armor", BTXItems.SOULFORGED_STEEL_CHESTPLATE, ARMOR_PLATE, NORMAL);
	public static final Achievement SF_FULL_SET = create("sf_full_set", BTXItems.SOULFORGED_STEEL_HELMET, SF_ARMOR, SPECIAL);

	//------------- Oddities -------------//

	public static final Achievement WOLF_FED = create("wolf_fed", Items.BONE, null, NORMAL);
	public static final Achievement KIBBLE = create("kibble", BTXItems.KIBBLE, WOLF_FED, NORMAL);
	public static final Achievement DUNG = create("dung", BTXItems.DUNG, WOLF_FED, NORMAL);
	public static final Achievement DUNG_DYE = create("dung_dye", BTXItems.DUNG, DUNG, SECRET);
	public static final Achievement WOLF_CHOP = create("wolf_chop", BTXItems.FOOD_WOLF_CHOP_RAW, null, SECRET);
	public static final Achievement WOLF_BETRAYAL = create("wolf_betrayal", BTXItems.FOOD_WOLF_CHOP_COOKED, WOLF_CHOP, SECRET);
	public static final Achievement COMPANION_CUBE = create("companion_cube", BTXBlocks.COMPANION_CUBE, WOLF_FED, NORMAL);
	public static final Achievement COMPANION_SLAB = create("companion_slab", BTXBlocks.COMPANION_SLAB, COMPANION_CUBE, SECRET);
	public static final Achievement COMPANION_GRIND = create("companion_grind", BTXBlocks.COMPANION_CUBE, COMPANION_CUBE, SECRET);

	static {
		obtain(GEAR, BTXItems.GEAR);
		obtain(HAND_CRANK, BTXBlocks.HAND_CRANK);
		obtain(MILLSTONE, BTXBlocks.MILLSTONE);
		obtain(HEMP_SEEDS, BTXItems.HEMP_SEEDS);
		obtain(HEMP, BTXItems.HEMP);
		obtain(HEMP_FIBERS, BTXItems.HEMP_FIBERS);
		obtain(FLOUR, BTXItems.FLOUR);
		obtain(FABRIC, BTXItems.FABRIC);
		obtain(SAIL, BTXItems.SAIL);
		obtain(GEARBOX, BTXBlocks.GEARBOX);
		obtain(SAW, BTXBlocks.SAW);
		obtain(TURNTABLE, BTXBlocks.TURNTABLE);
		obtain(BELLOWS, BTXBlocks.BELLOWS);
		obtain(HOPPER, BTXBlocks.HOPPER);
		obtain(BLOCK_DISPENSER, BTXBlocks.BLOCK_DISPENSER);
		obtain(DETECTOR_BLOCK, BTXBlocks.DETECTOR_BLOCK);
		obtain(LENS, BTXBlocks.LENS);
		obtain(LIGHT_BULB, BTXBlocks.LIGHT_BULB);
		obtain(BUDDY_BLOCK, BTXBlocks.BUDDY_BLOCK);
		obtain(ANCHOR, BTXBlocks.ANCHOR);
		obtain(PULLEY, BTXBlocks.PULLEY);
		obtain(PLATFORM, BTXBlocks.PLATFORM);

		obtain(CAULDRON, BTXBlocks.CAULDRON);
		obtain(SCOURED_LEATHER, BTXItems.SCOURED_LEATHER);
		obtain(TANNED_LEATHER, BTXItems.TANNED_LEATHER);
		obtain(STRAP, BTXItems.STRAP);
		obtain(BELT, BTXItems.BELT);
		obtain(GLUE, BTXItems.GLUE);
		obtain(HIBACHI, BTXBlocks.HIBACHI);
		obtain(CRUCIBLE, BTXBlocks.CRUCIBLE);
		obtain(PLANTER, BTXBlocks.PLANTER_SOIL);
		obtain(VASE, BTXBlocks.VASE);
		obtain(PAINTED_VASE, BTXBlocks.VASE_PAINTED);
		obtain(DONUT, BTXItems.DONUT);

		obtain(GROUND_NETHERRACK, BTXItems.GROUND_NETHERRACK);
		obtain(HELLFIRE_DUST, BTXItems.HELLFIRE_DUST);
		obtain(COAL_DUST, BTXItems.COAL_DUST);
		obtain(NETHERCOAL, Items.NETHERCOAL);
		obtain(CONCENTRATED_HELLFIRE, BTXItems.CONCENTRATED_HELLFIRE);
		obtain(HELLFIRE_BLOCK, BTXBlocks.CONCENTRATED_HELLFIRE_BLOCK);

		obtain(SFS_INGOT, BTXItems.SOULFORGED_STEEL_INGOT);
		obtain(SOULFORGE, BTXBlocks.SOULFORGE);
		obtain(HAFT, BTXItems.HAFT);
		obtain(ARMOR_PLATE, BTXItems.STEEL_ARMOR_PLATE);
		obtain(SF_PICKAXE, BTXItems.SOULFORGED_STEEL_PICKAXE);
		obtain(SF_SHOVEL, BTXItems.SOULFORGED_STEEL_SHOVEL);
		obtain(MATTOCK, BTXItems.SOULFORGED_STEEL_MATTOCK);
		obtain(SF_AXE, BTXItems.SOULFORGED_STEEL_AXE);
		obtain(BATTLEAXE, BTXItems.SOULFORGED_STEEL_BATTLEAXE);
		obtain(SF_SWORD, BTXItems.SOULFORGED_STEEL_SWORD);
		obtain(SF_HOE, BTXItems.SOULFORGED_STEEL_HOE);

		obtain(DUNG, BTXItems.DUNG);
		obtain(WOLF_CHOP, BTXItems.FOOD_WOLF_CHOP_RAW, BTXItems.FOOD_WOLF_CHOP_COOKED);
		obtain(COMPANION_CUBE, BTXBlocks.COMPANION_CUBE);
		obtain(COMPANION_SLAB, BTXBlocks.COMPANION_SLAB);
	}

	private BTXAchievements() {}

	public static void init() {}

	private static Achievement create(String id, IItemConvertible icon, @Nullable Achievement parent, Achievement.Type type) {
		return new Achievement(NamespaceID.fromPool(BTXMod.MOD_ID, id), BTXMod.MOD_ID + "." + id, icon, parent)
			.setType(type)
			.registerAchievement();
	}
}