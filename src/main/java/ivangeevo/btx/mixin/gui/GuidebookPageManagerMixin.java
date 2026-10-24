package ivangeevo.btx.mixin.gui;

import ivangeevo.btx.gui.guidebook.BTXGuidebook;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookPageManager;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.GuidebookSections;
import net.minecraft.client.gui.guidebook.SearchableGuidebookSection;
import net.minecraft.client.gui.guidebook.search.GuidebookPageSearch;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(GuidebookPageManager.class)
public abstract class GuidebookPageManagerMixin {
	@Unique
	private static String btx$lastLookup = null;

	@Shadow
	@Final
	private List<GuidebookPage> pages;

	@Shadow
	public abstract void setCurrentPage(int index, boolean addToStack);

	@Shadow
	public abstract GuidebookPage getPageByIndex(int index);

	@Inject(method = "setCurrentPage", at = @At("TAIL"))
	private void followPageToItsBook(int index, boolean addToStack, CallbackInfo ci) {
		BTXGuidebook.followPage((GuidebookPageManager) (Object) this, this.getPageByIndex(index));
	}

	@Inject(method = "updatePages", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/guidebook/GuidebookPageManager;setCurrentPage(IZ)V"))
	private void addOtherBookSearchResults(CallbackInfo ci) {
		SearchQuery query = GuidebookPageManager.searchQuery;
		if (query == null) {
			btx$lastLookup = null;
			return;
		}

		BTXGuidebook.Book active = BTXGuidebook.getActive();
		List<GuidebookSection> activeSections = BTXGuidebook.getSections(active);
		for (GuidebookSection section : BTXGuidebook.getSections(active.other())) {
			if (section instanceof SearchableGuidebookSection searchable && !activeSections.contains(section)) {
				this.pages.addAll(searchable.searchPages(query));
			}
		}

		btx$jumpToFirstLookupResult(query, active);
	}

	@Unique
	private void btx$jumpToFirstLookupResult(SearchQuery query, BTXGuidebook.Book active) {
		String text = GuidebookPageSearch.searchField == null ? null : GuidebookPageSearch.searchField.getText();
		boolean lookup = text != null && (text.startsWith("r:") || text.startsWith("u:")) && text.endsWith("!");
		if (!lookup || text.equals(btx$lastLookup)) {
			return;
		}
		btx$lastLookup = text;

		int index = btx$firstResultIndex(BTXGuidebook.getSections(active), query);
		if (index < 0) {
			index = btx$firstResultIndex(BTXGuidebook.getSections(active.other()), query);
		}
		if (index >= 0) {
			this.setCurrentPage(index, true);
		}
	}

	@Unique
	private int btx$firstResultIndex(List<GuidebookSection> sections, SearchQuery query) {
		for (GuidebookSection section : sections) {
			if (section == GuidebookSections.SEARCH || !(section instanceof SearchableGuidebookSection searchable)) {
				continue;
			}
			List<GuidebookPage> results = searchable.searchPages(query);
			if (results != null && !results.isEmpty()) {
				int index = this.pages.indexOf(results.get(0));
				if (index >= 0) {
					return index;
				}
			}
		}
		return -1;
	}
}