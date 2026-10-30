package ivangeevo.btx.impl.guidebook;

import ivangeevo.btx.api.guidebook.GuidebookBook;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.TooltipElement;
import net.minecraft.client.gui.guidebook.GuidebookPageManager;
import net.minecraft.client.gui.guidebook.ScreenGuidebook;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.lang.I18n;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class GuidebookRibbonsImpl {
	private static final int RIBBON_WIDTH = 9;
	private static final int RIBBON_HEIGHT = 22;
	private static final int RIBBON_ACTIVE_HEIGHT = 28;
	private static final int RIBBON_HANG = 14;
	private static final int RIBBON_ACTIVE_HANG = 20;
	private static final int RIBBON_SPACING = 4;
	private static final int RIBBON_EDGE_INSET = 3;
	private static final int BOOK_HEIGHT = 220;
	private static final int PAGE_WIDTH = 158;

	private static @Nullable TooltipElement tooltip;

	private GuidebookRibbonsImpl() {}

	public static void render(ScreenGuidebook screen) {
		List<GuidebookBook> books = GuidebookRegistryImpl.getBooks();
		if (books.size() < 2) {
			return;
		}

		for (int i = 0; i < books.size(); i++) {
			GuidebookBook book = books.get(i);
			boolean active = book == GuidebookNavigationImpl.getActive();
			String sprite = active ? book.getActiveRibbonSprite() : book.getRibbonSprite();
			Integer color = book.getRibbonColor();

			if (color != null) {
				GuidebookTintImpl.applyTemplateTint(color);
			} else {
				GuidebookTintImpl.resetTint();
			}

			screen.drawGuiIcon(ribbonX(screen, i, books.size()), ribbonY(screen, active), RIBBON_WIDTH, ribbonHeight(active), TextureRegistry.getTexture(sprite));
		}

		GuidebookTintImpl.resetTint();
	}

	public static void renderTooltip(ScreenGuidebook screen, int mouseX, int mouseY) {
		GuidebookBook hovered = getHovered(screen, mouseX, mouseY);

		if (hovered == null) {
			return;
		}

		if (tooltip == null) {
			tooltip = new TooltipElement(Minecraft.getMinecraft());
		}

		tooltip.render(I18n.getInstance().translateKey(hovered.getTranslationKey()), mouseX, mouseY, 8, -8);
	}

	public static boolean mouseClicked(ScreenGuidebook screen, int mouseX, int mouseY, int button) {
		if (button != 0) {
			return false;
		}

		GuidebookBook hovered = getHovered(screen, mouseX, mouseY);

		if (hovered == null) {
			return false;
		}

		GuidebookNavigationImpl.switchTo(hovered);

		return true;
	}

	private static @Nullable GuidebookBook getHovered(ScreenGuidebook screen, int mouseX, int mouseY) {
		List<GuidebookBook> books = GuidebookRegistryImpl.getBooks();

		if (books.size() < 2) {
			return null;
		}

		for (int i = 0; i < books.size(); i++) {
			GuidebookBook book = books.get(i);
			boolean active = book == GuidebookNavigationImpl.getActive();
			int x = ribbonX(screen, i, books.size());
			int y = ribbonY(screen, active);
			if (mouseX >= x && mouseX < x + RIBBON_WIDTH && mouseY >= y && mouseY < y + ribbonHeight(active)) {
				return book;
			}
		}

		return null;
	}

	private static int ribbonX(ScreenGuidebook screen, int index, int count) {
		GuidebookPageManager manager = ScreenGuidebook.getPageManager();
		int rowWidth = count * RIBBON_WIDTH + (count - 1) * RIBBON_SPACING;

		int start;

		if (manager.getLeftPage() == null) {
			start = screen.width / 2 - PAGE_WIDTH / 2 + RIBBON_EDGE_INSET;
		} else if (manager.getRightPage() == null) {
			start = screen.width / 2 + PAGE_WIDTH / 2 - RIBBON_EDGE_INSET - rowWidth;
		} else {
			start = screen.width / 2 - rowWidth / 2;
		}

		return start + index * (RIBBON_WIDTH + RIBBON_SPACING);
	}

	private static int ribbonY(ScreenGuidebook screen, boolean active) {
		int bookTop = screen.height / 2 - BOOK_HEIGHT / 2;
		int hang = active ? RIBBON_ACTIVE_HANG : RIBBON_HANG;
		return bookTop + hang - ribbonHeight(active);
	}

	private static int ribbonHeight(boolean active) {
		return active ? RIBBON_ACTIVE_HEIGHT : RIBBON_HEIGHT;
	}
}
