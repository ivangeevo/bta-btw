package ivangeevo.btx.world;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import net.minecraft.core.NextTickListEntry;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TickSnapshots {
    private static final Map<Chunk, ListTag> SNAPSHOTS = new ConcurrentHashMap<>();

    private TickSnapshots() {}

    /** Call on the thread that owns the world (the one that queues the save). */
    public static void capture(World world, Chunk chunk) {
        SNAPSHOTS.put(chunk, build(world, chunk.pos.x, chunk.pos.z));
    }

    /** Called by the save thread. Returns null if no snapshot was captured. */
    public static ListTag take(Chunk chunk) {
        return SNAPSHOTS.remove(chunk);
    }

    public static ListTag build(World world, int chunkX, int chunkZ) {
        TickPersistence tp = (TickPersistence) world;
        long now = tp.btx$runtime();
        ListTag list = new ListTag();

        for (NextTickListEntry entry : tp.btx$pendingInChunk(chunkX, chunkZ)) {
            CompoundTag t = new CompoundTag();
            t.putInt("X", entry.tilePos.x());
            t.putInt("Y", entry.tilePos.y());
            t.putInt("Z", entry.tilePos.z());
            t.putInt("Id", entry.block.id());
            t.putInt("Rel", (int) Math.max(1, entry.delay - now));
            list.addTag(t);
        }
        return list;
    }
}
