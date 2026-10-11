package ivangeevo.btx.gui.machine;

import ivangeevo.btx.block.entity.TileEntityMillStone;
import net.minecraft.client.gui.container.ScreenContainerAbstract;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.player.inventory.container.ContainerInventory;

public class ScreenMillStone extends ScreenContainerAbstract {
	private static final int GUI_HEIGHT = 193;
	private static final int GEAR_ICON_HEIGHT = 12;

	private TileEntityMillStone millStone;

	public ScreenMillStone(ContainerInventory inventory, TileEntityMillStone tileEntityMillStone) {
		super(new MenuMillStone( inventory, tileEntityMillStone));
		this.ySize = GUI_HEIGHT;
		this.millStone = tileEntityMillStone;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f) {
		this.mc.textureManager.loadTexture("/assets/btx/textures/gui/container/millstone.png").bind();
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int xPos = (width - xSize) / 2;
		int yPos = (height - ySize) / 2;
		this.drawTexturedModalRect(xPos, yPos, 0, 0, this.xSize, this.ySize);

		if (this.millStone.isGrinding()) {
			int scaledIconHeight = this.millStone.getGrindProgressScaled(GEAR_ICON_HEIGHT);
			this.drawTexturedModalRect(xPos + 80, yPos + 18 + GEAR_ICON_HEIGHT - scaledIconHeight, 176, GEAR_ICON_HEIGHT - scaledIconHeight, 14, scaledIconHeight + 2);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		I18n i18n = I18n.getInstance();
		this.drawStringNoShadow(this.fontRenderer, i18n.translateKey("btx.gui.millstone.title"), 66, 6, 4210752);
		this.drawStringNoShadow(this.fontRenderer, i18n.translateKey("gui.furnace.label.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
}
