package ivangeevo.btx.impl.guidebook;

import com.mojang.logging.LogUtils;
import ivangeevo.btx.api.guidebook.BookAwareSection;
import ivangeevo.btx.api.guidebook.GuidebookBook;
import ivangeevo.btx.mixin.api.guidebook.GuidebookSectionsAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public final class GuidebookRegistryImpl {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int MAX_TABS = 16;

	public static final GuidebookBook VANILLA = GuidebookBook.builder("minecraft:guidebook")
		.name("btx.guidebook.book.bta")
		.ribbon("btx:gui/guidebook/ribbon_bta")
		.liveSections(GuidebookSectionsAccessor::btx$getSectionList)
		.build();

	private static final List<GuidebookBook> BOOKS = new ArrayList<>(List.of(VANILLA));
	private static boolean sectionsReady = false;
	private static @Nullable GuidebookBook building = null;

	private GuidebookRegistryImpl() {}

	public static GuidebookBook register(GuidebookBook book) {
		for (GuidebookBook existing : BOOKS) {
			if (existing.getId().equals(book.getId())) {
				throw new IllegalArgumentException("Guidebook " + book.getId() + " is already registered");
			}
		}

		BOOKS.add(book);

		if (sectionsReady) {
			initBook(book);
		}

		return book;
	}

	public static List<GuidebookBook> getBooks() {
		return Collections.unmodifiableList(BOOKS);
	}

	public static @Nullable GuidebookBook getBook(String id) {
		for (GuidebookBook book : BOOKS) {
			if (book.getId().equals(id)) {
				return book;
			}
		}

		return null;
	}

	public static void onSectionsInit() {
		if (sectionsReady) {
			return;
		}

		sectionsReady = true;

		for (GuidebookBook book : BOOKS) {
			initBook(book);
		}
	}

	public static @Nullable List<GuidebookSection> getSectionListOverride() {
		GuidebookBook book = building != null ? building : GuidebookNavigationImpl.getActive();
		return book == VANILLA ? null : book.getSections();
	}

	public static <R> R withBook(GuidebookBook book, Supplier<R> action) {
		GuidebookBook previous = building;

		building = book;

		try {
			return action.get();
		} finally {
			building = previous;
		}
	}

	private static void initBook(GuidebookBook book) {
		withBook(book, () -> {
			book.initSections();
			return null;
		});

		List<GuidebookSection> sections = book.getSections();

		if (sections.size() > MAX_TABS) {
			LOGGER.warn("Guidebook {} has {} sections, only the first {} get a tab", book.getId(), sections.size(), MAX_TABS);
		}

		for (GuidebookSection section : sections) {
			if (section instanceof BookAwareSection aware) {
				aware.onAddedToBook(book);
			}
		}
	}
}
