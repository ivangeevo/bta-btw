package ivangeevo.btx.mixin.world;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import ivangeevo.btx.world.TickPersistence;
import net.minecraft.core.NextTickListEntry;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.chunk.ChunkLoaderLegacy;
import net.minecraft.core.world.pos.TilePos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkLoaderLegacy.class)
public abstract class ChunkLoaderLegacyMixin {

	@Inject(method = "storeChunkInCompound", at = @At("TAIL"))
	private static void btx$saveTicks(Chunk chunk, World world, CompoundTag tag, CallbackInfo ci) {
		TickPersistence tp = (TickPersistence) world;
		long now = tp.btx$runtime();
		ListTag list = new ListTag();

		for (NextTickListEntry entry : tp.btx$pendingInChunk(chunk.pos.x, chunk.pos.z)) {
			CompoundTag tempTag = new CompoundTag();
			tempTag.putInt("X", entry.tilePos.x());
			tempTag.putInt("Y", entry.tilePos.y());
			tempTag.putInt("Z", entry.tilePos.z());
			tempTag.putInt("Id", entry.block.id());
			tempTag.putInt("Rel", (int) Math.max(1, entry.delay - now));
			list.addTag(tempTag);
		}

		tag.putList("TicksList", list);
	}

	@Inject(method = "loadChunkIntoWorldFromCompound", at = @At("RETURN"))
	private static void btx$loadTicks(World world, CompoundTag tag, CallbackInfoReturnable<Chunk> cir) {
		if (world.isClientSide || !tag.containsKey("TicksList")) return;
		ListTag list = tag.getList("TicksList");
		TickPersistence tp = (TickPersistence) world;
		for (int i = 0; i < list.tagCount(); i++) {
			CompoundTag tempTag = (CompoundTag) list.tagAt(i);
			Block<?> block = Blocks.getBlock(tempTag.getInteger("Id"));
			if (block == null) continue;
			tp.btx$queueRestoredTick(
				new TilePos(tempTag.getInteger("X"), tempTag.getInteger("Y"), tempTag.getInteger("Z")),
				block, tempTag.getInteger("Rel")
			);
		}
	}

}
