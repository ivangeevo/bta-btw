package ivangeevo.btx.mixin;

import ivangeevo.btx.networking.ClientDataChangeHelper;
import net.minecraft.client.world.WorldClientMP;
import net.minecraft.core.block.Block;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldClientMP.class)
public abstract class MixinWorldClientMP {
	@Unique private Block<?> btx$oldBlock;
	@Unique private int btx$oldData;

	//@Inject(method = "blockChange", at = @At("HEAD"))
	private void btx$capture(TilePosc pos, Block<?> block, int meta, CallbackInfoReturnable<Boolean> cir) {
		World w = (World) (Object) this;
		btx$oldBlock = w.getBlockType(pos);
		btx$oldData = w.getBlockData(pos);
	}

	//@Inject(method = "blockChange", at = @At("RETURN"))
	private void btx$dispatch(TilePosc pos, Block<?> block, int meta, CallbackInfoReturnable<Boolean> cir) {
		ClientDataChangeHelper.notifyChange((World) (Object) this, pos, btx$oldBlock, btx$oldData, block, meta);
	}
}
