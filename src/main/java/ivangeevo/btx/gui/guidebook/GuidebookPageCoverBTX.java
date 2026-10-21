package ivangeevo.btx.gui.guidebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.core.lang.I18n;

@Environment(EnvType.CLIENT)
public class GuidebookPageCoverBTX extends GuidebookPage {
	private static final String COVER_TEXTURE = "/assets/btx/textures/gui/container/guidebook/cover.png";
	private static final int TEXT_X = 84;
	private static final int TITLE_Y = 50;
	private static final int SUBTITLE_Y = 62;

	public GuidebookPageCoverBTX(GuidebookSection section) {
		super(section);
	}

	@Override
	public boolean drawHeaders() {
		return false;
	}

	@Override
	protected void renderBackground(TextureManager re, int x, int y) {
		re.bindTexture(re.loadTexture(COVER_TEXTURE));
		this.drawTexturedModalRect(x, y, 0, 0, this.width, this.height);
	}

	@Override
	protected void renderForeground(TextureManager re, FontRenderer sr, int x, int y, int mouseX, int mouseY, float partialTicks) {
		I18n i18n = I18n.getInstance();
		drawEmbossed(sr, i18n.translateKey("btx.guidebook.cover.title"), x + TEXT_X, y + TITLE_Y);
		drawEmbossed(sr, i18n.translateKey("btx.guidebook.cover.subtitle"), x + TEXT_X, y + SUBTITLE_Y);
	}

	private void drawEmbossed(FontRenderer sr, String text, int x, int y) {
		int color = this.getSection().getForegroundColor();
		this.drawStringCenteredNoShadow(sr, text, x, y + 1, (color & 0xFCFCFC) >> 2);
		this.drawStringCenteredNoShadow(sr, text, x, y, color);
	}
}