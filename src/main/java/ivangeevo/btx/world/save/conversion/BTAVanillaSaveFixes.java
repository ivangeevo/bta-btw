package ivangeevo.btx.world.save.conversion;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import com.mojang.nbt.tags.Tag;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import org.jetbrains.annotations.Nullable;

// Workarounds for BTA's b1.7.3 save conversion, which assumes old-BTA saves rather than vanilla b1.7.3 ones
// (BTW 2.94 saves are vanilla b1.7.3). Remove this class and its hook calls in BTW294ChunkConverter and
// BTW294SaveConverter once BTA's converter handles doors, portals and armor slots itself.
public final class BTAVanillaSaveFixes {
	// b1.7.3 chunk after the stock 128 -> 256 height expansion: index = (x * 16 + z) * 256 + y
	private static final int CHUNK_SIZE = 65536;
	private static final int OLD_DOOR_WOOD = 64;
	private static final int OLD_DOOR_IRON = 71;
	private static final int OLD_OBSIDIAN = 49;
	private static final int OLD_PORTAL = 90;
	private static final int OLD_DOOR_TOP_BIT = 8;
	private static final int PORTAL_Z_AXIS_BIT = 1;

	private static volatile boolean active = false;

	private BTAVanillaSaveFixes() {}

	public static void runWithChunkFixes(boolean vanillaWorld, Runnable conversion) {
		active = vanillaWorld;
		try {
			conversion.run();
		} finally {
			active = false;
		}
	}

	public static boolean convertBlock(short[] blocks, byte[] metadata, int i, int id, byte @Nullable [] portalAxes) {
		if (!active) {
			return false;
		}

		int meta = metadata[i];

		if (id == OLD_DOOR_WOOD || id == OLD_DOOR_IRON) {
			// b1.7.3 marks the top half with bit 8; BTA uses separate top blocks and bit 8 as the hinge (vanilla doors had none)
			boolean top = (meta & OLD_DOOR_TOP_BIT) != 0;
			Block<?> door = id == OLD_DOOR_WOOD
				? (top ? Blocks.DOOR_PLANKS_OAK_TOP : Blocks.DOOR_PLANKS_OAK_BOTTOM)
				: (top ? Blocks.DOOR_IRON_TOP : Blocks.DOOR_IRON_BOTTOM);
			blocks[i] = (short) door.id();
			metadata[i] = (byte) (meta & 7);
			return true;
		}

		if (id == OLD_PORTAL) {
			// b1.7.3 portals always saved meta 0 and picked their axis from neighbors when rendering
			blocks[i] = (short) Blocks.PORTAL_NETHER.id();
			metadata[i] = portalAxes != null ? portalAxes[i] : 0;
			return true;
		}

		return false;
	}

	public static byte @Nullable [] findPortalAxes(short[] blocks) {
		if (!active || blocks.length != CHUNK_SIZE) {
			return null;
		}

		byte[] axes = new byte[blocks.length];

		for (int i = 0; i < blocks.length; i++) {
			if (blocks[i] != OLD_PORTAL) {
				continue;
			}

			int column = i >> 8;
			int x = column >> 4;
			int z = column & 15;
			int y = i & 255;

			boolean alongX = isAt(blocks, x - 1, y, z, OLD_PORTAL) || isAt(blocks, x + 1, y, z, OLD_PORTAL);
			boolean alongZ = isAt(blocks, x, y, z - 1, OLD_PORTAL) || isAt(blocks, x, y, z + 1, OLD_PORTAL);

			if (!alongX && !alongZ) {
				// the rest of the portal is past the chunk edge: the frame side tells the axis
				alongZ = isAt(blocks, x, y, z - 1, OLD_OBSIDIAN) || isAt(blocks, x, y, z + 1, OLD_OBSIDIAN);
			}

			axes[i] = (byte) (!alongX && alongZ ? PORTAL_Z_AXIS_BIT : 0);
		}

		return axes;
	}

	// b1.7.3 saves armor as slots 100-103 = boots, legs, chest, helmet; BTA reads slot - 100 as HumanArmorShape's
	// ordinal (head first) without remapping, so every piece would land in the mirrored slot
	public static void fixArmorSlots(@Nullable CompoundTag player) {
		if (player == null) {
			return;
		}

		ListTag inventory = player.getList("Inventory");
		if (inventory == null) {
			return;
		}

		for (Tag<?> t : inventory) {
			if (t instanceof CompoundTag stack) {
				int slot = stack.getByte("Slot") & 0xFF;
				if (slot >= 100 && slot <= 103) {
					stack.putByte("Slot", (byte) (203 - slot));
				}
			}
		}
	}

	private static boolean isAt(short[] blocks, int x, int y, int z, int id) {
		if (x < 0 || x > 15 || z < 0 || z > 15) {
			return false;
		}
		return blocks[(x * 16 + z) * 256 + y] == id;
	}
}
