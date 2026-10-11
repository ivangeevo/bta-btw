package ivangeevo.btx.mixin.world;

import ivangeevo.btx.world.TickPersistence;
import net.minecraft.core.NextTickListEntry;
import net.minecraft.core.block.Block;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(World.class)
public abstract class WorldMixin implements TickPersistence {
	@Shadow private long runtime;
	@Shadow @Final private TreeSet<NextTickListEntry> tickNextTickList;
	@Shadow public boolean isClientSide;
	@Shadow public abstract boolean areBlocksLoaded(TilePosc pos, int radius);
	@Shadow public abstract void scheduleBlockUpdate(TilePosc pos, Block<?> block, long delay);

	@Shadow @Final private @NotNull Set<NextTickListEntry> tickNextTickSet;
	@Unique private final Map<TickPersistence.PosKey, NextTickListEntry> btx$restored = new LinkedHashMap<>();

	@Override
	public long btx$runtime() {
		return this.runtime;
	}

	@Override
	public List<NextTickListEntry> btx$pendingInChunk(int chunkX, int chunkZ) {
		List<NextTickListEntry> out = new ArrayList<>();
		for (NextTickListEntry entryList : this.tickNextTickList) {
			if ((entryList.tilePos.x() >> 4) == chunkX && (entryList.tilePos.z() >> 4) == chunkZ) out.add(entryList);
		}
		return out;
	}

	@Override
	public void btx$queueRestoredTick(TilePosc pos, Block<?> block, long relativeDelay) {
		NextTickListEntry entry = new NextTickListEntry(new TilePos(pos), block);
		entry.setDelay(relativeDelay);
		this.btx$restored.put(TickPersistence.PosKey.of(pos), entry); // keyed by position, so reloads don't pile up duplicates
	}

	@Inject(method = "tickPendingTicks", at = @At("HEAD"))
	private void btx$drainRestored(boolean all, CallbackInfoReturnable<Boolean> cir) {
		if (this.isClientSide || this.btx$restored.isEmpty()) return;
		this.btx$restored.values().removeIf(e -> {
			if (!this.areBlocksLoaded(e.tilePos, 8)) return false; // wait until neighbors are loaded
			this.scheduleBlockUpdate(e.tilePos, e.block, Math.max(1, e.delay));
			return true;
		});
	}

	@Override
	public boolean btx$isUpdateScheduled(TilePosc pos, Block<?> block) {
		if (this.tickNextTickSet.contains(new NextTickListEntry(new TilePos(pos), block))) {
			return true;
		}
		// also count restored ticks still waiting for their area to load
		NextTickListEntry restored = this.btx$restored.get(TickPersistence.PosKey.of(pos));
		return restored != null && restored.block == block;
	}

}
