package ivangeevo.btx.gui.guidebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.core.item.ItemStack;

import java.util.List;

@Environment(EnvType.CLIENT)
public class GuidebookSectionCoverBTX extends GuidebookSection {
	private final List<GuidebookPage> pages = List.of(new GuidebookPageCoverBTX(this));

	public GuidebookSectionCoverBTX(String translationKey, ItemStack tabIcon, int bgColor, int fgColor) {
		super(translationKey, tabIcon, bgColor, fgColor);
	}

	@Override
	public List<GuidebookPage> getPages() {
		return this.pages;
	}

	@Override
	public List<Index> getIndices() {
		return null;
	}
}