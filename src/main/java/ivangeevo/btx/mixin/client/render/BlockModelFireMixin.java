package ivangeevo.btx.mixin.client.render;

import ivangeevo.btx.block.render.StokedFireOverlay;
import net.minecraft.client.render.block.model.BlockModelFire;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModelFire.class)
public abstract class BlockModelFireMixin {

	@Inject(
		method = "render(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/world/WorldSource;Lnet/minecraft/core/world/pos/TilePosc;)Z",
		at = @At("RETURN")
	)
	private void btx$stokedFireOverlay(TessellatorGeneral t, WorldSource ws, TilePosc pos,
									   CallbackInfoReturnable<Boolean> cir) {
		StokedFireOverlay.render(t, ws, pos);
		// the overlay only draws over stoked fire, so keep the original return value
	}
}
