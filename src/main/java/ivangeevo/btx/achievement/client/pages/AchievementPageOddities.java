package ivangeevo.btx.achievement.client.pages;

import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.ItemStack;

import java.util.Map;
import java.util.Random;

public class AchievementPageOddities extends BTXAchievementPage {
	private static final Map<Character, Object> CHAMBER_LEGEND = Map.of(
		'Q', Blocks.BRICK_MARBLE,
		'W', Blocks.BLOCK_QUARTZ,
		'C', "btx:block/companion_cube/companion_cube_front",
		'S', "btx:block/companion_cube/companion_cube",
		'D', "btx:block/detector_block/front",
		'U', "btx:block/buddy_block/front"
	);
	private static final String[] CHAMBER_FRONT = {
		"QQQQQQQQQQQ",
		"Q         Q",
		"Q D     U Q",
		"Q         Q",
		"Q  C   S  Q",
		"QQQQQQQQQQQ"
	};
	private static final String[] CHAMBER_BACK = {
		"...........",
		".WWWWWWWWW.",
		".WWWWWWWWW.",
		".WWWWWWWWW.",
		".WWWWWWWWW.",
		"..........."
	};

	private static final Map<Character, Object> DEN_LEGEND = Map.of(
		'D', Blocks.PATH_DIRT,
		'W', Blocks.WOOL
	);
	private static final String[] DEN_FRONT = {
		"   W     ",
		"DDDDDDDDD"
	};

	public AchievementPageOddities(String key, ItemStack icon, Achievement root) {
		super(key, icon, root);
	}

	@Override
	protected IconCoordinate tile(Random random, int tileX, int tileY, int jitterX, int jitterY) {
		if (jitterY >= 35) {
			return block(Blocks.BEDROCK);
		}

		if (jitterY <= 2) {
			return block(Blocks.DIRT);
		}

		int roll = random.nextInt(100);

		if (jitterY >= 26) {
			return roll < 6 ? block(Blocks.ORE_REDSTONE_STONE) : block(Blocks.GRANITE);
		}

		if (roll < 10) {
			return block(Blocks.COBBLE_STONE_MOSSY);
		}

		if (roll < 15) {
			return block(Blocks.ORE_COAL_STONE);
		}

		return block(Blocks.STONE);
	}

	@Override
	protected void decorate(Canvas canvas, Random random) {
		int[] heights = surface(canvas, random, -6, -2, tex(GRASS_SIDE));

		IconCoordinate pumpkin = block(Blocks.PUMPKIN);

		for (int x = MIN_X; x <= MAX_X; x++) {
			if (random.nextInt(9) == 0) {
				canvas.put(0, pumpkin, x, height(heights, x) - 1);
			}
		}

		tunnels(canvas, random, 10, 5, 10, 2, 0, 3);

		int denX = -14 + random.nextInt(6);
		int denY = 9 + random.nextInt(3);

		for (int x = denX; x < denX + 9; x++) {
			for (int y = denY - 3; y < denY; y++) {
				canvas.put(0, null, x, y);
			}
		}

		place(canvas, denX, denY - 1, DEN_LEGEND, DEN_FRONT, null);
		place(canvas, 6 + random.nextInt(8), 16 + random.nextInt(4), CHAMBER_LEGEND, CHAMBER_FRONT, CHAMBER_BACK);
	}

	@Override
	public int backgroundColor() {
		return 0x9FB4FF;
	}
}
