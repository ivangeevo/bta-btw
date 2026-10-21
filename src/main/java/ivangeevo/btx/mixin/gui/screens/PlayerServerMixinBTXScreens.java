package ivangeevo.btx.mixin.gui.screens;

import ivangeevo.btx.block.entity.TileEntityCauldron;
import ivangeevo.btx.block.entity.TileEntityCrucible;
import ivangeevo.btx.block.entity.TileEntityHopper;
import ivangeevo.btx.gui.hopper.MenuHopper;
import ivangeevo.btx.block.entity.TileEntityMillStone;
import ivangeevo.btx.config.BTXConfig;
import ivangeevo.btx.gui.BTXScreens;
import ivangeevo.btx.gui.cookingvessel.MenuCookingVessel;
import ivangeevo.btx.gui.millstone.MenuMillStone;
import ivangeevo.btx.gui.soulforge.MenuSoulforge;
import net.minecraft.core.world.pos.TilePosc;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.net.packet.PacketContainerOpen;
import net.minecraft.server.entity.player.PlayerServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.SERVER)
@Mixin(PlayerServer.class)
public abstract class PlayerServerMixinBTXScreens implements BTXScreens {
    @Shadow private int currentWindowId;
    @Shadow protected abstract void getNextWindowId();

    @Override
    public void btx$displayMillStoneScreen(@NotNull TileEntityMillStone tileEntity) {
        this.getNextWindowId();
        PlayerServer playerServer = (PlayerServer) (Object) this;
        playerServer.playerNetServerHandler.sendPacket(new PacketContainerOpen(this.currentWindowId, BTXConfig.MILLSTONE_SCREEN_ID, tileEntity.getNameTranslationKey(), tileEntity.getContainerSize()));
        playerServer.containerMenu.onCraftGuiClosed(playerServer);
        playerServer.containerMenu = new MenuMillStone(playerServer.inventory, tileEntity);
        playerServer.containerMenu.containerId = this.currentWindowId;
        playerServer.containerMenu.addSlotListener(playerServer);
    }

    @Override
    public void btx$displayCrucibleScreen(@NotNull TileEntityCrucible tileEntity) {
        this.getNextWindowId();
        PlayerServer playerServer = (PlayerServer) (Object) this;
        playerServer.playerNetServerHandler.sendPacket(new PacketContainerOpen(this.currentWindowId, BTXConfig.CRUCIBLE_SCREEN_ID, tileEntity.getNameTranslationKey(), tileEntity.getContainerSize()));
        playerServer.containerMenu.onCraftGuiClosed(playerServer);
        playerServer.containerMenu = new MenuCookingVessel(playerServer.inventory, tileEntity);
        playerServer.containerMenu.containerId = this.currentWindowId;
        playerServer.containerMenu.addSlotListener(playerServer);
    }

    @Override
    public void btx$displayCauldronScreen(@NotNull TileEntityCauldron tileEntity) {
        this.getNextWindowId();
        PlayerServer playerServer = (PlayerServer) (Object) this;
        playerServer.playerNetServerHandler.sendPacket(new PacketContainerOpen(this.currentWindowId, BTXConfig.CAULDRON_SCREEN_ID, tileEntity.getNameTranslationKey(), tileEntity.getContainerSize()));
        playerServer.containerMenu.onCraftGuiClosed(playerServer);
        playerServer.containerMenu = new MenuCookingVessel(playerServer.inventory, tileEntity);
        playerServer.containerMenu.containerId = this.currentWindowId;
        playerServer.containerMenu.addSlotListener(playerServer);
    }

    @Override
    public void btx$displayHopperScreen(@NotNull TileEntityHopper tileEntity) {
        this.getNextWindowId();
        PlayerServer playerServer = (PlayerServer) (Object) this;
        playerServer.playerNetServerHandler.sendPacket(new PacketContainerOpen(this.currentWindowId, BTXConfig.HOPPER_SCREEN_ID, tileEntity.getNameTranslationKey(), tileEntity.getContainerSize()));
        playerServer.containerMenu.onCraftGuiClosed(playerServer);
        playerServer.containerMenu = new MenuHopper(playerServer.inventory, tileEntity);
        playerServer.containerMenu.containerId = this.currentWindowId;
        playerServer.containerMenu.addSlotListener(playerServer);
    }

    @Override
    public void btx$displaySoulforgeScreen(@NotNull TilePosc tilePos) {
        this.getNextWindowId();
        PlayerServer playerServer = (PlayerServer) (Object) this;
        playerServer.playerNetServerHandler.sendPacket(new PacketContainerOpen(this.currentWindowId, BTXConfig.SOULFORGE_SCREEN_ID, "btx.gui.soulforge.title", 16));
        playerServer.containerMenu.onCraftGuiClosed(playerServer);
        playerServer.containerMenu = new MenuSoulforge(playerServer.inventory, playerServer.world, tilePos);
        playerServer.containerMenu.containerId = this.currentWindowId;
        playerServer.containerMenu.addSlotListener(playerServer);
    }
}
