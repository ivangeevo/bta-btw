package ivangeevo.btx.impl.guidebook;

import ivangeevo.btx.api.guidebook.GuidebookBook;
import ivangeevo.btx.mixin.api.guidebook.GuidebookPageManagerAccessor;
import ivangeevo.btx.mixin.api.guidebook.PageNavigationStackAccessor;
import ivangeevo.btx.mixin.api.guidebook.ScreenGuidebookAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookPageManager;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.ScreenGuidebook;
import net.minecraft.client.gui.guidebook.SearchableGuidebookSection;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.MathHelper;

import java.util.List;
import java.util.Stack;

@Environment(EnvType.CLIENT)
public final class GuidebookNavigationImpl {
	private static GuidebookBook active = GuidebookRegistryImpl.VANILLA;

	private GuidebookNavigationImpl() {
	}

	public static GuidebookBook getActive() {
		return active;
	}

	public static void switchTo(GuidebookBook book) {
		if (book == active || !GuidebookRegistryImpl.getBooks().contains(book)) {
			return;
		}

		GuidebookPageManager manager = ScreenGuidebook.getPageManager();

		if (GuidebookPagesImpl.isLookupActive()) {
			int index = GuidebookPagesImpl.findFirstResult(manager.getPages(), book);
			if (index < 0) {
				return;
			}
			active = book;
			manager.setCurrentPage(index, true);
		} else {
			GuidebookPagesImpl.rememberBrowsePage(active, Math.max(0, getStackPage(manager)));
			active = book;
			manager.updatePages();
			resetHistory(manager, GuidebookPagesImpl.getBrowsePage(book));
		}

		refreshTabs();

		String sound = book.getSwitchSound() != null ? book.getSwitchSound() : "random.page";
		Minecraft.getMinecraft().sndManager.playSound(sound, SoundCategory.GUI_SOUNDS, 0.8F, 1.0F);
	}

	public static void onPageShown(GuidebookPageManager manager) {
		if (!GuidebookPagesImpl.isLookupActive()) {
			return;
		}

		GuidebookPage page = manager.getLeftPage() != null ? manager.getLeftPage() : manager.getRightPage();
		GuidebookBook book = GuidebookPagesImpl.getBook(page);

		if (book != null && book != active) {
			active = book;
			refreshTabs();
		}
	}

	public static int getSectionIndex(GuidebookPageManager manager, GuidebookSection section) {
		List<GuidebookPage> sectionPages;
		try {
			sectionPages = section instanceof SearchableGuidebookSection searchable && GuidebookPageManager.searchQuery != null
				? searchable.searchPages(GuidebookPageManager.searchQuery)
				: section.getPages();
		} catch (RuntimeException e) {
			sectionPages = null;
		}

		if (sectionPages != null) {
			for (GuidebookPage page : sectionPages) {
				int index = manager.getPageIndex(page);
				if (index >= 0) {
					return index;
				}
			}
		}

		return Math.max(0, getStackPage(manager));
	}

	public static int getStackPage(GuidebookPageManager manager) {
		Stack<Integer> stack = stackOf(manager).btx$getStack();

		if (stack.isEmpty()) {
			return 0;
		}

		return stack.get(MathHelper.clamp(stackOf(manager).btx$getStackPointer(), 0, stack.size() - 1));
	}

	public static void showPage(GuidebookPageManager manager, int index) {
		int spread = toSpread(manager, index);
		PageNavigationStackAccessor accessor = stackOf(manager);
		Stack<Integer> stack = accessor.btx$getStack();

		if (stack.isEmpty()) {
			stack.push(spread);
			accessor.btx$setStackPointer(0);
		} else {
			stack.set(MathHelper.clamp(accessor.btx$getStackPointer(), 0, stack.size() - 1), spread);
		}

		manager.setCurrentPage(spread, false);
	}

	public static void resetHistory(GuidebookPageManager manager, int index) {
		int spread = toSpread(manager, index);
		PageNavigationStackAccessor accessor = stackOf(manager);
		accessor.btx$getStack().clear();
		accessor.btx$getStack().push(spread);
		accessor.btx$setStackPointer(0);
		manager.setCurrentPage(spread, false);
	}

	private static int toSpread(GuidebookPageManager manager, int index) {
		int clamped = MathHelper.clamp(index, 0, Math.max(0, manager.getNumPages() - 1));
		return clamped % 2 == 0 ? clamped - 1 : clamped;
	}

	private static PageNavigationStackAccessor stackOf(GuidebookPageManager manager) {
		return (PageNavigationStackAccessor) ((GuidebookPageManagerAccessor) manager).btx$getStack();
	}

	private static void refreshTabs() {
		if (Minecraft.getMinecraft().currentScreen instanceof ScreenGuidebook screen) {
			((ScreenGuidebookAccessor) screen).btx$initTabList();
		}
	}
}
