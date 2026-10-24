package ivangeevo.btx.gui.blockdispenser;

import ivangeevo.btx.block.entity.TileEntityBlockDispenser;
import net.minecraft.client.gui.container.ScreenContainerAbstract;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.player.inventory.container.ContainerInventory;

public class ScreenBlockDispenser extends ScreenContainerAbstract {
	private static final int GUI_HEIGHT = 182;
	private static final int SELECTION_ICON_SIZE = 20;

	private final TileEntityBlockDispenser dispenser;

	public ScreenBlockDispenser(ContainerInventory inventory, TileEntityBlockDispenser dispenser) {
		super(new MenuBlockDispenser(inventory, dispenser));
		this.ySize = GUI_HEIGHT;
		this.dispenser = dispenser;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f) {
		this.mc.textureManager.loadTexture("/assets/btx/textures/gui/container/block_dispenser.png").bind();
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int xPos = (width - xSize) / 2;
		int yPos = (height - ySize) / 2;
		this.drawTexturedModalRect(xPos, yPos, 0, 0, this.xSize, this.ySize);

		int nextSlot = this.dispenser.getNextSlotIndexToDispense();
		int xOffset = (nextSlot % MenuBlockDispenser.SLOT_COLUMNS) * 18;
		int yOffset = (nextSlot / MenuBlockDispenser.SLOT_COLUMNS) * 18;
		this.drawTexturedModalRect(xPos + 51 + xOffset, yPos + 15 + yOffset, 176, 0, SELECTION_ICON_SIZE, SELECTION_ICON_SIZE);
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		I18n i18n = I18n.getInstance();
		this.drawStringNoShadow(this.fontRenderer, i18n.translateKey("btx.gui.block_dispenser.title"), 48, 6, 4210752);
		this.drawStringNoShadow(this.fontRenderer, i18n.translateKey("gui.furnace.label.inventory"), 8, this.ySize - 94 + 2, 4210752);
	}
}