package ivangeevo.btx.gui.guidebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.index.GuidebookPageIndex;
import net.minecraft.core.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class GuidebookSectionIndexBTX extends GuidebookSection {
	private static final int LINES_PER_PAGE = 14;
	private List<GuidebookPage> indexPages = null;

	public GuidebookSectionIndexBTX(String translationKey, ItemStack tabIcon, int bgColor, int fgColor) {
		super(translationKey, tabIcon, bgColor, fgColor);
	}

	@Override
	public List<GuidebookPage> getPages() {
		if (this.indexPages == null) {
			initPages();
		}
		return this.indexPages;
	}

	@Override
	public List<Index> getIndices() {
		return null;
	}

	private void initPages() {
		List<GuidebookPage> allPages = new ArrayList<>();
		List<GuidebookSection> listed = new ArrayList<>();
		for (GuidebookSection section : BTXGuidebook.getSections(BTXGuidebook.Book.BTX)) {
			if (section == this || section instanceof GuidebookSectionCoverBTX) {
				continue;
			}
			List<GuidebookPage> pages = section.getPages();
			if (pages == null || pages.isEmpty()) {
				continue;
			}
			allPages.addAll(pages);
			listed.add(section);
		}

		List<Object> lines = new ArrayList<>();
		int totalLines = 0;
		for (GuidebookSection section : listed) {
			lines.add(section);
			lines.add(allPages.indexOf(section.getPages().get(0)) + 1);
			totalLines++;
			if (section.getIndices() != null) {
				for (Index index : section.getIndices()) {
					lines.add(index);
					lines.add(allPages.indexOf(index.getPage()) + 1);
					totalLines++;
				}
			}
		}

		int totalPages = totalLines / LINES_PER_PAGE + 1;
		lines.add(0, 1 - totalPages);
		lines.add(0, this);

		this.indexPages = new ArrayList<>();
		for (int i = 0; i < totalPages; i++) {
			this.indexPages.add(new GuidebookPageIndex(this, lines, i * LINES_PER_PAGE));
		}
	}
}