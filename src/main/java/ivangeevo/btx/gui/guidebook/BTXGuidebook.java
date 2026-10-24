package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.mixin.gui.GuidebookSectionsAccessor;
import ivangeevo.btx.mixin.gui.ScreenGuidebookAccessor;
import ivangeevo.btx.sound.BTXSoundTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookPageManager;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.ScreenGuidebook;
import net.minecraft.client.gui.guidebook.SearchableGuidebookSection;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.sound.SoundCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public final class BTXGuidebook {
	public enum Book {
		BTA("btx.guidebook.book.bta", "btx:gui/guidebook/ribbon_bta"),
		BTX("btx.guidebook.book.btx", "btx:gui/guidebook/ribbon_btx");

		public final String translationKey;
		public final String ribbonSprite;

		Book(String translationKey, String ribbonSprite) {
			this.translationKey = translationKey;
			this.ribbonSprite = ribbonSprite;
		}

		public Book other() {
			return this == BTA ? BTX : BTA;
		}
	}

	private static final List<GuidebookSection> btxSections = new ArrayList<>();
	private static final Map<Book, Integer> savedPages = new EnumMap<>(Book.class);
	private static Book active = Book.BTA;
	private static boolean switching = false;

	private BTXGuidebook() {
	}

	public static <T extends GuidebookSection> T register(T section) {
		btxSections.add(section);
		return section;
	}

	public static Book getActive() {
		return active;
	}

	public static List<GuidebookSection> getSections(Book book) {
		return Collections.unmodifiableList(book == Book.BTX ? btxSections : GuidebookSectionsAccessor.btx$getSectionList());
	}

	public static void switchTo(Book book, ScreenGuidebook screen) {
		if (book == active) {
			return;
		}

		GuidebookPageManager pageManager = ScreenGuidebook.getPageManager();
		savedPages.put(active, getCurrentPage(pageManager));
		active = book;
		switching = true;
		try {
			pageManager.updatePages();
			pageManager.setCurrentPage(savedPages.getOrDefault(book, 0), false);
		} finally {
			switching = false;
		}
		((ScreenGuidebookAccessor) screen).btx$initTabList();
		Minecraft.getMinecraft().sndManager.playSound(BTXSoundTypes.GUIDEBOOK_SWITCH_BOOK, SoundCategory.GUI_SOUNDS, 0.8F, 1.0F);
	}

	public static boolean owns(Book book, GuidebookPage page) {
		SearchQuery query = GuidebookPageManager.searchQuery;
		for (GuidebookSection section : getSections(book)) {
			List<GuidebookPage> pages = query != null && section instanceof SearchableGuidebookSection searchable
				? searchable.searchPages(query)
				: section.getPages();
			if (pages != null && pages.contains(page)) {
				return true;
			}
		}
		return false;
	}

	public static void followPage(GuidebookPageManager pageManager, GuidebookPage page) {
		Book other = active.other();
		if (switching || page == null || owns(active, page) || !owns(other, page)) {
			return;
		}
		savedPages.put(active, getCurrentPage(pageManager));
		active = other;
		switching = true;
		try {
			pageManager.updatePages();
			int index = pageManager.getPageIndex(page);
			if (index >= 0) {
				pageManager.setCurrentPage(index, false);
			}
		} finally {
			switching = false;
		}
		if (Minecraft.getMinecraft().currentScreen instanceof ScreenGuidebook screen) {
			((ScreenGuidebookAccessor) screen).btx$initTabList();
		}
	}

	private static int getCurrentPage(GuidebookPageManager pageManager) {
		GuidebookPage page = pageManager.getLeftPage() != null ? pageManager.getLeftPage() : pageManager.getRightPage();
		return page == null ? 0 : Math.max(0, pageManager.getPageIndex(page));
	}
}