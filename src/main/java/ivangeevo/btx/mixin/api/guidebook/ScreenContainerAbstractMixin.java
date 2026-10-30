package ivangeevo.btx.mixin.api.guidebook;

import ivangeevo.btx.impl.guidebook.GuidebookLookupImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.container.ScreenContainerAbstract;
import net.minecraft.client.gui.guidebook.GuidebookPageManager;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(ScreenContainerAbstract.class)
public abstract class ScreenContainerAbstractMixin {
	@Redirect(method = "checkKeybind", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/guidebook/GuidebookPageManager;getSectionIndex(Lnet/minecraft/client/gui/guidebook/GuidebookSection;)I"))
	private int btx$lookupIndex(GuidebookPageManager manager, GuidebookSection section) {
		return GuidebookLookupImpl.getLookupIndex(manager, section);
	}
}