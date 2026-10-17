package ivangeevo.btx.mixin.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
	@Shadow @Final private @NotNull Set<NextTickListEntry> tickNextTickSet;
	@Shadow public boolean isClientSide;
	@Shadow public abstract boolean areBlocksLoaded(TilePosc pos, int radius);
	@Shadow public abstract void scheduleBlockUpdate(TilePosc pos, Block<?> block, long delay);

	/** Restored ticks waiting for their area to load. entry.delay is RELATIVE here. */
	@Unique private final Map<TickPersistence.PosKey, NextTickListEntry> btx$restored = new LinkedHashMap<>();

	@Unique private Block<?> btx$tickingBlock;
	@Unique private int btx$tickX, btx$tickY, btx$tickZ;

	@Override
	public long btx$runtime() {
		return this.runtime;
	}

	@Override
	public List<NextTickListEntry> btx$pendingInChunk(int chunkX, int chunkZ) {
		List<NextTickListEntry> result = new ArrayList<>();
		for (NextTickListEntry e : this.tickNextTickList) {
			if ((e.tilePos.x() >> 4) == chunkX && (e.tilePos.z() >> 4) == chunkZ) {
				result.add(e);
			}
		}
		// restored ticks not yet scheduled must survive another save/unload
		for (NextTickListEntry e : this.btx$restored.values()) {
			if ((e.tilePos.x() >> 4) == chunkX && (e.tilePos.z() >> 4) == chunkZ) {
				NextTickListEntry copy = new NextTickListEntry(new TilePos(e.tilePos), e.block);
				copy.setDelay(this.runtime + e.delay); // back to absolute so build() computes the same Rel
				result.add(copy);
			}
		}
		return result;
	}

	@Override
	public void btx$queueRestoredTick(TilePosc pos, Block<?> block, long relativeDelay) {
		NextTickListEntry entry = new NextTickListEntry(new TilePos(pos), block);
		entry.setDelay(relativeDelay);
		this.btx$restored.put(TickPersistence.PosKey.of(pos), entry);
	}

	@Inject(method = "tickPendingTicks", at = @At("HEAD"))
	private void btx$drainRestored(boolean all, CallbackInfoReturnable<Boolean> cir) {
		if (this.isClientSide || this.btx$restored.isEmpty()) return;
		this.btx$restored.values().removeIf(entry -> {
			if (!this.areBlocksLoaded(entry.tilePos, 8)) return false;
			this.scheduleBlockUpdate(entry.tilePos, entry.block, Math.max(1, entry.delay));
			return true;
		});
	}

	@Override
	public boolean btx$isUpdateScheduled(TilePosc pos, Block<?> block) {
		if (this.tickNextTickSet.contains(new NextTickListEntry(new TilePos(pos), block))) {
			return true;
		}
		NextTickListEntry restored = this.btx$restored.get(TickPersistence.PosKey.of(pos));
		return restored != null && restored.block == block;
	}

	// Covers the normal path (tickPendingTicks) and the "immediate" path inside scheduleBlockUpdate.
	@WrapOperation(
		method = {
			"tickPendingTicks(Z)Z",
			"scheduleBlockUpdate(Lnet/minecraft/core/world/pos/TilePosc;Lnet/minecraft/core/block/Block;J)V"
		},
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/core/block/Block;updateTick(Lnet/minecraft/core/world/World;Lnet/minecraft/core/world/pos/TilePosc;Ljava/util/Random;Z)V"
		)
	)
	private void btx$trackTicking(Block instance, World world, TilePosc tilePos, Random rand, boolean isRandomTick, Operation<Void> original) {
		Block<?> prevBlock = this.btx$tickingBlock;
		int px = this.btx$tickX, py = this.btx$tickY, pz = this.btx$tickZ;

		this.btx$tickingBlock = instance;
		this.btx$tickX = tilePos.x();
		this.btx$tickY = tilePos.y();
		this.btx$tickZ = tilePos.z();
		try {
			original.call(instance, world, tilePos, rand, isRandomTick);
		} finally {
			this.btx$tickingBlock = prevBlock;
			this.btx$tickX = px;
			this.btx$tickY = py;
			this.btx$tickZ = pz;
		}
	}

	@Override
	public boolean btx$isUpdatePendingThisTick(TilePosc pos, Block<?> block) {
		return this.btx$tickingBlock != null
			&& this.btx$tickingBlock == block
			&& this.btx$tickX == pos.x()
			&& this.btx$tickY == pos.y()
			&& this.btx$tickZ == pos.z();
	}
}
