package ivangeevo.btx.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ivangeevo.btx.block.BTXBlocks;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicCropsPumpkin;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets pumpkins grow in a soil planter, which counts as always-wet farmland.
 */
@Mixin(BlockLogicCropsPumpkin.class)
public abstract class MixinBlockLogicCropsPumpkin {

	@Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
	private void btx$mayPlaceOnPlanter(Block<?> block, CallbackInfoReturnable<Boolean> cir) {
		if (block == BTXBlocks.PLANTER_SOIL) cir.setReturnValue(true);
	}

	// the soil check under each of the 3x3 tiles
	@WrapOperation(
		method = "getGrowthRate",
		at = @At(value = "INVOKE", ordinal = 0,
			target = "Lnet/minecraft/core/world/World;getBlockType(Lnet/minecraft/core/world/pos/TilePosc;)Lnet/minecraft/core/block/Block;")
	)
	private Block<?> btx$planterIsFarmland(World world, TilePosc pos, Operation<Block<?>> original) {
		Block<?> block = original.call(world, pos);
		return block == BTXBlocks.PLANTER_SOIL ? Blocks.FARMLAND_DIRT : block;
	}

	// the first data read is the wetness check
	@WrapOperation(
		method = "getGrowthRate",
		at = @At(value = "INVOKE", ordinal = 0,
			target = "Lnet/minecraft/core/world/World;getBlockData(Lnet/minecraft/core/world/pos/TilePosc;)I")
	)
	private int btx$planterIsWet(World world, TilePosc pos, Operation<Integer> original) {
		int data = original.call(world, pos);
		return world.getBlockType(pos) == BTXBlocks.PLANTER_SOIL ? data | 1 : data;
	}
}
