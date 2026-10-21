package ivangeevo.btx.gui;

import ivangeevo.btx.block.entity.TileEntityCauldron;
import ivangeevo.btx.block.entity.TileEntityCrucible;
import ivangeevo.btx.block.entity.TileEntityHopper;
import ivangeevo.btx.block.entity.TileEntityMillStone;
import net.minecraft.core.world.pos.TilePosc;

public interface BTXScreens {
	void btx$displayMillStoneScreen(TileEntityMillStone tileEntity);

	void btx$displayCrucibleScreen(TileEntityCrucible tileEntity);

	void btx$displayCauldronScreen(TileEntityCauldron tileEntity);

	void btx$displayHopperScreen(TileEntityHopper tileEntity);

	void btx$displaySoulforgeScreen(TilePosc tilePos);
}
