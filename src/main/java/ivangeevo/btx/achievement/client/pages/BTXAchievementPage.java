package ivangeevo.btx.achievement.client.pages;

import net.minecraft.client.gui.achievements.ScreenAchievements;
import net.minecraft.client.gui.achievements.data.AchievementPage;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.block.Block;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.helper.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public abstract class BTXAchievementPage extends AchievementPage {
	protected static final int MIN_X = -100;
	protected static final int MAX_X = 100;
	protected static final String GRASS_SIDE = "minecraft:block/grass/side_colored_achievement";

	private final String key;
	private final ItemStack icon;
	private final Achievement root;
	private final Map<Object, IconCoordinate> icons = new HashMap<>();
	private final Set<IconCoordinate> seeThrough = new HashSet<>();

	protected BTXAchievementPage(String key, ItemStack icon, Achievement root) {
		this.key = key;
		this.icon = icon;
		this.root = root;
	}

	protected abstract IconCoordinate tile(Random random, int tileX, int tileY, int jitterX, int jitterY);

	protected abstract void decorate(Canvas canvas, Random random);

	protected void scene(AchievementScene scene) {
	}

	@Override
	public final IconCoordinate getBackgroundTile(ScreenAchievements screen, int layer, Random random, int tileX, int tileY) {
		random.setSeed(random.nextLong() + this.key.hashCode());
		int jitterX = tileX + random.nextInt(3) - random.nextInt(3);
		int jitterY = tileY + random.nextInt(3) - random.nextInt(3);
		return tile(random, tileX, tileY, jitterX, jitterY);
	}

	@Override
	public final void postProcessBackground(ScreenAchievements screen, Random random, ScreenAchievements.BGLayer layer, int orgX, int orgY) {
		random.setSeed(random.nextLong() + this.key.hashCode());
		if (layer.id == 0) {
			decorate(new Canvas(screen, orgX, orgY), random);
		} else if (layer.id == 1) {
			IconCoordinate[] front = screen.getLayer(0).getData();
			IconCoordinate[] back = layer.getData();
			for (int i = 0; i < back.length; i++) {
				if (front[i] != null && !this.seeThrough.contains(front[i])) {
					back[i] = null;
				}
			}
		}
	}

	protected IconCoordinate block(Block<?> block) {
		return this.icons.computeIfAbsent(block, b -> getTextureFromBlock(block));
	}

	protected IconCoordinate tex(String path) {
		return this.icons.computeIfAbsent(path, p -> TextureRegistry.getTexture(path));
	}

	protected IconCoordinate seeThroughTex(String path) {
		IconCoordinate icon = tex(path);
		this.seeThrough.add(icon);
		return icon;
	}

	private IconCoordinate resolve(Object o) {
		if (o instanceof Block<?> b) {
			return block(b);
		}
		if (o instanceof String s) {
			return tex(s);
		}
		return (IconCoordinate) o;
	}

	protected int[] surface(Canvas canvas, Random random, int upper, int lower, IconCoordinate top) {
		int[] heights = new int[MAX_X - MIN_X + 1];
		int y = (upper + lower) / 2;
		int delta = 0;
		for (int x = MIN_X; x <= MAX_X; x++) {
			delta = MathHelper.clamp(delta + random.nextInt(2) - random.nextInt(2), -3, 3);
			int d = Math.abs(delta);
			if (d != 0) {
				y += (int) Math.signum(delta) * random.nextInt(d);
			}
			if (y > upper) {
				delta--;
			}
			if (y < lower) {
				delta++;
			}
			y = Math.min(y, lower + 2);
			heights[x - MIN_X] = y;
			canvas.put(0, top, x, y);
			canvas.clearAbove(x, y - 1);
		}
		
		return heights;
	}

	protected static int height(int[] heights, int x) {
		return heights[MathHelper.clamp(x, MIN_X, MAX_X) - MIN_X];
	}

	protected void tunnels(Canvas canvas, Random random, int chance, int length, int baseY, int yJitter, int minRadius, int radiusJitter) {
		int cave = -1;
		for (int x = MIN_X; x <= MAX_X; x++) {
			if (random.nextInt(chance) == 0 && cave <= 0) {
				cave = length + random.nextInt(5) - random.nextInt(5);
			}
			if (cave > 0) {
				int y = baseY + random.nextInt(yJitter + 1) - random.nextInt(yJitter + 1);
				canvas.carve(x, y, minRadius + random.nextInt(radiusJitter));
			}
			cave--;
		}
	}

	// '.' leaves the tile as it is, ' ' clears it
	protected void place(Canvas canvas, int x, int y, Map<Character, Object> legend, String[] front, String[] back) {
		String[][] layers = {front, back};
		for (int layer = 0; layer < layers.length; layer++) {
			if (layers[layer] == null) {
				continue;
			}
			for (int row = 0; row < layers[layer].length; row++) {
				String line = layers[layer][row];
				for (int col = 0; col < line.length(); col++) {
					char c = line.charAt(col);
					if (c == '.') {
						continue;
					}
					canvas.put(layer, c == ' ' ? null : resolve(Objects.requireNonNull(legend.get(c), "No legend entry for " + c)), x + col, y + row);
				}
			}
		}
	}

	protected int placeOnSurface(Canvas canvas, int[] heights, int x, Map<Character, Object> legend, String[] front, String[] back, IconCoordinate foundation) {
		int width = front[0].length();
		int floorY = Integer.MAX_VALUE;
		for (int col = 0; col < width; col++) {
			floorY = Math.min(floorY, height(heights, x + col));
		}
		for (int col = 0; col < width; col++) {
			for (int y = floorY + 1; y <= height(heights, x + col); y++) {
				canvas.put(0, foundation, x + col, y);
			}
		}
		int topY = floorY - front.length + 1;
		place(canvas, x, topY, legend, front, back);
		return topY;
	}

	@NotNull
	@Override
	public String getName() {
		return I18n.getInstance().translateKey(this.key + ".name");
	}

	@NotNull
	@Override
	public String getDescription() {
		return I18n.getInstance().translateKey(this.key + ".desc");
	}

	@NotNull
	@Override
	public ItemStack getIcon() {
		return this.icon;
	}

	@NotNull
	@Override
	public AchievementEntry onOpenAchievement() {
		return Objects.requireNonNull(getEntry(this.root));
	}

	@Override
	public int backgroundLayers() {
		return 2;
	}

	@Override
	public IconCoordinate getAchievementIcon(Achievement achievement) {
		return TextureRegistry.getTexture(achievement.getType().texture());
	}

	@Override
	public int lineColorLocked(boolean isHovered) {
		return 0;
	}

	@Override
	public int lineColorUnlocked(boolean isHovered) {
		return 0x707070;
	}

	@Override
	public int lineColorCanUnlock(boolean isHovered) {
		return 0x00FF00;
	}

	protected static final class Canvas {
		private final ScreenAchievements screen;
		private final int orgX;
		private final int orgY;

		private Canvas(ScreenAchievements screen, int orgX, int orgY) {
			this.screen = screen;
			this.orgX = orgX;
			this.orgY = orgY;
		}

		public void put(int layer, IconCoordinate icon, int x, int y) {
			this.screen.getLayer(layer).put(icon, x - this.orgX, y - this.orgY);
		}

		public void clear(int x, int y) {
			put(0, null, x, y);
			put(1, null, x, y);
		}

		public void clearAbove(int x, int fromY) {
			for (int y = fromY; y >= this.orgY; y--) {
				clear(x, y);
			}
		}

		public void carve(int centerX, int centerY, int radius) {
			for (int x = centerX - radius; x <= centerX + radius; x++) {
				for (int y = centerY - radius; y < centerY + radius; y++) {
					double dx = x + 0.5D - centerX;
					double dy = y + 0.5D - centerY;
					if (dx * dx + dy * dy < radius * radius) {
						put(0, null, x, y);
					}
				}
			}
		}
	}
}
