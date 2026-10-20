package ivangeevo.btx.gui.hopper;

import ivangeevo.btx.block.entity.TileEntityHopper;
import net.minecraft.client.gui.container.ScreenContainerAbstract;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.player.inventory.container.ContainerInventory;

public class ScreenHopper extends ScreenContainerAbstract {
	private static final int GUI_HEIGHT = 193;
	private static final int MACHINE_ICON_SIZE = 14;

	private final TileEntityHopper hopper;

	public ScreenHopper(ContainerInventory inventory, TileEntityHopper hopper) {
		super(new MenuHopper(inventory, hopper));
		this.ySize = GUI_HEIGHT;
		this.hopper = hopper;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f) {
		this.mc.textureManager.loadTexture("/assets/btx/textures/gui/container/hopper.png").bind();
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int xPos = (width - xSize) / 2;
		int yPos = (height - ySize) / 2;
		this.drawTexturedModalRect(xPos, yPos, 0, 0, this.xSize, this.ySize);

		if (this.hopper.isEjecting()) {
			this.drawTexturedModalRect(xPos + 80, yPos + 18, 176, 0, MACHINE_ICON_SIZE, MACHINE_ICON_SIZE);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		I18n i18n = I18n.getInstance();
		this.drawStringNoShadow(this.fontRenderer, i18n.translateKey("btx.gui.hopper.title"), 70, 6, 4210752);
		this.drawStringNoShadow(this.fontRenderer, i18n.translateKey("gui.furnace.label.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
}
