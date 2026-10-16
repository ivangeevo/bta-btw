package ivangeevo.btx.world.save.conversion.twoninefour; // TODO: rename

import com.mojang.logging.LogUtils;
import com.mojang.nbt.NbtIo;
import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import com.mojang.nbt.tags.ShortTag; // ASSUMPTION: class exists in this package (DoubleTag/ListTag/CompoundTag do)
import com.mojang.nbt.tags.Tag;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ivangeevo.btx.BTXGlobals;
import net.minecraft.core.world.Dimension;
import net.minecraft.core.world.ProgressListener;
import net.minecraft.core.world.save.ISaveConverter;
import net.minecraft.core.world.save.ISaveFormat;
import net.minecraft.core.world.save.SaveConverters;
import net.minecraft.core.world.save.SaveFormats;
import net.minecraft.core.world.save.conversion.SaveConverter19132To19133;
import net.minecraft.core.world.save.mcregion.RegionFile;
import net.minecraft.core.world.save.mcregion.RegionFileCache;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * <h2>Why this exists</h2>
 * <p>
 * Item IDs are <strong>NOT converted by a save step</strong>.
 * </p>
 *
 * <p>
 * {@code ItemStack.readFromNBT} converts IDs lazily and blindly:
 * a legacy stack without an {@code "Expanded"} tag and with
 * {@code id >= 256} simply gets {@code id += 16128}.
 * </p>
 *
 * <p>
 * BTW items (legacy IDs {@code 478-523}) would therefore land on
 * IDs {@code 16606-16651}, which are almost certainly real BTA items.
 * </p>
 *
 * <p>
 * For <strong>BTW worlds only</strong>, we rewrite BTW stacks to their
 * <strong>final form</strong> before the stock converter runs:
 * </p>
 *
 * <ul>
 *   <li>Set the final item ID.</li>
 *   <li>Set {@code "Expanded"} to {@code 1}.</li>
 *   <li>Set {@code "Version"} to {@code 19135}.</li>
 * </ul>
 *
 * <p>
 * The stock {@code ItemStack} converter then leaves these stacks alone.
 * </p>
 *
 * <h2>Converter priority</h2>
 * <p>
 * Installed at index {@code 0} of {@code SaveConverters.saveConverters},
 * using the same {@code fromVersion} and {@code toVersion} as the stock
 * step. The selection loop in {@code Minecraft.convertSaveVersion}
 * keeps the <strong>first converter on a tie</strong>, so ours wins
 * and then delegates to the stock converter.
 * </p>
 *
 * <h2>Testing status</h2>
 * <p>
 * <strong>NOT TESTED against a real BTA build.</strong>
 * </p>
 */
public class BTW294SaveConverter implements ISaveConverter {
	private static final Logger LOGGER = BTXGlobals.LOGGER;
	private static final int FROM = 19132;
	private static final int TO = 19133;

	/** b1.7.3 stores an item's ID as 256 + the constructor ID used in the BTW source. */
	public static final int ITEM_ID_OFFSET = 256;

	private static final int FIRST_BTW_BLOCK = 221; // vanilla <= 96, stock old-BTA table <= 148
	private static final int FIRST_BTW_ITEM = 222 + ITEM_ID_OFFSET;
	private static final int LAST_BTW_ITEM = 267 + ITEM_ID_OFFSET;

	// names from the scan (registry.txt)
	private static final Set<String> BTW_TILE_IDS = Set.of("Cement", "Cauldron", "MillStone", "Hopper",
		"BlockDispenser", "Pulley", "Turntable", "UnfiredPottery", "Vase", "Crucible");
	private static final Set<String> BTW_ENTITY_IDS = Set.of("WaterWheel", "WindMill", "MovingAnchor",
		"MovingPlatform", "BlockLiftedByPlatform", "BroadheadArrow");

	public static void install() {
		BTW294ChunkConverter.install();
		if (SaveConverters.saveConverters.stream().noneMatch(c -> c instanceof BTW294SaveConverter)) {
			SaveConverters.saveConverters.add(0, new BTW294SaveConverter());
		}
		LOGGER.info("[BTX] install(): list={}", SaveConverters.saveConverters);
	}

	@Override
	public int fromVersion() {
		return FROM;
	}

	@Override
	public int toVersion() {
		return TO;
	}

	@Override
	public void convertSave(@NotNull CompoundTag levelData, @NotNull File savesDir, @NotNull String worldDirName,
							@NotNull ProgressListener progress) {
		LOGGER.info("[BTX] convertSave called for '{}'", worldDirName);

		ISaveFormat from = SaveFormats.createSaveFormat(FROM, savesDir);
		if (from == null) {
			LOGGER.warn("[BTX] no save format for version {}", FROM);
		}
		else {
			List<File> regions = regionFiles(from, worldDirName);
			LOGGER.info("[BTX] found {} region files", regions.size());

			boolean force = Boolean.getBoolean("btw.forceConvert");
			boolean detected = force || hasBtwStack(levelData) || isBtwWorld(regions);
			LOGGER.info("[BTX] force={}, detected={}", force, detected);

			// -Dbtw.forceConvert=true skips detection (useful for testing with a hand-edited world)
			if (detected) {
				LOGGER.info("BTW world detected: converting BTW items/entities for '{}'", worldDirName);
				rewriteRegions(regions);
				walk(levelData); // Player tag inside level.dat, if present (the caller saves this tag afterwards)
				rewritePlayerFiles(new File(new File(savesDir, worldDirName), "players")); // ASSUMPTION: location
			}
		}
		new SaveConverter19132To19133().convertSave(levelData, savesDir, worldDirName, progress);
	}

	// ---------------------------------------------------------------- detection
	private static boolean isBtwWorld(List<File> regions) {
		for (File region : regions) {
			RegionFile rf = new RegionFile(region);
			for (int x = 0; x < 32; x++) {
				for (int z = 0; z < 32; z++) {
					try (DataInputStream in = rf.getChunkDataInputStream(x, z)) {
						if (in != null && looksBtw(NbtIo.read(in).getCompound("Level"))) {
							return true;
						}
					} catch (IOException e) {
						LOGGER.error("Could not scan chunk {},{} of {}", x, z, region, e);
					}
				}
			}
		}
		return false;
	}

	private static boolean looksBtw(CompoundTag level) {
		for (byte b : level.getByteArray("Blocks")) {
			if ((b & 0xFF) >= FIRST_BTW_BLOCK) return true;
		}
		return hasId(level.getList("TileEntities"), BTW_TILE_IDS)
			|| hasId(level.getList("Entities"), BTW_ENTITY_IDS)
			|| hasBtwStack(level);
	}

	private static boolean hasId(ListTag list, Set<String> ids) {
		if (list != null) {
			for (Tag<?> t : list) {
				if (t instanceof CompoundTag c && ids.contains(c.getString("id"))) {
					return true;
				}
			}
		}
		return false;
	}

	/** True if any not-yet-converted ItemStack in this tag tree has a BTW item/block-item ID. */
	private static boolean hasBtwStack(Tag<?> tag) {
		if (tag instanceof CompoundTag c) {
			if (c.getValue().get("id") instanceof ShortTag && c.containsKey("Count") && c.getByte("Expanded") == 0) {
				int id = c.getShort("id");
				if ((id >= FIRST_BTW_BLOCK && id <= BTW294ChunkConverter.MAX_LEGACY_ID) || (id >= FIRST_BTW_ITEM && id <= LAST_BTW_ITEM)) {
					return true;
				}
			}
			for (Tag<?> child : c.getValues()) {
				if (hasBtwStack(child)) return true;
			}
		} else if (tag instanceof ListTag l) {
			for (Tag<?> child : l) {
				if (hasBtwStack(child)) return true;
			}
		}
		return false;
	}

	// ---------------------------------------------------------------- rewriting
	private static void rewriteRegions(List<File> regions) {
		for (File region : regions) {
			RegionFile rf = new RegionFile(region);
			for (int x = 0; x < 32; x++) {
				for (int z = 0; z < 32; z++) {
					try {
						CompoundTag chunk;
						try (DataInputStream in = rf.getChunkDataInputStream(x, z)) {
							if (in == null) {
								continue;
							}
							chunk = NbtIo.read(in);
						}
						CompoundTag level = chunk.getCompound("Level");
						walk(level); // every ItemStack in Entities and TileEntities
						ListTag entities = level.getList("Entities");
						if (entities != null) {
							for (Tag<?> t : entities) {
								if (t instanceof CompoundTag e) {
									fixEntity(e);
								}
							}
						}
						try (DataOutputStream out = rf.getChunkDataOutputStream(x, z)) {
							NbtIo.write(chunk, out);
						}
					} catch (IOException e) {
						LOGGER.error("Could not rewrite chunk {},{} of {}", x, z, region, e);
					}
				}
			}
		}
		RegionFileCache.flushCache();
	}

	private static void rewritePlayerFiles(File playersDir) {
		File[] files = playersDir.listFiles((d, n) -> n.endsWith(".dat"));
		if (files == null) {
			return;
		}
		for (File f : files) {
			try {
				CompoundTag tag;
				try (InputStream in = Files.newInputStream(f.toPath())) {
					tag = NbtIo.readCompressed(in);
				}
				walk(tag);
				try (OutputStream out = Files.newOutputStream(f.toPath())) {
					NbtIo.writeCompressed(tag, out);
				}
			} catch (IOException e) {
				LOGGER.error("Could not rewrite player file {}", f, e);
			}
		}
	}

	/** Entities that embed block IDs. Their NBT keys come from the scan. */
	private static void fixEntity(CompoundTag e) {
		if ("BlockLiftedByPlatform".equals(e.getString("id"))) {
			int[] c = BTW294ChunkConverter.convertOne(e.getInteger("m_iBlockID"), e.getInteger("m_iBlockMetaData"));
			e.putInt("m_iBlockID", c[0]);
			e.putInt("m_iBlockMetaData", c[1]);
		}
		// TODO BroadheadArrow: "inTile" is a BYTE (read it as unsigned) + "inData"; decide how your arrow stores the block.
		// TODO Nether only: 19134->19135 shifts Pos and TileEntity y, but not BTW's embedded Y values
		//      (associatedPulleyPosJ, m_AssociatedAnchorLastKnownYPos).
	}

	/** Visit every compound/list; anything that looks like an ItemStack (short "id" + "Count") gets rewritten. */
	private static void walk(Tag<?> tag) {
		if (tag instanceof CompoundTag c) {
			if (c.getValue().get("id") instanceof ShortTag && c.containsKey("Count")) {
				BtwItems.rewriteStack(c);
			}
			for (Tag<?> child : c.getValues()) {
				walk(child);
			}
		} else if (tag instanceof ListTag l) {
			for (Tag<?> child : l) {
				walk(child);
			}
		}
	}

	private static List<File> regionFiles(ISaveFormat format, String worldDirName) {
		List<File> out = new ArrayList<>();
		for (Dimension dim : Dimension.getDimensionList().values()) {
			File[] files = new File(format.getDimensionRootDir(worldDirName, dim), "region").listFiles();
			if (files != null) {
				out.addAll(Arrays.asList(files));
			}
		}
		return out;
	}

	// ---------------------------------------------------------------- item rules
	/**
	 * Legacy BTW item/block-item ID -> {final id, final damage}.
	 * <p>
	 * The two kinds are keyed differently because b1.7.3 stores them differently:
	 * <ul>
	 *   <li>Real items ({@link #simple}, {@link #rule}): pass the ID as written in the BTW source
	 *       (e.g. fcWindMillItemID = 238). The 256 offset is added for you; saved range is 478-523.</li>
	 *   <li>Block items ({@link #blockRule}): pass the raw block ID (221-255), no offset.</li>
	 * </ul>
	 */
	public static final class BtwItems {
		public interface Rule {
			int[] apply(int damage);
		}

		private static final Map<Integer, Rule> RULES = new HashMap<>();

		private BtwItems() {}

		/** Real items: pass the ID as written in the BTW source (fcWindMillItemID = 238). Same damage value, new ID. */
		public static void simple(int btwSourceId, int newId) {
			RULES.put(btwSourceId + ITEM_ID_OFFSET, d -> new int[]{newId, d});
		}
		public static void rule(int btwSourceId, Rule rule) {
			RULES.put(btwSourceId + ITEM_ID_OFFSET, rule);
		}

		/** Block items (221-255): stored with the raw block ID, no offset. */
		public static void blockRule(int blockId, Rule rule) {
			RULES.put(blockId, rule);
		}

		/*
		 * Two kinds of rules, keyed differently:
		 *  simple / rule REAL items. Pass the ID as written in the BTW source (fcWindMillItemID = 238); the 256 offset is added for you.
		 *  Saved range is 478-523.
		 * blockRule BLOCK ITEMS (221-255). Pass the raw block ID, no offset.
		 * Block items fall through to the chunk converter unless you add a blockRule.
		 * A blockRule is needed when the item DAMAGE differs from the block METADATA:
		 *   237 Companion cube: damage 0 = cube, >0 = slab   (the block uses meta bit 8)
		 *   246 Panel: damage = block meta (bit 0 wood/stone)
		 *   252 Unfired pottery, 255 Vase, 254 Planter: damage = block meta (check the damage values)
		 * Example:
		 *   blockRule(237, d -> new int[]{d > 0 ? COMPANION_SLAB : COMPANION_CUBE, 0});
		 */

		static void rewriteStack(CompoundTag stack) {
			if (stack.getByte("Expanded") != 0) {
				return; // already in the new format
			}
			int id = stack.getShort("id");
			int damage = stack.getShort("Damage");
			int[] r = null;
			Rule rule = RULES.get(id);
			if (rule != null) {
				r = rule.apply(damage);
			} else if (id >= FIRST_BTW_BLOCK && id <= BTW294ChunkConverter.MAX_LEGACY_ID) {
				r = BTW294ChunkConverter.convertOne(id, damage);
			} else if (id >= FIRST_BTW_ITEM && id <= LAST_BTW_ITEM) {
				LOGGER.warn("No rule for BTW item id {}", id); // add a simple(...) rule
			}
			if (r == null) {
				return; // not a BTW stack: leave it to the stock ItemStack conversion
			}
			stack.putShort("id", (short) r[0]);
			stack.putShort("Damage", (short) r[1]);
			stack.putByte("Expanded", (byte) 1);
			stack.putInt("Version", 19135);
			LOGGER.info("BTW item {}:{} -> {}:{}", id, damage, r[0], r[1]);
		}
	}
}
