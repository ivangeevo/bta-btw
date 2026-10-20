package ivangeevo.btx.mixin.entity;

import com.mojang.nbt.tags.CompoundTag;
import ivangeevo.btx.entity.FeedableWolf;
import ivangeevo.btx.util.WolfDungHelper;
import ivangeevo.btx.util.WolfFeedingHelper;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.animal.MobWolf;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBdc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MobWolf.class)
public abstract class MobWolfMixin implements FeedableWolf {
	@Unique private int btx$fedCountdown = -1;

	@Shadow protected abstract float getSoundVolume();

	@Override
	public int btx$getFedCountdown() {
		return this.btx$fedCountdown;
	}

	@Override
	public void btx$setFedCountdown(int fedCountdown) {
		this.btx$fedCountdown = fedCountdown;
	}

	@Override
	public float btx$getSoundVolume() {
		return this.getSoundVolume();
	}

	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void btx$interact(@NotNull Player player, CallbackInfoReturnable<Boolean> cir) {
		if (WolfFeedingHelper.onInteract((MobWolf) (Object) this, player)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void btx$updateFedState(CallbackInfo ci) {
		WolfFeedingHelper.updateFedState((MobWolf) (Object) this);
	}

	@Redirect(method = "updateAI", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/world/World;getEntitiesWithinAABB(Ljava/lang/Class;Lorg/joml/primitives/AABBdc;)Ljava/util/List;"))
	private <T extends Entity> List<T> btx$ignoreDungWhenFetching(World world, Class<T> ofClass, AABBdc aabb) {
		return WolfDungHelper.withoutDung(world.getEntitiesWithinAABB(ofClass, aabb));
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/world/World;getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/core/entity/Entity;Lorg/joml/primitives/AABBdc;)Ljava/util/List;"))
	private List<?> btx$ignoreDungWhenPickingUp(World world, Entity exclude, AABBdc aabb) {
		return WolfDungHelper.withoutDung(world.getEntitiesWithinAABBExcludingEntity(exclude, aabb));
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void btx$writeFedState(@NotNull CompoundTag tag, CallbackInfo ci) {
		WolfFeedingHelper.writeFedState((MobWolf) (Object) this, tag);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void btx$readFedState(@NotNull CompoundTag tag, CallbackInfo ci) {
		WolfFeedingHelper.readFedState((MobWolf) (Object) this, tag);
	}
}