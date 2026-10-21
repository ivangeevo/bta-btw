package ivangeevo.btx.gui.soulforge;

import net.minecraft.client.gui.container.ScreenContainerAbstract;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public class ScreenSoulforge extends ScreenContainerAbstract {
	private static final int GUI_HEIGHT = 184;

	public ScreenSoulforge(ContainerInventory inventory, World world, TilePosc tilePos) {
		super(new MenuSoulforge(inventory, world, tilePos));
		this.ySize = GUI_HEIGHT;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick) {
		this.mc.textureManager.loadTexture("/assets/btx/textures/gui/container/soulforge.png").bind();

		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		int xPos = (this.width - this.xSize) / 2;
		int yPos = (this.height - this.ySize) / 2;

		this.drawTexturedModalRect(xPos, yPos, 0, 0, this.xSize, this.ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		I18n i18n = I18n.getInstance();

		this.drawStringNoShadow(this.fontRenderer, i18n.translateKey("btx.gui.soulforge.title"), 22, 6, 4210752);
		this.drawStringNoShadow(this.fontRenderer, i18n.translateKey("gui.furnace.label.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	public void removed() {
		super.removed();
		this.inventorySlots.onCraftGuiClosed(this.mc.thePlayer);
	}
}
