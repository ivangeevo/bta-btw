package ivangeevo.btx.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicStokedFire;
import ivangeevo.btx.block.blocks.util.FireInteraction;
import ivangeevo.btx.util.utils.FireUtils;
import net.minecraft.core.block.BlockLogicFire;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.enums.EnumFireSpread;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(BlockLogicFire.class)
public abstract class MixinBlockLogicFire {

    // BTW: doesBlockBelowExtinguishFire
	@Inject(method = "updateTick(Lnet/minecraft/core/world/World;Lnet/minecraft/core/world/pos/TilePosc;Ljava/util/Random;Z)V",
		at = @At("HEAD"), cancellable = true)
	private void btx$extinguishFromBelow(World world, TilePosc tilePos, Random rand, boolean isRandomTick, CallbackInfo ci) {
		if (FireUtils.doesBlockBelowExtinguishFire(world, tilePos)) {
			world.setBlockTypeNotify(tilePos, Blocks.AIR);
			ci.cancel();
		}
	}

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

	// Fire on top of a stoked fire, or clinging only to stoked fire, has no hitbox, so it reads as
	// part of the stoked fire instead of a separate clickable block.
	@Inject(method = "collisionRayTrace", at = @At("HEAD"), cancellable = true)
	private void btx$noHitboxBesideStokedFire(World world, TilePosc tilePos, Vector3dc start, Vector3dc end,
											  boolean useSelectionBounds, CallbackInfoReturnable<HitResult> cir) {
		if ((Object) this instanceof BlockLogicStokedFire) return;

		TilePos below = tilePos.down(new TilePos());

		// the top half of a stoked fire is never targetable; only the stoked fire block below is
		if (world.getBlockType(below) == BTXBlocks.STOKED_FIRE) {
			cir.setReturnValue(null);
			return;
		}

		// fire with a floor under it sits on that block, not on the stoked fire's side
		if (world.isBlockNormalCube(below) || BlockLogicFire.canBurn(world, below)) return;

		// it clings to every burnable side/top neighbour (BlockLogicFire.getShapeMask); only hide it
		// when all of those are stoked fire, so fire also burning on e.g. a log stays clickable
		boolean besideStokedFire = false;
		for (TilePos neighbor : new TilePos[]{
			tilePos.west(new TilePos()), tilePos.east(new TilePos()),
			tilePos.north(new TilePos()), tilePos.south(new TilePos()), tilePos.up(new TilePos())
		}) {
			if (!BlockLogicFire.canBurn(world, neighbor)) continue;
			if (world.getBlockType(neighbor) != BTXBlocks.STOKED_FIRE) return;
			besideStokedFire = true;
		}

		if (besideStokedFire) {
			cir.setReturnValue(null);
		}
	}

	@Inject(method = "checkBurn", at = {
		@At(value = "INVOKE", target = "Lnet/minecraft/core/block/BlockLogicFire;setBurnResult(Lnet/minecraft/core/world/World;Lnet/minecraft/core/world/pos/TilePosc;)V"),
		@At(value = "INVOKE", target = "Lnet/minecraft/core/world/World;setBlockTypeDataNotify(Lnet/minecraft/core/world/pos/TilePosc;Lnet/minecraft/core/block/Block;I)Z")
	})
	private void btx$onBurned(World world, TilePos tilePos, int chance, Random random, int meta, EnumFireSpread spreadRule, CallbackInfo ci) {
		if (world.getBlockType(tilePos).getLogic() instanceof FireInteraction fireInteraction) {
			fireInteraction.onBurned(world, tilePos);
		}
	}

	@ModifyExpressionValue(
		method = "getShapeMask",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/core/world/WorldSource;isBlockNormalCube(Lnet/minecraft/core/world/pos/TilePosc;)Z"
		)
	)
	private static boolean btx$sideShapeBesideClingBlock(boolean original,
														 @Local(argsOnly = true) WorldSource source,
														 @Local(argsOnly = true) TilePosc tilePos) {
		return original && !FireUtils.prefersSideDisplay(source, tilePos);
	}

	@Inject(method = "canBurn(Lnet/minecraft/core/world/WorldSource;Lnet/minecraft/core/world/pos/TilePosc;)Z", at = @At("HEAD"), cancellable = true)
	private static void btx$fireClingsTo(WorldSource source, TilePosc tilePos, CallbackInfoReturnable<Boolean> cir) {
		if (source.getBlockType(tilePos).getLogic() instanceof FireInteraction fireInteraction
			&& fireInteraction.canFireCling(source, tilePos)) {
			cir.setReturnValue(true);
		}
	}
}
