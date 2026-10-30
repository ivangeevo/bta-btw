package ivangeevo.btx.mixin.achievement;

import ivangeevo.btx.achievement.client.pages.AchievementScene;
import ivangeevo.btx.achievement.client.pages.BTXAchievementPage;
import net.minecraft.client.gui.achievements.ScreenAchievements;
import net.minecraft.client.gui.achievements.data.AchievementPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenAchievements.class)
public abstract class ScreenAchievementsMixin {
	@Shadow private int viewportLeft;
	@Shadow private int viewportTop;
	@Shadow private int viewportWidth;
	@Shadow private int viewportHeight;
	@Shadow private double viewportZoom;
	@Shadow private AchievementPage currentPage;

	@Inject(method = "drawBackgroundTiles", at = @At("TAIL"))
	private void btx$renderScene(double shiftX, double shiftY, CallbackInfo ci) {
		if (this.currentPage instanceof BTXAchievementPage page) {
			AchievementScene.render(page, this.viewportLeft, this.viewportTop, this.viewportWidth, this.viewportHeight, this.viewportZoom, shiftX, shiftY);
		}
	}
}