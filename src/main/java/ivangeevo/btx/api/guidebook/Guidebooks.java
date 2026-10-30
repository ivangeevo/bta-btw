package ivangeevo.btx.api.guidebook;

import ivangeevo.btx.impl.guidebook.GuidebookLookupImpl;
import ivangeevo.btx.impl.guidebook.GuidebookNavigationImpl;
import ivangeevo.btx.impl.guidebook.GuidebookPagesImpl;
import ivangeevo.btx.impl.guidebook.GuidebookRegistryImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Entry point for adding books to the guidebook screen.
 * <p>
 * Register books during client initialization:
 * <pre>{@code
 * Guidebooks.register(GuidebookBook.builder("mymod:manual")
 *     .name("mymod.guidebook.manual")
 *     .ribbon("mymod:gui/guidebook/ribbon")
 *     .sections(sections -> {
 *         sections.add(GuidebookSections.SEARCH);
 *         sections.add(new MyRecipeSection());
 *     })
 *     .build());
 * }</pre>
 * Ribbons for switching books appear as soon as more than one book is registered.
 * While a search or R/U lookup is active, results from every book are shown together,
 * BTA's book first, and the open book follows the page being read.
 */
@Environment(EnvType.CLIENT)
public final class Guidebooks {
	/** BTA's own guidebook. Always registered and always first. */
	public static final GuidebookBook VANILLA = GuidebookRegistryImpl.VANILLA;

	private Guidebooks() {}

	/** Registers a book and returns it. Books are ordered by registration, after {@link #VANILLA}. */
	public static GuidebookBook register(@NotNull GuidebookBook book) {
		return GuidebookRegistryImpl.register(book);
	}

	/** All registered books in ribbon order. */
	public static List<GuidebookBook> getBooks() {
		return GuidebookRegistryImpl.getBooks();
	}

	/** The registered book with this id, or {@code null}. */
	public static @Nullable GuidebookBook getBook(@NotNull String id) {
		return GuidebookRegistryImpl.getBook(id);
	}

	/** The book that owns this page in the current page list, or {@code null}. */
	public static @Nullable GuidebookBook getBook(@Nullable GuidebookPage page) {
		return GuidebookPagesImpl.getBook(page);
	}

	/** The book currently open. */
	public static GuidebookBook getActive() {
		return GuidebookNavigationImpl.getActive();
	}

	/** Opens a book, as if its ribbon was clicked. */
	public static void switchTo(@NotNull GuidebookBook book) {
		GuidebookNavigationImpl.switchTo(book);
	}

	/** Whether search text is entered, so the pages are search results instead of whole books. */
	public static boolean isLookupActive() {
		return GuidebookPagesImpl.isLookupActive();
	}

	/**
	 * Shows the recipes or usages of an item, like pressing R or U over it.
	 *
	 * @param mode {@link SearchQuery.SearchMode#RECIPE} or {@link SearchQuery.SearchMode#USAGE}
	 */
	public static void lookup(@NotNull ItemStack stack, @NotNull SearchQuery.SearchMode mode) {
		GuidebookLookupImpl.lookup(stack, mode);
	}
}
