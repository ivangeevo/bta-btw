package ivangeevo.btx.achievement.client;

import ivangeevo.btx.achievement.client.pages.AchievementPageHearth;
import ivangeevo.btx.achievement.client.pages.AchievementPageMechanical;
import ivangeevo.btx.achievement.client.pages.AchievementPageOddities;
import ivangeevo.btx.achievement.client.pages.AchievementPageSoulforge;
import ivangeevo.btx.achievement.client.pages.AchievementPageSouls;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.BTXItems;
import net.minecraft.client.gui.achievements.data.AchievementPage;
import net.minecraft.client.gui.achievements.data.AchievementPages;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.item.ItemStack;

import static ivangeevo.btx.achievement.BTXAchievements.*;

public final class BTXAchievementPages {
	public static AchievementPage mechanicalPage;
	public static AchievementPage hearthPage;
	public static AchievementPage soulsPage;
	public static AchievementPage soulforgePage;
	public static AchievementPage odditiesPage;

	private BTXAchievementPages() {}

	public static void init() {
		mechanicalPage = AchievementPages.register(new AchievementPageMechanical("gui.achievements.page.btx.mechanical", new ItemStack(BTXItems.GEAR), GEAR));
		add(mechanicalPage, GEAR, 0, 0);
		add(mechanicalPage, HEMP_SEEDS, 0, -5);
		add(mechanicalPage, HEMP, 2, -5);
		add(mechanicalPage, MILLSTONE, 2, -3);
		add(mechanicalPage, FLOUR, 4, -1);
		add(mechanicalPage, HEMP_FIBERS, 4, -3);
		add(mechanicalPage, FABRIC, 6, -3);
		add(mechanicalPage, SAIL, 8, -3);
		add(mechanicalPage, WINDMILL, 10, -3);
		add(mechanicalPage, WINDMILL_STORM, 12, -4);
		add(mechanicalPage, WATERWHEEL, 2, 1);
		add(mechanicalPage, HAND_CRANK, 0, 3);
		add(mechanicalPage, CRANK_OVERPOWER, 0, 5);
		add(mechanicalPage, GEARBOX, 4, 1);
		add(mechanicalPage, AXLE_NETWORK, 4, 3);
		add(mechanicalPage, AXLE_SNAP, 4, 5);
		add(mechanicalPage, SAW, 6, 1);
		add(mechanicalPage, SAW_CUT, 8, -1);
		add(mechanicalPage, SAW_BREAK, 8, 1);
		add(mechanicalPage, SAW_HURT, 8, 3);
		add(mechanicalPage, TURNTABLE, 6, 4);
		add(mechanicalPage, TURNTABLE_SPIN, 8, 5);
		add(mechanicalPage, BELLOWS, 6, 6);
		add(mechanicalPage, HOPPER, 10, 1);
		add(mechanicalPage, BLOCK_DISPENSER, 12, 1);
		add(mechanicalPage, DETECTOR_BLOCK, 14, 1);
		add(mechanicalPage, LENS, 16, 1);
		add(mechanicalPage, LIGHT_BULB, 14, 3);
		add(mechanicalPage, BUDDY_BLOCK, 16, 3);
		add(mechanicalPage, ANCHOR, 10, 4);
		add(mechanicalPage, PULLEY, 12, 4);
		add(mechanicalPage, PLATFORM, 12, 6);
		add(mechanicalPage, LIFT, 14, 6);

		hearthPage = AchievementPages.register(new AchievementPageHearth("gui.achievements.page.btx.hearth", new ItemStack(BTXBlocks.CAULDRON), CAULDRON));
		add(hearthPage, CAULDRON, 0, 0);
		add(hearthPage, SCOURED_LEATHER, 2, -1);
		add(hearthPage, TANNED_LEATHER, 4, -1);
		add(hearthPage, STRAP, 6, -1);
		add(hearthPage, BELT, 8, -1);
		add(hearthPage, GLUE, 2, 1);
		add(hearthPage, VESSEL_BOOM, 0, 2);
		add(hearthPage, DONUT, 2, 3);
		add(hearthPage, FOUL_FOOD, 0, 5);
		add(hearthPage, HIBACHI, 0, -4);
		add(hearthPage, STOKE_FIRE, 2, -4);
		add(hearthPage, KILN, 4, -4);
		add(hearthPage, CRUCIBLE, 6, -4);
		add(hearthPage, RECLAIM, 8, -5);
		add(hearthPage, INCINERATE, 8, -3);
		add(hearthPage, PLANTER, 4, -6);
		add(hearthPage, VASE, 6, -7);
		add(hearthPage, PAINTED_VASE, 8, -7);

		soulsPage = AchievementPages.register(new AchievementPageSouls("gui.achievements.page.btx.souls", new ItemStack(BTXItems.HELLFIRE_DUST), GROUND_NETHERRACK));
		add(soulsPage, GROUND_NETHERRACK, 0, 0);
		add(soulsPage, SOUL_FILTER, 2, 0);
		add(soulsPage, SOUL_OVERLOAD, 2, 2);
		add(soulsPage, CEMENT, 0, 2);
		add(soulsPage, HELLFIRE_DUST, 4, 0);
		add(soulsPage, CONCENTRATED_HELLFIRE, 6, 0);
		add(soulsPage, HELLFIRE_BLOCK, 8, 0);
		add(soulsPage, COAL_DUST, 0, -2);
		add(soulsPage, COOK_NETHERCOAL, 4, -2);

		soulforgePage = AchievementPages.register(new AchievementPageSoulforge("gui.achievements.page.btx.soulforge", new ItemStack(BTXItems.SOULFORGED_STEEL_INGOT), SFS_INGOT));
		add(soulforgePage, SFS_INGOT, 0, 0);
		add(soulforgePage, SOULFORGE, 2, 0);
		add(soulforgePage, SF_REPAIR, 2, -2);
		add(soulforgePage, HAFT, 4, 0);
		add(soulforgePage, SF_PICKAXE, 6, -3);
		add(soulforgePage, AREA_MINE, 8, -4);
		add(soulforgePage, AREA_TOGGLE, 8, -2);
		add(soulforgePage, SF_SHOVEL, 6, -1);
		add(soulforgePage, MATTOCK, 8, 0);
		add(soulforgePage, SF_AXE, 6, 1);
		add(soulforgePage, BATTLEAXE, 8, 2);
		add(soulforgePage, SF_SWORD, 6, 3);
		add(soulforgePage, SF_HOE, 6, 5);
		add(soulforgePage, ARMOR_PLATE, 2, 3);
		add(soulforgePage, SF_ARMOR, 2, 5);
		add(soulforgePage, SF_FULL_SET, 4, 6);

		odditiesPage = AchievementPages.register(new AchievementPageOddities("gui.achievements.page.btx.oddities", new ItemStack(BTXBlocks.COMPANION_CUBE), WOLF_FED));
		add(odditiesPage, WOLF_FED, 0, 0);
		add(odditiesPage, KIBBLE, 2, -2);
		add(odditiesPage, DUNG, 2, 0);
		add(odditiesPage, DUNG_DYE, 4, 0);
		add(odditiesPage, COMPANION_CUBE, 2, 2);
		add(odditiesPage, COMPANION_SLAB, 4, 2);
		add(odditiesPage, COMPANION_GRIND, 4, 4);
		add(odditiesPage, WOLF_CHOP, 0, -4);
		add(odditiesPage, WOLF_BETRAYAL, 2, -4);
	}

	private static void add(AchievementPage page, Achievement achievement, int x, int y) {
		page.addAchievement(achievement, x, y);
	}
}