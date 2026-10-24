package ivangeevo.btx.mixin.world;

import ivangeevo.btx.block.blocks.BlockLogicDetectorLogic;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldDetectorLogicAirMixin {
	@Inject(method = "isAirBlock(Lnet/minecraft/core/world/pos/TilePosc;)Z", at = @At("RETURN"), cancellable = true)
	private void btx$treatDetectorLogicAsAir(TilePosc tilePos, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ() && BlockLogicDetectorLogic.isLogicBlock(((World) (Object) this).getBlockType(tilePos))) {
			cir.setReturnValue(true);
		}
	}
}