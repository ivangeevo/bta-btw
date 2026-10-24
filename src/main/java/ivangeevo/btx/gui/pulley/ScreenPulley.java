package ivangeevo.btx.gui.pulley;

import ivangeevo.btx.block.entity.TileEntityPulley;
import net.minecraft.client.gui.container.ScreenContainerAbstract;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.player.inventory.container.ContainerInventory;

public class ScreenPulley extends ScreenContainerAbstract {
	private static final int GUI_HEIGHT = 174;
	private static final int MACHINE_ICON_SIZE = 14;

	private final TileEntityPulley pulley;

	public ScreenPulley(ContainerInventory inventory, TileEntityPulley pulley) {
		super(new MenuPulley(inventory, pulley));
		this.ySize = GUI_HEIGHT;
		this.pulley = pulley;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f) {
		this.mc.textureManager.loadTexture("/assets/btx/textures/gui/container/pulley.png").bind();
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int xPos = (width - xSize) / 2;
		int yPos = (height - ySize) / 2;
		this.drawTexturedModalRect(xPos, yPos, 0, 0, this.xSize, this.ySize);

		if (this.pulley.mechanicalPowerIndicator > 0) {
			this.drawTexturedModalRect(xPos + 80, yPos + 18, 176, 0, MACHINE_ICON_SIZE, MACHINE_ICON_SIZE);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		I18n i18n = I18n.getInstance();
		this.drawStringNoShadow(this.fontRenderer, i18n.translateKey("btx.gui.pulley.title"), 75, 6, 4210752);
		this.drawStringNoShadow(this.fontRenderer, i18n.translateKey("gui.furnace.label.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
}