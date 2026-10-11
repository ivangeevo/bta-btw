package ivangeevo.btx.mixin.gui.screens;

import ivangeevo.btx.block.entity.TileEntityMillStone;
import ivangeevo.btx.gui.BTXScreens;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public abstract class PlayerMixinBTXScreens implements BTXScreens {
	@Override
	public void btx$displayMillStoneScreen(TileEntityMillStone tileEntity) {}
}
