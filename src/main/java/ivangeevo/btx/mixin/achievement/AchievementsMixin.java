package ivangeevo.btx.mixin.achievement;

import ivangeevo.btx.achievement.BTXAchievements;
import net.minecraft.core.achievement.Achievements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Achievements.class)
public abstract class AchievementsMixin {
	@Inject(method = "init", at = @At("TAIL"))
	private static void btx$registerAchievements(CallbackInfo ci) {
		BTXAchievements.init();
	}
}