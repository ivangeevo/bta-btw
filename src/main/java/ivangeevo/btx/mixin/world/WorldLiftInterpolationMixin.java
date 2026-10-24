package ivangeevo.btx.mixin.world;

import ivangeevo.btx.entity.entities.lift.LiftUtils;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldLiftInterpolationMixin {
	@Inject(method = "updateEntityWithOptionalForce", at = {
		@At(value = "INVOKE", target = "Lnet/minecraft/core/entity/Entity;rideTick()V"),
		@At(value = "INVOKE", target = "Lnet/minecraft/core/entity/Entity;tick()V")
	})
	private void btx$keepLiftPushInterpolated(Entity entity, boolean forceUpdate, CallbackInfo ci) {
		LiftUtils.restoreInterpolation(entity, ((World) (Object) this).getTotalWorldTime());
	}

	@Inject(method = "updateEntityWithOptionalForce", at = {
		@At(value = "INVOKE", target = "Lnet/minecraft/core/entity/Entity;rideTick()V", shift = At.Shift.AFTER),
		@At(value = "INVOKE", target = "Lnet/minecraft/core/entity/Entity;tick()V", shift = At.Shift.AFTER)
	})
	private void btx$snapClientLiftRider(Entity entity, boolean forceUpdate, CallbackInfo ci) {
		if (((World) (Object) this).isClientSide) {
			LiftUtils.snapClientRider(entity);

			if (entity instanceof Player player) {
				LiftUtils.liftPlayerOutOfPlatformBlock(player);
			}
		}
	}
}