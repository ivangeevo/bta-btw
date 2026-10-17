package ivangeevo.btx.mixin.client.render;

import ivangeevo.btx.block.render.KilnCookOverlay;
import net.minecraft.client.render.block.model.BlockModel;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModelGeneric.class)
public abstract class MixinBlockModelGeneric {
    @Inject(method = "renderAttached", at = @At("RETURN"), cancellable = true)
    private void btx$kilnCookOverlay(
		TessellatorGeneral tessellator, WorldSource worldSource,
		TilePosc tilePos, boolean cullFaces, IconCoordinate overrideTexture, CallbackInfoReturnable<Boolean> cir
	) {
        boolean drew = cir.getReturnValueZ();
        if (KilnCookOverlay.render((BlockModel<?>) (Object) this, tessellator, worldSource, tilePos, drew, overrideTexture)) {
            cir.setReturnValue(true);
        }
    }
}
