package ivangeevo.btx.achievement.client.pages;

import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.ItemStack;

import java.util.Map;
import java.util.Random;

public class AchievementPageSouls extends BTXAchievementPage {
	private static final Map<Character, Object> WORKS_LEGEND = Map.of(
		'B', Blocks.BRICK_NETHERRACK,
		'S', Blocks.SOULSAND,
		'H', "btx:block/hopper/hopper",
		'G', Blocks.GLOWSTONE,
		'C', "btx:block/cement/wet",
		'F', "btx:block/concentrated_hellfire_block"
	);
	private static final String[] WORKS_FRONT = {
		"BBBBBBBBBBBBBBB",
		"B   G     G   B",
		"B  S   S      B",
		"B  H   H  CCC B",
		"BBBBBBBBBBBBBBB"
	};
	private static final String[] WORKS_BACK = {
		"...............",
		".BBBBBBBBBBBBB.",
		".BBBBBBFBBBBBB.",
		".BBBBBBBBBBBBB.",
		"..............."
	};

	public AchievementPageSouls(String key, ItemStack icon, Achievement root) {
		super(key, icon, root);
	}

	@Override
	protected IconCoordinate tile(Random random, int tileX, int tileY, int jitterX, int jitterY) {
		if (jitterY >= 35) {
			return block(Blocks.BEDROCK);
		}

		int roll = random.nextInt(100);

		if (jitterY <= -12 && roll < 18) {
			return block(Blocks.GLOWSTONE);
		}

		if (jitterY >= 3 && jitterY <= 8) {
			if (roll < 60) {
				return block(Blocks.SOULSAND);
			}
			if (roll < 72) {
				return block(Blocks.SOULSCHIST);
			}
		}

		if (jitterY >= 22 && roll < 3) {
			return tex("btx:block/concentrated_hellfire_block");
		}

		if (roll < 12) {
			return block(Blocks.ORE_NETHERCOAL_NETHERRACK);
		}

		return roll < 56 ? block(Blocks.NETHERRACK) : block(Blocks.COBBLE_NETHERRACK);
	}

	@Override
	protected void decorate(Canvas canvas, Random random) {
		tunnels(canvas, random, 20, 20, 25, 3, 2, 3);
		tunnels(canvas, random, 10, 5, 10, 1, 0, 3);
		tunnels(canvas, random, 14, 8, -10, 2, 1, 2);
		place(canvas, 10 + random.nextInt(10), 14 + random.nextInt(4), WORKS_LEGEND, WORKS_FRONT, WORKS_BACK);
	}

	@Override
	public int backgroundColor() {
		return 0;
	}

	@Override
	public int lineColorLocked(boolean isHovered) {
		return 0x002233;
	}

	@Override
	public int lineColorCanUnlock(boolean isHovered) {
		return 0xFF0000;
	}
}
