package ivangeevo.btx.world;

import net.minecraft.core.NextTickListEntry;
import net.minecraft.core.block.Block;
import net.minecraft.core.world.pos.TilePosc;

import java.util.List;

/**
 * Lets scheduled block updates survive saving and loading.
 * Implemented on {@code World} by a mixin.
 */
public interface TickPersistence {

	/** Returns the pending scheduled updates located in the given chunk. */
	List<NextTickListEntry> btx$pendingInChunk(int chunkX, int chunkZ);

	/** True while this block's own scheduled tick is running (it has already left the pending set). */
	boolean btx$isUpdatePendingThisTick(TilePosc pos, Block<?> block);

	/** Returns the world's current tick counter. */
	long btx$runtime();

	/**
	 * Queues a scheduled update loaded from disk. It is scheduled once the
	 * surrounding area is loaded.
	 *
	 * @param relativeDelay ticks from now until the update should run
	 */
	void btx$queueRestoredTick(TilePosc pos, Block<?> block, long relativeDelay);

	/** Returns whether an update is pending or waiting to be restored, for this block. */
	boolean btx$isUpdateScheduled(TilePosc pos, Block<?> block);

	/**
	 * Map key identifying a block position, used to track restored ticks by
	 * location so reloading the same position doesn't queue duplicates.
	 * <p>
	 * Stores x, y and z as full ints, so there is no coordinate or world height
	 * limit. Custom world heights are safe, and two different positions can never
	 * share a key.
	 */
	record PosKey(int x, int y, int z) {

		/** Creates a key from a block position. */
		public static PosKey of(TilePosc pos) {
			return new PosKey(pos.x(), pos.y(), pos.z());
		}
	}
}
