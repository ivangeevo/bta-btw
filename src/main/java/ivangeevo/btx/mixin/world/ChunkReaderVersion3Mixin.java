package ivangeevo.btx.mixin.world;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.NextTickListEntry;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.chunk.reader.ChunkReader;
import net.minecraft.core.world.chunk.reader.ChunkReaderVersion3;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkReaderVersion3.class)
public abstract class ChunkReaderVersion3Mixin extends ChunkReader {

	public ChunkReaderVersion3Mixin(@NotNull CompoundTag chunkTag) {
		super(chunkTag);
	}

	@Inject(method = "getTickList", at = @At("HEAD"), cancellable = true)
	private void btx$getTickList(CallbackInfoReturnable<Int2ObjectMap<NextTickListEntry>> cir) {
		if (!this.tag.containsKey("TicksList")) return;
		ListTag ticksTag = this.tag.getList("TicksList");

		Int2ObjectMap<NextTickListEntry> map = new Int2ObjectArrayMap<>();
		for (int i = 0; i < ticksTag.tagCount(); i++) {
			CompoundTag t = (CompoundTag) ticksTag.tagAt(i);
			Block<?> block = Blocks.getBlock(t.getInteger("Id"));
			if (block == null) continue;

			NextTickListEntry e = new NextTickListEntry(
				new TilePos(t.getInteger("X"), t.getInteger("Y"), t.getInteger("Z")), block);
			e.setDelay(t.getLong("Delay"));
			map.put(map.size(), e);
		}
		cir.setReturnValue(map);
	}}
