package ivangeevo.btx.mixin.achievement;

import ivangeevo.btx.achievement.BTXAchievementTriggers;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerAchievementMixin {
	@Inject(method = "onLivingUpdate", at = @At("TAIL"))
	private void btx$checkAchievements(CallbackInfo ci) {
		BTXAchievementTriggers.tickPlayer((Player) (Object) this);
	}
}