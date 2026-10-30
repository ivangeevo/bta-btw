package ivangeevo.btx.api.guidebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.index.GuidebookPageIndex;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * A table of contents for its book, like BTA's index tab.
 * <p>
 * It lists every section added after it, with page numbers, and refreshes when their page counts change.
 * Add it right after the book's cover.
 */
@Environment(EnvType.CLIENT)
public class GuidebookIndexSection extends GuidebookSection implements BookAwareSection {
	private static final int LINES_PER_PAGE = GuidebookPageIndex.LINES_PER_PAGE;

	private GuidebookBook book;
	private final List<GuidebookPage> pages = new ArrayList<>();
	private List<Integer> pageCounts = null;

	/** An index tab with BTA's compass icon and colors. */
	public GuidebookIndexSection() {
		this("guidebook.section.index", new ItemStack(Items.TOOL_COMPASS), 0x0094FF, 0xC29A1C);
	}

	public GuidebookIndexSection(String translationKey, ItemStack tabIcon, int bgColor, int fgColor) {
		super(translationKey, tabIcon, bgColor, fgColor);
	}

	@Override
	public void onAddedToBook(GuidebookBook book) {
		this.book = book;
		this.pageCounts = null;
	}

	@Override
	public List<GuidebookPage> getPages() {
		List<GuidebookSection> listed = getListedSections();
		List<Integer> counts = new ArrayList<>();

		for (GuidebookSection section : listed) {
			List<GuidebookPage> sectionPages = section.getPages();
			counts.add(sectionPages == null ? 0 : sectionPages.size());
		}

		if (!counts.equals(this.pageCounts)) {
			this.pageCounts = counts;
			rebuild(listed);
		}

		return this.pages;
	}

	@Override
	public List<Index> getIndices() {
		return null;
	}

	/** The sections this index lists. By default every section after this one in its book. */
	protected List<GuidebookSection> getListedSections() {
		List<GuidebookSection> listed = new ArrayList<>();

		if (this.book == null) {
			return listed;
		}

		boolean afterThis = false;

		for (GuidebookSection section : this.book.getSections()) {
			if (section == this) {
				afterThis = true;
			} else if (afterThis) {
				listed.add(section);
			}
		}

		return listed;
	}

	private void rebuild(List<GuidebookSection> listed) {
		List<GuidebookPage> allPages = new ArrayList<>();
		List<Object> lines = new ArrayList<>();
		int totalLines = 0;

		for (GuidebookSection section : listed) {
			List<GuidebookPage> sectionPages = section.getPages();
			if (sectionPages == null || sectionPages.isEmpty()) {
				continue;
			}

			lines.add(section);
			lines.add(allPages.size() + 1);
			totalLines++;

			List<Index> indices = section.getIndices();
			if (indices != null) {
				for (Index index : indices) {
					lines.add(index);
					lines.add(allPages.size() + sectionPages.indexOf(index.getPage()) + 1);
					totalLines++;
				}
			}

			allPages.addAll(sectionPages);
		}

		int totalPages = totalLines / LINES_PER_PAGE + 1;
		lines.add(0, 1 - totalPages);
		lines.add(0, this);

		this.pages.clear();
		for (int i = 0; i < totalPages; i++) {
			this.pages.add(new GuidebookPageIndex(this, lines, i * LINES_PER_PAGE));
		}
	}
}
