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
		for (NextTickListEntry e : entries.values()) {
			if (e == null) continue;
			CompoundTag t = new CompoundTag();
			t.putInt("X", e.tilePos.x());
			t.putInt("Y", e.tilePos.y());
			t.putInt("Z", e.tilePos.z());
			t.putInt("Id", e.block.id());
			t.putLong("Delay", e.delay);
			ticksTag.addTag(t);
		}
		this.tag.putList("TicksList", ticksTag);
		ci.cancel();
	}
}
