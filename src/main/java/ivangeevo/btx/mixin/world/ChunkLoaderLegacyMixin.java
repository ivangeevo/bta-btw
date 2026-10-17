package ivangeevo.btx.mixin.world;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import ivangeevo.btx.world.TickPersistence;
import ivangeevo.btx.world.TickSnapshots;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.chunk.ChunkLoaderLegacy;
import net.minecraft.core.world.pos.TilePos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkLoaderLegacy.class)
public abstract class ChunkLoaderLegacyMixin {
	@Unique private static final String KEY = "BTXTicks";

	@Inject(method = "storeChunkInCompound", at = @At("TAIL"))
	private static void btx$saveTicks(Chunk chunk, World world, CompoundTag tag, CallbackInfo ci) {
		ListTag list = TickSnapshots.get(chunk);
		if (list == null) {
			// sync loader: we are on the world thread
			list = TickSnapshots.build(world, chunk.pos.x, chunk.pos.z);
		}
		tag.putList(KEY, list);
	}

	@Inject(method = "loadChunkIntoWorldFromCompound", at = @At("RETURN"))
	private static void btx$loadTicks(World world, CompoundTag tag, CallbackInfoReturnable<Chunk> cir) {
		if (world.isClientSide || !tag.containsKey(KEY)) return;
		ListTag list = tag.getList(KEY);
		TickPersistence tp = (TickPersistence) world;
		for (int i = 0; i < list.tagCount(); i++) {
			CompoundTag t = (CompoundTag) list.tagAt(i);
			Block<?> block = Blocks.getBlock(t.getInteger("Id"));
			if (block == null) continue;
			tp.btx$queueRestoredTick(
				new TilePos(t.getInteger("X"), t.getInteger("Y"), t.getInteger("Z")),
				block, t.getInteger("Rel"));
		}
	}

}
