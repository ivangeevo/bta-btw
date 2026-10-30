package ivangeevo.btx.mixin.client.render;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.render.BlockColorPaintedMechanical;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockColorDispatcher.class)
public abstract class BlockColorDispatcherMixin {
	@Inject(method = "reload", at = @At("TAIL"))
	private void btx$addPaintedMechanicalColors(CallbackInfo ci) {
		// reload can run before BTX blocks are registered; it runs again on every world change
		if (BTXBlocks.AXLE_PAINTED == null) {
			return;
		}

		BlockColorDispatcher self = (BlockColorDispatcher) (Object) this;
		BlockColorPaintedMechanical color = new BlockColorPaintedMechanical();
		self.addDispatch(BTXBlocks.AXLE_PAINTED, color);
		self.addDispatch(BTXBlocks.AXLE_POWER_SOURCE_PAINTED, color);
		self.addDispatch(BTXBlocks.GEARBOX_PAINTED, color);
	}
}