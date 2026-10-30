package ivangeevo.btx.achievement.client.pages;

import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.ItemStack;

import java.util.Map;
import java.util.Random;

public class AchievementPageHearth extends BTXAchievementPage {
	private static final String FIRE = "btx:block/stoked_fire/stoked_fire_base_0";

	private static final Map<Character, Object> KILN_LEGEND = Map.of(
		'B', Blocks.BRICK_CLAY,
		'U', "btx:block/crucible/crucible",
		'V', "btx:block/vase/vase_orange"
	);
	private static final String[] KILN_FRONT = {
		"..BBB..",
		".BB BB.",
		".BU VB.",
		".B   B.",
		".B   B.",
		"BBBBBBB"
	};
	private static final String[] KILN_BACK = {
		".......",
		"...B...",
		"..BBB..",
		"..BBB..",
		"..BBB..",
		"......."
	};

	private static final Map<Character, Object> CELLAR_LEGEND = Map.of(
		'P', Blocks.PLANKS_OAK,
		'K', "btx:block/cauldron/cauldron",
		'R', "btx:block/vase/vase_red",
		'U', "btx:block/vase/vase_blue",
		'Y', "btx:block/vase/vase_yellow",
		'N', "btx:block/planter/planter",
		'S', "btx:block/pane/slats"
	);
	private static final String[] CELLAR_FRONT = {
		"PPPPPPPPPPPPP",
		"P           P",
		"P SSS   SSS P",
		"P K  RUY  N P",
		"PPPPPPPPPPPPP"
	};
	private static final String[] CELLAR_BACK = {
		".............",
		".PPPPPPPPPPP.",
		".PPPPPPPPPPP.",
		".PPPPPPPPPPP.",
		"............."
	};

	public AchievementPageHearth(String key, ItemStack icon, Achievement root) {
		super(key, icon, root);
	}

	@Override
	protected IconCoordinate tile(Random random, int tileX, int tileY, int jitterX, int jitterY) {
		if (jitterY >= 35) {
			return block(Blocks.BEDROCK);
		}

		if (jitterY <= 1) {
			return block(Blocks.DIRT);
		}

		int roll = random.nextInt(100);

		if (jitterY <= 7) {
			return roll < 20 ? block(Blocks.DIRT) : block(Blocks.BLOCK_CLAY);
		}

		if (jitterY <= 11) {
			return roll < 50 ? block(Blocks.GRAVEL) : block(Blocks.SAND);
		}

		if (jitterY >= 28) {
			return roll < 8 ? block(Blocks.ORE_COAL_GRANITE) : block(Blocks.GRANITE);
		}

		return roll < 10 ? block(Blocks.ORE_COAL_LIMESTONE) : block(Blocks.LIMESTONE);
	}

	@Override
	protected void decorate(Canvas canvas, Random random) {
		int[] heights = surface(canvas, random, -7, -3, tex(GRASS_SIDE));

		tunnels(canvas, random, 20, 20, 24, 3, 2, 3);

		IconCoordinate fire = seeThroughTex(FIRE);

		int kilnX = -16 + random.nextInt(6);

		placeOnSurface(canvas, heights, kilnX, KILN_LEGEND, KILN_FRONT, KILN_BACK, block(Blocks.BRICK_CLAY));

		int kilnFloor = Integer.MAX_VALUE;

		for (int col = 0; col < KILN_FRONT[0].length(); col++) {
			kilnFloor = Math.min(kilnFloor, height(heights, kilnX + col));
		}

		for (int col = 2; col <= 4; col++) {
			canvas.put(0, fire, kilnX + col, kilnFloor - 1);
		}

		for (int camp = 0; camp < 3; camp++) {
			int x = 14 + camp * 9 + random.nextInt(4);
			int y = height(heights, x);
			canvas.put(0, tex("btx:block/hibachi/hibachi"), x, y - 1);
			canvas.put(0, fire, x, y - 2);
		}

		place(canvas, 8 + random.nextInt(8), 13 + random.nextInt(3), CELLAR_LEGEND, CELLAR_FRONT, CELLAR_BACK);
	}

	@Override
	public int backgroundColor() {
		return 0xF2AE6E;
	}
}
