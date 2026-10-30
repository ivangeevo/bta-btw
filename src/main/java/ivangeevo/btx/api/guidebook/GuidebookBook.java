package ivangeevo.btx.api.guidebook;

import ivangeevo.btx.impl.guidebook.GuidebookTintImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.GuidebookSections;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A book shown in the guidebook screen, with its own sections and a ribbon to switch to it.
 * <p>
 * Create one with {@link #builder(String)} and register it with {@link Guidebooks#register(GuidebookBook)}.
 */
@Environment(EnvType.CLIENT)
public final class GuidebookBook {
	private final String id;
	private final String translationKey;
	private final String ribbonSprite;
	private final String activeRibbonSprite;
	private final boolean customRibbon;
	private @Nullable Integer ribbonColor;
	private final @Nullable String switchSound;
	private final @Nullable Consumer<GuidebookCoverSection.Builder> cover;
	private final @Nullable Consumer<List<GuidebookSection>> sectionProvider;
	private final @Nullable Supplier<List<GuidebookSection>> liveSections;
	private final List<GuidebookSection> sections = new ArrayList<>();
	private boolean built = false;

	private GuidebookBook(Builder builder) {
		this.id = builder.id;
		this.translationKey = builder.translationKey;
		this.customRibbon = builder.ribbonSprite != null;
		this.ribbonSprite = this.customRibbon ? builder.ribbonSprite : GuidebookTintImpl.RIBBON_TEMPLATE;
		this.activeRibbonSprite = this.customRibbon ? builder.activeRibbonSprite : GuidebookTintImpl.RIBBON_TEMPLATE_ACTIVE;
		this.ribbonColor = builder.ribbonColor;
		this.switchSound = builder.switchSound;
		this.cover = builder.cover;
		this.sectionProvider = builder.sectionProvider;
		this.liveSections = builder.liveSections;
	}

	/**
	 * Starts a new book.
	 *
	 * @param id a unique id, e.g. {@code "mymod:manual"}
	 */
	public static Builder builder(@NotNull String id) {
		return new Builder(id);
	}

	/** The unique id this book was built with. */
	public String getId() {
		return this.id;
	}

	/** The translation key of the book's name, shown when hovering its ribbon. */
	public String getTranslationKey() {
		return this.translationKey;
	}

	/**
	 * The ribbon tint as {@code 0xRRGGBB}, or {@code null} when the ribbon is drawn as-is.
	 * Only the grayscale template ribbon is tinted; custom ribbon textures never are.
	 * Without an explicit color it is the cover color, or else the first tab's background color.
	 */
	public @Nullable Integer getRibbonColor() {
		if (this.customRibbon) {
			return null;
		}

		if (this.ribbonColor != null) {
			return this.ribbonColor;
		}

		List<GuidebookSection> sections = getSections();
		return sections.isEmpty() ? null : sections.get(0).getBackgroundColor() & 0xFFFFFF;
	}

	/** The ribbon sprite drawn while another book is open. */
	public String getRibbonSprite() {
		return this.ribbonSprite;
	}

	/** The ribbon sprite drawn while this book is open. */
	public String getActiveRibbonSprite() {
		return this.activeRibbonSprite;
	}

	/** The sound played when switching to this book, or {@code null} for the page-turn sound. */
	public @Nullable String getSwitchSound() {
		return this.switchSound;
	}

	/** The book's sections in tab order. Empty until the guidebook is first initialized. */
	public List<GuidebookSection> getSections() {
		if (this.liveSections != null) {
			return this.liveSections.get();
		}
		return Collections.unmodifiableList(this.sections);
	}

	/** Whether the given section belongs to this book. */
	public boolean contains(GuidebookSection section) {
		return getSections().contains(section);
	}

	@ApiStatus.Internal
	public void initSections() {
		if (this.built) {
			return;
		}

		this.built = true;

		if (this.cover != null) {
			GuidebookCoverSection.Builder coverBuilder = GuidebookCoverSection.builder();
			this.cover.accept(coverBuilder);

			GuidebookCoverSection section = coverBuilder.build();
			this.sections.add(section);

			if (this.ribbonColor == null && section.usesTemplate()) {
				this.ribbonColor = section.getColor();
			}
		}

		if (this.sectionProvider != null) {
			this.sectionProvider.accept(this.sections);
		}
	}

	@Override
	public String toString() {
		return "GuidebookBook[" + this.id + "]";
	}

	/** Builds a {@link GuidebookBook}. It needs a cover or sections; everything else is optional. */
	@Environment(EnvType.CLIENT)
	public static final class Builder {
		private final String id;
		private String translationKey;
		private @Nullable String ribbonSprite;
		private @Nullable String activeRibbonSprite;
		private @Nullable Integer ribbonColor;
		private @Nullable String switchSound;
		private @Nullable Consumer<GuidebookCoverSection.Builder> cover;
		private @Nullable Consumer<List<GuidebookSection>> sectionProvider;
		private @Nullable Supplier<List<GuidebookSection>> liveSections;

		private Builder(String id) {
			this.id = Objects.requireNonNull(id, "id");
			this.translationKey = id;
		}

		/** The translation key of the book's name. Defaults to the id. */
		public Builder name(@NotNull String translationKey) {
			this.translationKey = Objects.requireNonNull(translationKey, "translationKey");
			return this;
		}

		/**
		 * Colors the default ribbon, as {@code 0xRRGGBB}.
		 * Without this the ribbon takes the cover color, or else the first tab's background color.
		 */
		public Builder ribbon(int rgb) {
			this.ribbonColor = rgb & 0xFFFFFF;
			this.ribbonSprite = null;
			this.activeRibbonSprite = null;
			return this;
		}

		/**
		 * Uses your own ribbon texture instead of the colored one, e.g. {@code "mymod:gui/guidebook/ribbon"}.
		 * The active sprite is the same id with {@code "_active"} appended. Ribbons are 9×22, and 9×28 when active.
		 */
		public Builder ribbon(@NotNull String sprite) {
			return ribbon(sprite, sprite + "_active");
		}

		/** Uses your own ribbon textures for the inactive and active state. */
		public Builder ribbon(@NotNull String sprite, @NotNull String activeSprite) {
			this.ribbonSprite = Objects.requireNonNull(sprite, "sprite");
			this.activeRibbonSprite = Objects.requireNonNull(activeSprite, "activeSprite");
			return this;
		}

		/** The sound played when switching to this book. Defaults to the page-turn sound. */
		public Builder switchSound(@Nullable String sound) {
			this.switchSound = sound;
			return this;
		}

		/**
		 * Gives the book a cover as its first tab, drawn from BTA's cover in your colors:
		 * <pre>{@code
		 * .cover(cover -> cover.color(0x306333).title("mymod.guidebook.title"))
		 * }</pre>
		 * The cover can also use your own texture or page, see {@link GuidebookCoverSection.Builder}.
		 */
		public Builder cover(@NotNull Consumer<GuidebookCoverSection.Builder> cover) {
			this.cover = Objects.requireNonNull(cover, "cover");
			return this;
		}

		/**
		 * Fills the book's sections in tab order, after the cover if there is one.
		 * <p>
		 * Runs once, after BTA's own sections exist, so they can be reused.
		 * Add {@link GuidebookSections#SEARCH} to give the book a search tab. A book shows at most 16 tabs.
		 */
		public Builder sections(@NotNull Consumer<List<GuidebookSection>> provider) {
			this.sectionProvider = Objects.requireNonNull(provider, "provider");
			return this;
		}

		/** Reads the sections from a live list each time instead of filling them once. */
		public Builder liveSections(@NotNull Supplier<List<GuidebookSection>> sections) {
			this.liveSections = Objects.requireNonNull(sections, "sections");
			return this;
		}

		/** Creates the book. Register it with {@link Guidebooks#register(GuidebookBook)}. */
		public GuidebookBook build() {
			if (this.sectionProvider == null && this.liveSections == null && this.cover == null) {
				throw new IllegalStateException("Guidebook " + this.id + " has no sections");
			}

			return new GuidebookBook(this);
		}
	}
}
