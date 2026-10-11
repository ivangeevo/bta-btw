package ivangeevo.btx.mixin.world;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.NextTickListEntry;
import net.minecraft.core.world.chunk.writer.ChunkWriter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkWriter.class)
public abstract class ChunkWriterMixin {
	@Final @Shadow private CompoundTag tag;

	@Inject(method = "putTickList", at = @At("HEAD"), cancellable = true)
	private void btx$putTickList(@NotNull Int2ObjectMap<NextTickListEntry> entries, CallbackInfo ci) {
		ListTag ticksTag = new ListTag();

		for (NextTickListEntry entry : entries.values()) {
			if (entry == null) continue;

			CompoundTag tempTag = new CompoundTag();
			tempTag.putInt("X", entry.tilePos.x());
			tempTag.putInt("Y", entry.tilePos.y());
			tempTag.putInt("Z", entry.tilePos.z());
			tempTag.putInt("Id", entry.block.id());
			tempTag.putLong("Delay", entry.delay);
			ticksTag.addTag(tempTag);
		}

		this.tag.putList("TicksList", ticksTag);

		ci.cancel();
	}

}
