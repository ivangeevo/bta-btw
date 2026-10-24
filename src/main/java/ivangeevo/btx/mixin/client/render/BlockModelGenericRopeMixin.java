package ivangeevo.btx.mixin.client.render;

import ivangeevo.btx.block.blocks.BlockLogicAnchor;
import net.minecraft.client.render.block.model.generic.BlockModelGenericRope;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModelGenericRope.class)
public abstract class BlockModelGenericRopeMixin {
	@Inject(method = "canConnect", at = @At("HEAD"), cancellable = true)
	private void btx$anchoredRopeHangsStraight(WorldSource source, TilePosc pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (direction != Direction.DOWN) {
			return;
		}

		if (BlockLogicAnchor.isAnchoredRope(source, pos) || BlockLogicAnchor.isLiftAnchor(source, pos.down(new TilePos()))) {
			cir.setReturnValue(true);
		}
	}
}