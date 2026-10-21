package ivangeevo.btx.mixin.gui;

import ivangeevo.btx.gui.guidebook.BTXGuidebook;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.TooltipElement;
import net.minecraft.client.gui.guidebook.GuidebookPageManager;
import net.minecraft.client.gui.guidebook.ScreenGuidebook;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.lang.I18n;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ScreenGuidebook.class)
public abstract class ScreenGuidebookMixin extends Screen {
	@Unique private static final int RIBBON_WIDTH = 9;
	@Unique private static final int RIBBON_HEIGHT = 22;
	@Unique private static final int RIBBON_ACTIVE_HEIGHT = 28;
	@Unique private static final int RIBBON_HANG = 14;
	@Unique private static final int RIBBON_ACTIVE_HANG = 20;
	@Unique private static final int RIBBON_SPACING = 4;
	@Unique private static final int RIBBON_EDGE_INSET = 3;
	@Unique private static final int BOOK_HEIGHT = 220;
	@Unique private static final int PAGE_WIDTH = 158;
	@Unique private static final String ACTIVE_SUFFIX = "_active";

	@Unique private TooltipElement btx$ribbonTooltip;

	protected ScreenGuidebookMixin(Screen parent) {
		super(parent);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/guidebook/ScreenGuidebook;drawCurrentPageOverlays(IIIF)V"))
	private void drawBookRibbons(int mx, int my, float partialTicks, CallbackInfo ci) {
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		for (BTXGuidebook.Book book : BTXGuidebook.Book.values()) {
			boolean active = book == BTXGuidebook.getActive();
			String sprite = active ? book.ribbonSprite + ACTIVE_SUFFIX : book.ribbonSprite;
			this.drawGuiIcon(btx$ribbonX(book), btx$ribbonY(book), RIBBON_WIDTH, btx$ribbonHeight(book), TextureRegistry.getTexture(sprite));
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void drawRibbonTooltip(int mx, int my, float partialTicks, CallbackInfo ci) {
		BTXGuidebook.Book hovered = btx$getHoveredRibbon(mx, my);

		if (hovered == null) {
			return;
		}

		if (this.btx$ribbonTooltip == null) {
			this.btx$ribbonTooltip = new TooltipElement(this.mc);
		}

		this.btx$ribbonTooltip.render(I18n.getInstance().translateKey(hovered.translationKey), mx, my, 8, -8);
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void clickBookRibbon(int mx, int my, int buttonNum, CallbackInfo ci) {
		if (buttonNum != 0) {
			return;
		}

		BTXGuidebook.Book hovered = btx$getHoveredRibbon(mx, my);

		if (hovered != null) {
			BTXGuidebook.switchTo(hovered, (ScreenGuidebook) (Object) this);
			ci.cancel();
		}
	}

	@Unique
	private @Nullable BTXGuidebook.Book btx$getHoveredRibbon(int mx, int my) {
		for (BTXGuidebook.Book book : BTXGuidebook.Book.values()) {
			int x = btx$ribbonX(book);
			int y = btx$ribbonY(book);
			if (mx >= x && mx < x + RIBBON_WIDTH && my >= y && my < y + btx$ribbonHeight(book)) {
				return book;
			}
		}

		return null;
	}

	@Unique
	private int btx$ribbonX(BTXGuidebook.Book book) {
		GuidebookPageManager pageManager = ScreenGuidebook.getPageManager();

		int pairWidth = RIBBON_WIDTH * 2 + RIBBON_SPACING;
		int start;
		if (pageManager.getLeftPage() == null) {
			start = this.width / 2 - PAGE_WIDTH / 2 + RIBBON_EDGE_INSET;
		} else if (pageManager.getRightPage() == null) {
			start = this.width / 2 + PAGE_WIDTH / 2 - RIBBON_EDGE_INSET - pairWidth;
		} else {
			start = this.width / 2 - pairWidth / 2;
		}

		return start + book.ordinal() * (RIBBON_WIDTH + RIBBON_SPACING);
	}

	@Unique
	private int btx$ribbonY(BTXGuidebook.Book book) {
		int bookTop = this.height / 2 - BOOK_HEIGHT / 2;
		int hang = book == BTXGuidebook.getActive() ? RIBBON_ACTIVE_HANG : RIBBON_HANG;
		return bookTop + hang - btx$ribbonHeight(book);
	}

	@Unique
	private static int btx$ribbonHeight(BTXGuidebook.Book book) {
		return book == BTXGuidebook.getActive() ? RIBBON_ACTIVE_HEIGHT : RIBBON_HEIGHT;
	}
}
