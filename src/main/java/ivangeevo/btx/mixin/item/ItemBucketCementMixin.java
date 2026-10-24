package ivangeevo.btx.mixin.item;

import ivangeevo.btx.block.BTXBlocks;
import net.minecraft.core.item.ItemBucket;
import net.minecraft.core.util.collection.NamespaceID;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemBucket.class)
public abstract class ItemBucketCementMixin {
	@Inject(method = "getFluidStateAt", at = @At("HEAD"), cancellable = true)
	private void btx$preventScoopingCement(World world, TilePosc tilePos, CallbackInfoReturnable<NamespaceID> cir) {
		if (world.getBlockType(tilePos) == BTXBlocks.CEMENT) {
			cir.setReturnValue(null);
		}
	}
}