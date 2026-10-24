package ivangeevo.btx.mixin.achievement;

import ivangeevo.btx.achievement.client.BTXAchievementPages;
import net.minecraft.client.gui.achievements.data.AchievementPages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AchievementPages.class)
public abstract class AchievementPagesMixin {
	@Inject(method = "init", at = @At("TAIL"))
	private static void btx$registerPages(CallbackInfo ci) {
		BTXAchievementPages.init();
	}
}