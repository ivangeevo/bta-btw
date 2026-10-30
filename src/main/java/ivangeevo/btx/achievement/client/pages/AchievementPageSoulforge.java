package ivangeevo.btx.achievement.client.pages;

import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.ItemStack;

import java.util.Map;
import java.util.Random;

public class AchievementPageSoulforge extends BTXAchievementPage {
	private static final Map<Character, Object> FORGE_LEGEND = Map.of(
		'S', Blocks.BRICK_STEEL,
		'B', Blocks.BRICK_BASALT,
		'P', Blocks.BASALT_POLISHED,
		'X', "btx:block/soulforged_steel_block/soulforged_steel_block",
		'H', "btx:block/concentrated_hellfire_block",
		'C', "btx:block/crucible/crucible",
		'I', "btx:block/hibachi/hibachi",
		'M', "btx:block/soulforge"
	);
	private static final String[] FORGE_FRONT = {
		"SSSSSSSSSSSSS",
		"SX         XS",
		"SX   C     XS",
		"SX         XS",
		"SX   I  M  XS",
		"BBBBBBBBBBBBB"
	};
	private static final String[] FORGE_BACK = {
		".............",
		"..PPPHPPHPP..",
		"..PPPPPPPPP..",
		"..PPPPPPPPP..",
		"..PPPPPPPPP..",
		"............."
	};

	public AchievementPageSoulforge(String key, ItemStack icon, Achievement root) {
		super(key, icon, root);
	}

	@Override
	protected IconCoordinate tile(Random random, int tileX, int tileY, int jitterX, int jitterY) {
		if (jitterY >= 35) {
			return block(Blocks.BEDROCK);
		}

		int roll = random.nextInt(100);

		if (jitterY >= 22 && roll < 30) {
			return block(Blocks.OBSIDIAN);
		}

		if (jitterY >= -14 && jitterY <= -8) {
			return block(Blocks.SLATE);
		}

		if (roll < 5) {
			return block(Blocks.ORE_IRON_BASALT);
		}

		if (roll < 7) {
			return block(Blocks.ORE_GOLD_BASALT);
		}

		if (roll < 8) {
			return block(Blocks.ORE_DIAMOND_BASALT);
		}

		return block(Blocks.BASALT);
	}

	@Override
	protected void decorate(Canvas canvas, Random random) {
		tunnels(canvas, random, 20, 20, 26, 3, 2, 3);
		tunnels(canvas, random, 12, 6, 11, 2, 0, 3);

		IconCoordinate fire = seeThroughTex("btx:block/stoked_fire/stoked_fire_base_0");
		int[] hallXs = {-18 + random.nextInt(4), 12 + random.nextInt(6)};

		for (int x : hallXs) {
			int y = -3 + random.nextInt(3);
			place(canvas, x, y, FORGE_LEGEND, FORGE_FRONT, FORGE_BACK);
			canvas.put(0, fire, x + 5, y + 3);
		}
	}

	@Override
	public int backgroundColor() {
		return 0x1A0E0A;
	}

	@Override
	public int lineColorCanUnlock(boolean isHovered) {
		return 0xFFAA00;
	}
}
