package ivangeevo.btx.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import ivangeevo.btx.block.blocks.util.FireInteraction;
import ivangeevo.btx.util.utils.FireUtils;
import net.minecraft.core.block.BlockLogicFire;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(BlockLogicFire.class)
public abstract class BlockLogicFireMixin {

    // BTW: doesBlockBelowExtinguishFire
	@Inject(method = "updateTick(Lnet/minecraft/core/world/World;Lnet/minecraft/core/world/pos/TilePosc;Ljava/util/Random;Z)V",
		at = @At("HEAD"), cancellable = true)
	private void btx$extinguishFromBelow(World world, TilePosc tilePos, Random rand, boolean isRandomTick, CallbackInfo ci) {
		if (FireUtils.doesBlockBelowExtinguishFire(world, tilePos)) {
			world.setBlockTypeNotify(tilePos, Blocks.AIR);
			ci.cancel();
		}
	}

    // BTW: hasInfiniteBurnNeighbor, OR'd into the tag check
	@ModifyExpressionValue(
		method = "updateTick(Lnet/minecraft/core/world/World;Lnet/minecraft/core/world/pos/TilePosc;Ljava/util/Random;Z)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/core/block/Block;hasTag(Lnet/minecraft/core/data/tag/Tag;)Z"
		)
	)
	private boolean btx$neighborInfiniteBurn(boolean original,
											 @Local(argsOnly = true) World world,
											 @Local(argsOnly = true) TilePosc tilePos) {
		return original || FireUtils.hasInfiniteBurnNeighbor(world, tilePos);
	}
}
