package ivangeevo.btx.api.guidebook;

import ivangeevo.btx.impl.guidebook.GuidebookTintImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A book's cover tab.
 * <p>
 * By default it draws BTA's cover shape in your colors with a title, subtitle and footer:
 * <pre>{@code
 * GuidebookCoverSection.builder()
 *     .color(0x306333)
 *     .title("mymod.guidebook.title")
 *     .subtitle("mymod.guidebook.subtitle")
 *     .build();
 * }</pre>
 * Use {@link Builder#texture(String)} for your own cover image, or {@link Builder#page(Function)} for a fully custom page.
 */
@Environment(EnvType.CLIENT)
public class GuidebookCoverSection extends GuidebookSection {
	/** BTA's cover red. */
	public static final int DEFAULT_COLOR = 0x9C1103;
	/** BTA's cover gold. */
	public static final int DEFAULT_TRIM_COLOR = 0xD9C41C;

	private final int color;
	private final int trimColor;
	private final int textColor;
	private final @Nullable String texture;
	private final @Nullable String title;
	private final @Nullable String subtitle;
	private final @Nullable String footer;
	private final List<GuidebookPage> pages;

	protected GuidebookCoverSection(Builder builder) {
		super(builder.translationKey, builder.tabIcon != null ? builder.tabIcon : new ItemStack(Blocks.MOTION_SENSOR_ACTIVE),
			builder.tabBackground != null ? builder.tabBackground : builder.color,
			builder.tabForeground != null ? builder.tabForeground : builder.trimColor
		);
		this.color = builder.color;
		this.trimColor = builder.trimColor;
		this.textColor = builder.textColor != null ? builder.textColor : builder.trimColor;
		this.texture = builder.texture;
		this.title = builder.title;
		this.subtitle = builder.subtitle;
		this.footer = builder.footer;
		this.pages = List.of(builder.page != null ? builder.page.apply(this) : new CoverPage(this));
	}

	/** Starts a cover with BTA's colors. */
	public static Builder builder() {
		return new Builder();
	}

	/** The cover color. */
	public int getColor() {
		return this.color;
	}

	/** The color of the frame and corners. */
	public int getTrimColor() {
		return this.trimColor;
	}

	/** The color of the cover text. */
	public int getTextColor() {
		return this.textColor;
	}

	/** Whether the cover is drawn from the colored template rather than a custom texture or page. */
	public boolean usesTemplate() {
		return this.texture == null && this.pages.get(0) instanceof CoverPage;
	}

	@Override
	public List<GuidebookPage> getPages() {
		return this.pages;
	}

	@Override
	public List<Index> getIndices() {
		return null;
	}

	/** The default cover page: the template or texture, then the embossed title, subtitle and footer. */
	@Environment(EnvType.CLIENT)
	public static class CoverPage extends GuidebookPage {
		private static final int TEXT_X = 84;
		private static final int TITLE_Y = 50;
		private static final int SUBTITLE_Y = 62;
		private static final int FOOTER_Y = 150;

		public CoverPage(GuidebookSection section) {
			super(section);
		}

		@Override
		public boolean drawHeaders() {
			return false;
		}

		@Override
		protected void renderBackground(TextureManager manager, int x, int y) {
			GuidebookCoverSection cover = (GuidebookCoverSection) this.section;
			if (cover.texture != null) {
				GuidebookTintImpl.resetTint();
				manager.bindTexture(manager.loadTexture(cover.texture));
				this.drawTexturedModalRect(x, y, 0, 0, this.width, this.height);
				return;
			}

			GuidebookTintImpl.applyTemplateTint(cover.color);
			manager.bindTexture(manager.loadTexture(GuidebookTintImpl.COVER_TEMPLATE));
			this.drawTexturedModalRect(x, y, 0, 0, this.width, this.height);

			GuidebookTintImpl.applyTemplateTint(cover.trimColor);
			manager.bindTexture(manager.loadTexture(GuidebookTintImpl.COVER_TRIM_TEMPLATE));
			this.drawTexturedModalRect(x, y, 0, 0, this.width, this.height);

			GuidebookTintImpl.resetTint();
		}

		@Override
		protected void renderForeground(TextureManager manager, FontRenderer fontRenderer, int x, int y, int mouseX, int mouseY, float partialTicks) {
			GuidebookCoverSection cover = (GuidebookCoverSection) this.section;
			drawEmbossed(fontRenderer, cover.title, x + TEXT_X, y + TITLE_Y, cover.textColor);
			drawEmbossed(fontRenderer, cover.subtitle, x + TEXT_X, y + SUBTITLE_Y, cover.textColor);
			drawEmbossed(fontRenderer, cover.footer, x + TEXT_X, y + FOOTER_Y, cover.textColor);
		}

		/** Draws centered text with BTA's pressed-in shadow. The text is translated if it is a translation key. */
		protected void drawEmbossed(FontRenderer fontRenderer, @Nullable String text, int x, int y, int color) {
			if (text == null || text.isEmpty()) {
				return;
			}

			String translated = I18n.getInstance().translateKey(text);

			this.drawStringCenteredNoShadow(fontRenderer, translated, x, y + 1, (color & 0xFCFCFC) >> 2);
			this.drawStringCenteredNoShadow(fontRenderer, translated, x, y, color);
		}
	}

	/** Builds a {@link GuidebookCoverSection}. Every setting is optional. */
	@Environment(EnvType.CLIENT)
	public static class Builder {
		private String translationKey = "guidebook.section.cover";
		private int color = DEFAULT_COLOR;
		private int trimColor = DEFAULT_TRIM_COLOR;
		private @Nullable Integer textColor;
		private @Nullable String texture;
		private @Nullable String title;
		private @Nullable String subtitle;
		private @Nullable String footer;
		private @Nullable ItemStack tabIcon;
		private @Nullable Integer tabBackground;
		private @Nullable Integer tabForeground;
		private @Nullable Function<GuidebookSection, GuidebookPage> page;

		protected Builder() {
		}

		/** The cover color as {@code 0xRRGGBB}. Defaults to BTA's red. */
		public Builder color(int rgb) {
			this.color = rgb & 0xFFFFFF;
			return this;
		}

		/** The frame and corner color as {@code 0xRRGGBB}. Defaults to BTA's gold. */
		public Builder trimColor(int rgb) {
			this.trimColor = rgb & 0xFFFFFF;
			return this;
		}

		/** The text color as {@code 0xRRGGBB}. Defaults to the trim color. */
		public Builder textColor(int rgb) {
			this.textColor = rgb & 0xFFFFFF;
			return this;
		}

		/** The title near the top of the cover, as a translation key or plain text. */
		public Builder title(@Nullable String text) {
			this.title = text;
			return this;
		}

		/** A second line under the title, as a translation key or plain text. */
		public Builder subtitle(@Nullable String text) {
			this.subtitle = text;
			return this;
		}

		/** A line near the bottom of the cover, e.g. a version, as a translation key or plain text. */
		public Builder footer(@Nullable String text) {
			this.footer = text;
			return this;
		}

		/**
		 * Uses your own cover image instead of the colored template, e.g. {@code "/assets/mymod/textures/gui/cover.png"}.
		 * The cover is the top-left 158×220 of the texture, like BTA's. Text is still drawn on top.
		 */
		public Builder texture(@NotNull String path) {
			this.texture = Objects.requireNonNull(path, "path");
			return this;
		}

		/** Replaces the whole cover page with your own. The other settings then only affect the tab. */
		public Builder page(@NotNull Function<GuidebookSection, GuidebookPage> factory) {
			this.page = Objects.requireNonNull(factory, "factory");
			return this;
		}

		/** The translation key of the tab's name. Defaults to BTA's "Cover". */
		public Builder name(@NotNull String translationKey) {
			this.translationKey = Objects.requireNonNull(translationKey, "translationKey");
			return this;
		}

		/** The tab icon. Defaults to BTA's cover icon. */
		public Builder tabIcon(@NotNull ItemStack icon) {
			this.tabIcon = Objects.requireNonNull(icon, "icon");
			return this;
		}

		/** The tab colors as {@code 0xRRGGBB}. Default to the cover and trim colors. */
		public Builder tabColors(int background, int foreground) {
			this.tabBackground = background & 0xFFFFFF;
			this.tabForeground = foreground & 0xFFFFFF;
			return this;
		}

		/** Creates the cover section. */
		public GuidebookCoverSection build() {
			return new GuidebookCoverSection(this);
		}
	}
}
