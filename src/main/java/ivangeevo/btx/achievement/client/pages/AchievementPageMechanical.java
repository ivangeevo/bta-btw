package ivangeevo.btx.achievement.client.pages;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.entity.entities.WindMillEntity;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.ItemStack;

import java.util.Map;
import java.util.Random;

public class AchievementPageMechanical extends BTXAchievementPage {
	private static final int SHAFT_Y = 14;
	private static final String[] MACHINES = {
		"btx:block/millstone/millstone_side",
		"btx:block/hopper/hopper",
		"btx:block/saw/saw_front",
		"btx:block/bellows/bellows_front",
		"btx:block/turntable/turntable",
		"btx:block/block_dispenser/front"
	};

	private static final Map<Character, Object> MILL_LEGEND = Map.of(
		'P', Blocks.PLANKS_OAK,
		'C', Blocks.COBBLE_STONE,
		'M', "btx:block/millstone/millstone_side"
	);
	private static final String[] MILL_FRONT = {
		"...P...",
		"..P P..",
		".PP PP.",
		".C   C.",
		".C   C.",
		".C M C.",
		"CCCCCCC"
	};
	private static final String[] MILL_BACK = {
		".......",
		".......",
		".......",
		"..PPP..",
		"..PPP..",
		"..PPP..",
		"......."
	};

	private int shaftStart;
	private int millX;
	private int millY;
	private int windmillGround;
	private WindMillEntity windmill;

	public AchievementPageMechanical(String key, ItemStack icon, Achievement root) {
		super(key, icon, root);
	}

	@Override
	protected IconCoordinate tile(Random random, int tileX, int tileY, int jitterX, int jitterY) {
		if (jitterY >= 35) {
			return block(Blocks.BEDROCK);
		}

		if (jitterY <= 3) {
			return block(Blocks.DIRT);
		}

		int roll = random.nextInt(100);

		if (jitterY <= 6 && roll < 25) {
			return block(Blocks.GRAVEL);
		}

		if (jitterY >= 27) {
			return roll < 6 ? block(Blocks.ORE_IRON_BASALT) : block(Blocks.BASALT);
		}

		if (roll < 7) {
			return block(Blocks.ORE_IRON_STONE);
		}

		if (roll < 12) {
			return block(Blocks.ORE_COAL_STONE);
		}

		return block(Blocks.STONE);
	}

	@Override
	protected void decorate(Canvas canvas, Random random) {
		int[] heights = surface(canvas, random, -6, -2, tex(GRASS_SIDE));

		IconCoordinate hemp = seeThroughTex("btx:block/hemp/hemp_7");

		for (int x = -8; x <= 3; x++) {
			if (random.nextInt(3) != 0) {
				canvas.put(0, hemp, x, height(heights, x) - 1);
				canvas.put(0, hemp, x, height(heights, x) - 2);
			}
		}

		tunnels(canvas, random, 10, 5, 9, 2, 0, 3);

		IconCoordinate planks = block(Blocks.PLANKS_OAK);

		int start = -40 + random.nextInt(6);

		this.shaftStart = start;

		for (int x = start; x <= 80; x++) {
			for (int y = SHAFT_Y - 2; y <= SHAFT_Y + 1; y++) {
				canvas.put(0, null, x, y);
				canvas.put(1, planks, x, y);
			}
			int station = x - start;
			if (station % 12 == 0) {
				canvas.put(0, tex(MACHINES[(station / 12) % MACHINES.length]), x, SHAFT_Y + 1);
			}
		}

		this.millX = 20 + random.nextInt(12);
		this.millY = placeOnSurface(canvas, heights, this.millX, MILL_LEGEND, MILL_FRONT, MILL_BACK, block(Blocks.COBBLE_STONE));
		this.windmillGround = height(heights, this.millX - 9);
	}

	@Override
	protected void scene(AchievementScene scene) {
		for (int x = this.shaftStart; x <= 80; x++) {
			if ((x - this.shaftStart) % 12 == 0) {
				scene.block(BTXBlocks.GEARBOX, 0, x, SHAFT_Y);
			} else {
				scene.block(BTXBlocks.AXLE, 0, x, SHAFT_Y, 0.0F, 90.0F, 0.0F);
			}
		}

		scene.block(BTXBlocks.AXLE, 0, this.millX + 3, this.millY + 1, 90.0F, 0.0F, 90.0F);
		scene.block(BTXBlocks.AXLE, 0, this.millX + 3, this.millY + 2, 90.0F, 0.0F, 90.0F);
		scene.block(BTXBlocks.GEARBOX, 0, this.millX + 3, this.millY + 3);
		scene.block(BTXBlocks.AXLE, 0, this.millX + 3, this.millY + 4, 90.0F, 0.0F, 90.0F);

		if (this.windmill == null) {
			this.windmill = new WindMillEntity(scene.world());
			this.windmill.alignedToX = false;
		}

		this.windmill.rotation = scene.seconds() * -2.4F;

		int hubY = this.windmillGround - 8;

		//scene.block(BTXBlocks.AXLE, 0, this.millX - 9, hubY);
		scene.block(BTXBlocks.AXLE, 0, this.millX + 3, hubY, 0.0F, 90.0F, 0.0F);
		scene.entity(this.windmill, this.millX + 3, hubY);
	}

	@Override
	public int backgroundColor() {
		return 0x76B6FF;
	}
}
