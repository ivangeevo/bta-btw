package ivangeevo.btx.mixin.gui.screens;

import ivangeevo.btx.block.entity.TileEntityCauldron;
import ivangeevo.btx.block.entity.TileEntityCrucible;
import ivangeevo.btx.block.entity.TileEntityHopper;
import ivangeevo.btx.block.entity.TileEntityMillStone;
import ivangeevo.btx.gui.BTXScreens;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public abstract class PlayerMixinBTXScreens implements BTXScreens {
	@Override
	public void btx$displayMillStoneScreen(TileEntityMillStone tileEntity) {}

	@Override
	public void btx$displayCrucibleScreen(TileEntityCrucible tileEntity) {}

	@Override
	public void btx$displayCauldronScreen(TileEntityCauldron tileEntity) {}

	@Override
	public void btx$displayHopperScreen(TileEntityHopper tileEntity) {}

	@Override
	public void btx$displaySoulforgeScreen(TilePosc tilePos) {}
}
