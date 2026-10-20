package ivangeevo.btx.mixin;

import ivangeevo.btx.block.blocks.pottery.BlockLogicPlanterGrass;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSugarcane;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Sugar cane in a grass planter doesn't need nearby water */
@Mixin(BlockLogicSugarcane.class)
public abstract class MixinBlockLogicSugarcane {
	@Inject(method = "canPlaceAt", at = @At("HEAD"), cancellable = true)
	private void btx$canPlaceOnPlanter(World world, TilePosc tilePos, CallbackInfoReturnable<Boolean> cir) {
		Block<?> blockBelow = world.getBlockType(tilePos.down(new TilePos()));

		if (blockBelow.getLogic() instanceof BlockLogicPlanterGrass) {
			cir.setReturnValue(true);
		}
	}
}
