package ivangeevo.btx.mixin.api.guidebook;

import ivangeevo.btx.impl.guidebook.GuidebookPagesImpl;
import ivangeevo.btx.impl.guidebook.GuidebookRibbonsImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.ScreenGuidebook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ScreenGuidebook.class)
public abstract class ScreenGuidebookMixin {
	@Inject(method = "removed", at = @At("TAIL"))
	private void btx$onClosed(CallbackInfo ci) {
		GuidebookPagesImpl.onScreenClosed();
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/guidebook/ScreenGuidebook;drawCurrentPageOverlays(IIIF)V"))
	private void btx$renderRibbons(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		GuidebookRibbonsImpl.render((ScreenGuidebook) (Object) this);
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void btx$renderRibbonTooltip(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		GuidebookRibbonsImpl.renderTooltip((ScreenGuidebook) (Object) this, mouseX, mouseY);
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void btx$clickRibbon(int mouseX, int mouseY, int button, CallbackInfo ci) {
		if (GuidebookRibbonsImpl.mouseClicked((ScreenGuidebook) (Object) this, mouseX, mouseY, button)) {
			ci.cancel();
		}
	}
}