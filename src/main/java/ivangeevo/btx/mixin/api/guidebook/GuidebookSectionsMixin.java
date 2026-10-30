package ivangeevo.btx.mixin.api.guidebook;

import ivangeevo.btx.impl.guidebook.GuidebookRegistryImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.GuidebookSections;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(GuidebookSections.class)
public abstract class GuidebookSectionsMixin {
	@Inject(method = "init", at = @At("TAIL"))
	private static void btx$initBooks(CallbackInfo ci) {
		GuidebookRegistryImpl.onSectionsInit();
	}

	@Inject(method = "getSectionList", at = @At("HEAD"), cancellable = true)
	private static void btx$getBookSections(CallbackInfoReturnable<List<GuidebookSection>> cir) {
		List<GuidebookSection> sections = GuidebookRegistryImpl.getSectionListOverride();
		if (sections != null) {
			cir.setReturnValue(Collections.unmodifiableList(sections));
		}
	}
}