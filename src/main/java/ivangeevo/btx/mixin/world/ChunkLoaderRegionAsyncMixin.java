package ivangeevo.btx.mixin.world;

import ivangeevo.btx.world.TickSnapshots;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.chunk.ChunkLoaderRegionAsync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkLoaderRegionAsync.class)
public abstract class ChunkLoaderRegionAsyncMixin {
    @Inject(method = "saveChunk", at = @At("HEAD"))
    private void btx$snapshotTicks(World world, Chunk chunk, CallbackInfo ci) {
        TickSnapshots.capture(world, chunk);
    }
}
