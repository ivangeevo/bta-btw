package ivangeevo.btx.mixin.gui;

import ivangeevo.btx.gui.guidebook.BTXGuidebook;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookPageManager;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.SearchableGuidebookSection;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(GuidebookPageManager.class)
public abstract class GuidebookPageManagerMixin {
	@Shadow
	@Final
	private List<GuidebookPage> pages;

	@Inject(method = "updatePages", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/guidebook/GuidebookPageManager;setCurrentPage(IZ)V"))
	private void addOtherBookSearchResults(CallbackInfo ci) {
		SearchQuery query = GuidebookPageManager.searchQuery;
		if (query == null) {
			return;
		}

		BTXGuidebook.Book active = BTXGuidebook.getActive();
		List<GuidebookSection> activeSections = BTXGuidebook.getSections(active);
		for (GuidebookSection section : BTXGuidebook.getSections(active.other())) {
			if (section instanceof SearchableGuidebookSection searchable && !activeSections.contains(section)) {
				this.pages.addAll(searchable.searchPages(query));
			}
		}
	}
}