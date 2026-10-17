package ivangeevo.btx.world;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import net.minecraft.core.NextTickListEntry;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class TickSnapshots {
	// weak keys: no leak, and a second save of the same chunk can still find a snapshot
	private static final Map<Chunk, ListTag> SNAPSHOTS = Collections.synchronizedMap(new WeakHashMap<>());

	private TickSnapshots() {}

	/** World thread only. */
	public static void capture(World world, Chunk chunk) {
		SNAPSHOTS.put(chunk, build(world, chunk.pos.x, chunk.pos.z));
	}

	/** Save thread. Returns null if the chunk never went through the async loader. */
	public static ListTag get(Chunk chunk) {
		return SNAPSHOTS.get(chunk);
	}

	/** Must run on the world thread: it reads the world's tick set. */
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
