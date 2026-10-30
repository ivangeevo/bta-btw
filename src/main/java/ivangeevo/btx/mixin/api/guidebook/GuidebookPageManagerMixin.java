package ivangeevo.btx.mixin.api.guidebook;

import ivangeevo.btx.impl.guidebook.GuidebookNavigationImpl;
import ivangeevo.btx.impl.guidebook.GuidebookPagesImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookPageManager;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(GuidebookPageManager.class)
public abstract class GuidebookPageManagerMixin {
	@Shadow @Final private List<GuidebookPage> pages;

	@Inject(method = "updatePages", at = @At("HEAD"), cancellable = true)
	private void btx$rebuildPages(CallbackInfo ci) {
		GuidebookPagesImpl.rebuild((GuidebookPageManager) (Object) this, this.pages);
		ci.cancel();
	}

	@Inject(method = "setCurrentPage", at = @At("TAIL"))
	private void btx$onPageShown(int index, boolean addToStack, CallbackInfo ci) {
		GuidebookNavigationImpl.onPageShown((GuidebookPageManager) (Object) this);
	}

	@Inject(method = "getSectionIndex", at = @At("HEAD"), cancellable = true)
	private void btx$getSectionIndex(GuidebookSection section, CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(GuidebookNavigationImpl.getSectionIndex((GuidebookPageManager) (Object) this, section));
	}
}
