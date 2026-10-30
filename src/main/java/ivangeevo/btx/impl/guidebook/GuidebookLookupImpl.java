package ivangeevo.btx.impl.guidebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookPageManager;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.ScreenGuidebook;
import net.minecraft.client.gui.guidebook.search.GuidebookPageSearch;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class GuidebookLookupImpl {
	private GuidebookLookupImpl() {
	}

	public static void lookup(ItemStack stack, SearchQuery.SearchMode mode) {
		String prefix = mode == SearchQuery.SearchMode.USAGE ? "u:" : mode == SearchQuery.SearchMode.RECIPE ? "r:" : "";
		String query = prefix + stack.getDisplayName() + "!";
		GuidebookPageManager.searchQuery = SearchQuery.resolve(query);

		if (GuidebookPageSearch.searchField != null) {
			GuidebookPageSearch.searchField.setText(query);
		}

		GuidebookPageManager manager = ScreenGuidebook.getPageManager();
		manager.updatePages();

		int target = GuidebookPagesImpl.getLookupTarget();

		if (target >= 0 && !isShown(manager, target)) {
			manager.setCurrentPage(target, true);
		}
	}

	private static boolean isShown(GuidebookPageManager manager, int index) {
		GuidebookPage page = manager.getPageByIndex(index);
		return page != null && (page == manager.getLeftPage() || page == manager.getRightPage());
	}

	public static int getLookupIndex(GuidebookPageManager manager, GuidebookSection section) {
		int target = GuidebookPagesImpl.getLookupTarget();
		return target >= 0 && GuidebookPagesImpl.isLookupActive() ? target : GuidebookNavigationImpl.getSectionIndex(manager, section);
	}
}
