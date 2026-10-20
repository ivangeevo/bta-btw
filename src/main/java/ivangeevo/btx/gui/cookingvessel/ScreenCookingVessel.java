package ivangeevo.btx.gui.cookingvessel;

import ivangeevo.btx.block.entity.TileEntityCookingVessel;
import net.minecraft.client.gui.container.ScreenContainerAbstract;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.player.inventory.container.ContainerInventory;

public class ScreenCookingVessel extends ScreenContainerAbstract {
	private static final int GUI_HEIGHT = 193;
	private static final int FIRE_ICON_HEIGHT = 12;

	private final TileEntityCookingVessel vessel;
	private final String titleTranslationKey;

	public ScreenCookingVessel(ContainerInventory inventory, TileEntityCookingVessel vessel, String titleTranslationKey) {
		super(new MenuCookingVessel(inventory, vessel));

		this.ySize = GUI_HEIGHT;
		this.vessel = vessel;
		this.titleTranslationKey = titleTranslationKey;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f) {
		this.mc.textureManager.loadTexture("/assets/btx/textures/gui/container/cooking_vessel.png").bind();
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		int xPos = (width - xSize) / 2;
		int yPos = (height - ySize) / 2;

		this.drawTexturedModalRect(xPos, yPos, 0, 0, this.xSize, this.ySize);

		if (this.vessel.isCooking()) {
			int scaledIconHeight = this.vessel.getCookProgressScaled(FIRE_ICON_HEIGHT);

			this.drawTexturedModalRect(
				xPos + 81, yPos + 19 + FIRE_ICON_HEIGHT - scaledIconHeight,
				176, FIRE_ICON_HEIGHT - scaledIconHeight,
				14, scaledIconHeight + 2
			);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		I18n i18n = I18n.getInstance();

		this.drawStringNoShadow(this.fontRenderer, i18n.translateKey(titleTranslationKey), 66, 6, 4210752);
		this.drawStringNoShadow(this.fontRenderer, i18n.translateKey("gui.furnace.label.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
}
