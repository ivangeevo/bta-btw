package ivangeevo.btx.impl.guidebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.renderer.GLRenderer;

@Environment(EnvType.CLIENT)
public final class GuidebookTintImpl {
	public static final String RIBBON_TEMPLATE = "btx:gui/guidebook/ribbon_template";
	public static final String RIBBON_TEMPLATE_ACTIVE = "btx:gui/guidebook/ribbon_template_active";
	public static final String COVER_TEMPLATE = "/assets/btx/textures/gui/container/guidebook/cover_template.png";
	public static final String COVER_TRIM_TEMPLATE = "/assets/btx/textures/gui/container/guidebook/cover_trim_template.png";

	private static final float TEMPLATE_BASE = 0xDD / 255.0F;

	private GuidebookTintImpl() {}

	public static void applyTemplateTint(int rgb) {
		GLRenderer.setColor4f(channel(rgb, 16), channel(rgb, 8), channel(rgb, 0), 1.0F);
	}

	public static void resetTint() {
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private static float channel(int rgb, int shift) {
		return Math.min(1.0F, ((rgb >> shift) & 0xFF) / 255.0F / TEMPLATE_BASE);
	}
}
