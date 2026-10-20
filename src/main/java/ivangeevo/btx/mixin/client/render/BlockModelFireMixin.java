package ivangeevo.btx.mixin.client.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import ivangeevo.btx.block.render.StokedFireOverlay;
import ivangeevo.btx.util.utils.FireUtils;
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

	@ModifyExpressionValue(
		method = "render(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/world/WorldSource;Lnet/minecraft/core/world/pos/TilePosc;)Z",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/core/world/WorldSource;isBlockNormalCube(Lnet/minecraft/core/world/pos/TilePosc;)Z"
		)
	)
	private boolean btx$sideFlamesBesideClingBlock(boolean original,
												   @Local(argsOnly = true) WorldSource worldSource,
												   @Local(argsOnly = true) TilePosc tilePos) {
		return original && !FireUtils.prefersSideDisplay(worldSource, tilePos);
	}

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
