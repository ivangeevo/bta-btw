package ivangeevo.btx.mixin.gui.screens;

import ivangeevo.btx.block.entity.TileEntityCauldron;
import ivangeevo.btx.block.entity.TileEntityCrucible;
import ivangeevo.btx.block.entity.TileEntityHopper;
import ivangeevo.btx.gui.hopper.ScreenHopper;
import ivangeevo.btx.block.entity.TileEntityMillStone;
import ivangeevo.btx.gui.BTXScreens;
import ivangeevo.btx.gui.cookingvessel.ScreenCookingVessel;
import ivangeevo.btx.gui.millstone.ScreenMillStone;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(PlayerLocal.class)
public abstract class PlayerLocalMixinBTXScreens extends Player implements BTXScreens {
    @Shadow protected Minecraft mc;

	protected PlayerLocalMixinBTXScreens(World world) {
		super(world);
	}

	@Override
    public void btx$displayMillStoneScreen(TileEntityMillStone tileEntity) {
        this.mc.displayScreen(new ScreenMillStone(this.inventory, tileEntity));
    }

	@Override
	public void btx$displayCrucibleScreen(TileEntityCrucible tileEntity) {
		this.mc.displayScreen(new ScreenCookingVessel(this.inventory, tileEntity, "btx.gui.crucible.title"));
	}

	@Override
	public void btx$displayCauldronScreen(TileEntityCauldron tileEntity) {
		this.mc.displayScreen(new ScreenCookingVessel(this.inventory, tileEntity, "btx.gui.cauldron.title"));
	}

	@Override
	public void btx$displayHopperScreen(TileEntityHopper tileEntity) {
		this.mc.displayScreen(new ScreenHopper(this.inventory, tileEntity));
	}
}
