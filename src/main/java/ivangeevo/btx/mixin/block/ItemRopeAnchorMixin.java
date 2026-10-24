package ivangeevo.btx.mixin.block;

import ivangeevo.btx.block.blocks.BlockLogicAnchor;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemRope;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRope.class)
public abstract class ItemRopeAnchorMixin {
	@Inject(method = "onUseOnBlock", at = @At("HEAD"), cancellable = true)
	private void btx$extendAnchoredRope(ItemStack selfStack, World world, Player player, TilePosc blockPos, Side side,
										double xPlaced, double yPlaced, CallbackInfoReturnable<Boolean> cir) {
		if (player != null && player.isSneaking() || !BlockLogicAnchor.isAnchoredRope(world, blockPos)) {
			return;
		}

		cir.setReturnValue(BlockLogicAnchor.extendAnchoredRope(world, blockPos, player, selfStack));
	}
}