package ivangeevo.btx.impl.guidebook;

import com.mojang.logging.LogUtils;
import ivangeevo.btx.api.guidebook.GuidebookBook;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookPageManager;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.GuidebookSections;
import net.minecraft.client.gui.guidebook.SearchableGuidebookSection;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Environment(EnvType.CLIENT)
public final class GuidebookPagesImpl {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Map<GuidebookPage, GuidebookBook> PAGE_BOOKS = new IdentityHashMap<>();
	private static final Map<GuidebookBook, Integer> BROWSE_PAGES = new HashMap<>();
	private static final Set<GuidebookSection> FAILED_SECTIONS = Collections.newSetFromMap(new IdentityHashMap<>());
	private static boolean wasLookup = false;
	private static @Nullable String lastLookup = null;
	private static int lookupTarget = -1;

	private GuidebookPagesImpl() {
	}

	public static boolean isLookupActive() {
		SearchQuery query = GuidebookPageManager.searchQuery;
		return query != null && query.rawQuery != null && !query.rawQuery.trim().isEmpty();
	}

	public static @Nullable GuidebookBook getBook(@Nullable GuidebookPage page) {
		return page == null ? null : PAGE_BOOKS.get(page);
	}

	public static int getLookupTarget() {
		return lookupTarget;
	}

	public static void onScreenClosed() {
		lastLookup = null;
	}

	public static void rememberBrowsePage(GuidebookBook book, int index) {
		BROWSE_PAGES.put(book, index);
	}

	public static int getBrowsePage(GuidebookBook book) {
		return BROWSE_PAGES.getOrDefault(book, 0);
	}

	public static void rebuild(GuidebookPageManager manager, List<GuidebookPage> pages) {
		GuidebookPage shown = manager.getLeftPage() != null ? manager.getLeftPage() : manager.getRightPage();
		int previousIndex = GuidebookNavigationImpl.getStackPage(manager);
		GuidebookBook active = GuidebookNavigationImpl.getActive();
		SearchQuery query = GuidebookPageManager.searchQuery;
		boolean lookup = isLookupActive();

		if (lookup && !wasLookup) {
			rememberBrowsePage(active, Math.max(0, previousIndex));
		}

		pages.clear();
		PAGE_BOOKS.clear();

		Set<GuidebookSection> added = Collections.newSetFromMap(new IdentityHashMap<>());

		if (lookup) {
			for (GuidebookBook book : GuidebookRegistryImpl.getBooks()) {
				collect(book, pages, added, query, book == GuidebookRegistryImpl.VANILLA);
			}
		} else {
			collect(active, pages, added, query, true);
		}

		boolean lookupToggled = lookup != wasLookup;

		wasLookup = lookup;
		lookupTarget = lookup ? findFirstResult(pages) : -1;

		if (lookup) {
			String raw = query.rawQuery;
			if (isExplicitLookup(raw) && !Objects.equals(raw, lastLookup)) {
				lastLookup = raw;
				GuidebookNavigationImpl.resetHistory(manager, Math.max(0, lookupTarget));
				return;
			}
		} else {
			lastLookup = null;
			if (lookupToggled) {
				GuidebookNavigationImpl.resetHistory(manager, getBrowsePage(active));
				return;
			}
		}

		int index = shown == null ? -1 : pages.indexOf(shown);

		if (index < 0) {
			index = previousIndex;
		}

		if (lookupToggled) {
			GuidebookNavigationImpl.resetHistory(manager, index);
		} else {
			GuidebookNavigationImpl.showPage(manager, index);
		}
	}

	private static void collect(GuidebookBook book, List<GuidebookPage> pages, Set<GuidebookSection> added, @Nullable SearchQuery query, boolean includeAll) {
		GuidebookRegistryImpl.withBook(book, () -> {
			for (GuidebookSection section : book.getSections()) {
				if (!added.add(section)) {
					continue;
				}

				List<GuidebookPage> sectionPages = null;
				try {
					if (section instanceof SearchableGuidebookSection searchable && query != null) {
						sectionPages = searchable.searchPages(query);
					} else if (includeAll) {
						sectionPages = section.getPages();
					}
				} catch (RuntimeException e) {
					if (FAILED_SECTIONS.add(section)) {
						LOGGER.error("Guidebook section {} in {} failed to build its pages", section.getTranslationKey(), book.getId(), e);
					}
				}

				if (sectionPages == null) {
					continue;
				}

				for (GuidebookPage page : sectionPages) {
					if (page != null) {
						pages.add(page);
						PAGE_BOOKS.putIfAbsent(page, book);
					}
				}
			}

			return null;
		});
	}

	private static int findFirstResult(List<GuidebookPage> pages) {
		for (int i = 0; i < pages.size(); i++) {
			GuidebookSection section = pages.get(i).getSection();

			if (section instanceof SearchableGuidebookSection && section != GuidebookSections.SEARCH) {
				return i;
			}
		}

		return -1;
	}

	public static int findFirstResult(List<GuidebookPage> pages, GuidebookBook book) {
		for (int i = 0; i < pages.size(); i++) {
			GuidebookPage page = pages.get(i);
			GuidebookSection section = page.getSection();

			if (PAGE_BOOKS.get(page) == book && section instanceof SearchableGuidebookSection && section != GuidebookSections.SEARCH) {
				return i;
			}
		}

		return -1;
	}

	private static boolean isExplicitLookup(String raw) {
		String lower = raw.toLowerCase();
		return (lower.startsWith("r:") || lower.startsWith("u:")) && raw.endsWith("!");
	}
}
