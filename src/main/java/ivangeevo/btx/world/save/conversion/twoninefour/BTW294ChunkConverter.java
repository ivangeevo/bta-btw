package ivangeevo.btx.world.save.conversion.twoninefour;

import java.lang.reflect.Field;

import ivangeevo.btx.block.BTXBlocks;
import net.minecraft.core.world.save.conversion.ChunkConverter;

/**
 * Replaces ChunkConverter.converters[0] (b1.7.3/old-BTA block IDs -> BTA block IDs).
 *  - keeps every stock mapping (copied by reflection; 'mappings' is protected and we only hold a base-typed ref)
 *  - fixes the stock sign-extension problem: SaveConverter19132To19133 stores IDs as signed bytes, so
 *    IDs 128-255 arrive as NEGATIVE shorts and the stock loop skips them (blocks would stay unconverted)
 *  - adds the BTW 2.9x blocks (IDs 221-255)
 *
 * NOT TESTED against a real BTA build. Target IDs are placeholders: fill them from your addon's registry.
 */
public class BTW294ChunkConverter extends ChunkConverter {
    public static final int MAX_LEGACY_ID = 255;

    public static void install() {
        if (!(converters[0] instanceof BTW294ChunkConverter)) {
            converters[0] = new BTW294ChunkConverter(converters[0]);
        }
    }

    public BTW294ChunkConverter(ChunkConverter stock) {
        super(19132, 19133, MAX_LEGACY_ID);
        copyStockMappings(stock);
        registerBtwBlocks();
    }

    private void copyStockMappings(ChunkConverter stock) {
        try {
            Field f = ChunkConverter.class.getDeclaredField("mappings");
            f.setAccessible(true);
            MappingBase[] old = (MappingBase[]) f.get(stock);
            System.arraycopy(old, 0, this.mappings, 0, old.length);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not copy stock block mappings", e);
        }
    }

    /**
     * BTW 2.9x blocks. Metadata layouts come from the scan; "pass" means the layout is kept as-is, which is only
     * correct if your block uses the same bit layout (facing numbering included!). Verify each one.
     */
    private void registerBtwBlocks() {
        // 221 Anvil              pass
        // 222/223 Light block    off/on are separate IDs, no meta
        // 224 Hibachi            pass (bit 4 = lit)
        // 225 Hopper             TE "Hopper"          (meta: re-scan needed)
        // 226 Saw                bits0-2 facing, bit 8 on
        // 227 Platform           (meta: re-scan needed)
        // 228 Cement             TE "Cement"          (meta: re-scan needed)
        // 229 Pulley             TE "Pulley"          (meta: re-scan needed)
        // 230 Obsidian plate     vanilla PressurePlateBlock class, check its meta
        // 231 Moulding / 232 Corner   (Moulding meta: re-scan needed; Corner bits 0-2 + bit 8 unknown)
        // 233 Block dispenser / 234 Cauldron   TEs
        // 235/236 Detector rails vanilla rail meta
        // 237 Companion cube     bit 8 = half-cube (slab)      -> split by bit
        // 238 Detector / 240 Lens  bit0 on, bits1+ facing
        // 241 Hemp crop          0-7 growth, pass
        // 242 Hand crank / 243 Mill stone / 244 Anchor (meta = facing) / 245 Rope
        // 246 Panel              bit0 wood/stone, bits1+ facing -> split by bit
        // 247 Axle               bits0-1 power, bits2-3 axis, pass
        // 248 Gear box           bits0-2 facing, bit 8 on
        // 249 Turntable / 250 Bellows / 251 Stoked fire / 252 Unfired pottery / 253 Crucible
        // 254 Planter / 255 Vase

        // Examples (replace the 0s with your block IDs):
        // addMapping(new MappingSimple(247, 0 /* AXLE */));
        // addMapping(new MappingBitSplit(246, 1, 0 /* PANEL_STONE */, 0 /* PANEL_WOOD */, 1));
		addMapping(new MappingSimple(243, BTXBlocks.MILLSTONE.id()));
		addMapping(new MappingSimple(247, BTXBlocks.AXLE.id()));
		// unfired pottery: meta 0 crucible, 1 planter, 2 vase -> separate blocks
		addMapping(new MappingByMeta(252, BTXBlocks.UNFIRED_CRUCIBLE.id(), BTXBlocks.UNFIRED_PLANTER.id(), BTXBlocks.UNFIRED_VASE.id()));
		// vase: meta is the wool color (0 = white), the same as BTA's DyeColor block meta. White is the plain vase.
		addMapping(new MappingSplitOnZero(255, BTXBlocks.VASE.id(), BTXBlocks.VASE_PAINTED.id()));
		// hopper: keep bit 1 (on) and bit 8 (filter), drop 2.94's full (2) and redstone (4) bits
		addMapping(new MappingMasked(225, BTXBlocks.HOPPER.id(), 1 | 8));
		addMapping(new MappingCompanionCube(237, BTXBlocks.COMPANION_CUBE.id(), BTXBlocks.COMPANION_SLAB.id()));

	}

    /** One block or item stack: returns {newId, newMeta}. Unmapped IDs become {0, 0} like the stock converter. */
    public static int[] convertOne(int id, int meta) {
        short[] b = {(short) id};
        byte[] m = {(byte) meta};
        converters[0].convertBlocksAndMetadata(b, m);
        return new int[]{b[0] & 0xFFFF, m[0] & 0xFF};
    }

    @Override
    public void convertBlocksAndMetadata(short[] blocks, byte[] metadata) {
        for (int i = 0; i < blocks.length; i++) {
            int raw = blocks[i];
            int id = raw < 0 ? raw & 0xFF : raw;   // undo sign extension from the byte->short copy
            if (id >= this.mappings.length) {
                continue;                           // not a legacy ID (already converted)
            }
            MappingBase mapping = this.mappings[id];
            if (mapping == null) {
                blocks[i] = 0;                      // TODO optional: log unmapped IDs here
                metadata[i] = 0;
                continue;
            }
            int[] c = mapping.getConversion(metadata[i]);
            blocks[i] = (short) c[0];
            metadata[i] = (byte) c[1];
        }
    }

    /**
     * One old ID becomes two new blocks depending on one metadata bit.
     * New meta = (old meta without that bit) >> shift.
     */
    protected static final class MappingCompanionCube extends MappingBase {
        private final int cubeId, slabId;

        public MappingCompanionCube(int oldId, int cubeId, int slabId) {
            super(oldId);
            this.cubeId = cubeId;
            this.slabId = slabId;
        }

        @Override
        public int[] getConversion(int meta) {
            return (meta & 8) != 0 ? new int[]{this.slabId, 0} : new int[]{this.cubeId, meta & 7};
        }
    }

    /** One old ID to one new block, keeping only the metadata bits in mask. */
    protected static final class MappingMasked extends MappingBase {
        private final int newId, mask;

        public MappingMasked(int oldId, int newId, int mask) {
            super(oldId);
            this.newId = newId;
            this.mask = mask;
        }

        @Override
        public int[] getConversion(int meta) {
            return new int[]{this.newId, meta & this.mask};
        }
    }

    /** One old ID to one new block per metadata value (meta becomes 0). Metas past the list fall back to the first. */
    protected static final class MappingByMeta extends MappingBase {
        private final int[] idsByMeta;

        public MappingByMeta(int oldId, int... idsByMeta) {
            super(oldId);
            this.idsByMeta = idsByMeta;
        }

        @Override
        public int[] getConversion(int meta) {
            int index = meta >= 0 && meta < this.idsByMeta.length ? meta : 0;
            return new int[]{this.idsByMeta[index], 0};
        }
    }

    /** Meta 0 goes to idZero with meta 0; anything else goes to idOther, keeping the meta. */
    protected static final class MappingSplitOnZero extends MappingBase {
        private final int idZero, idOther;

        public MappingSplitOnZero(int oldId, int idZero, int idOther) {
            super(oldId);
            this.idZero = idZero;
            this.idOther = idOther;
        }

        @Override
        public int[] getConversion(int meta) {
            int m = meta & 0xF;
            return m == 0 ? new int[]{this.idZero, 0} : new int[]{this.idOther, m};
        }
    }

    protected static final class MappingBitSplit extends MappingBase {
        private final int bit, idClear, idSet, shift;

        public MappingBitSplit(int oldId, int bit, int idClear, int idSet, int shift) {
            super(oldId);
            this.bit = bit;
            this.idClear = idClear;
            this.idSet = idSet;
            this.shift = shift;
        }

        @Override
        public int[] getConversion(int meta) {
            return new int[]{(meta & this.bit) != 0 ? this.idSet : this.idClear, (meta & ~this.bit) >> this.shift};
        }
    }
}
