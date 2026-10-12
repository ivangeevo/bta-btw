package ivangeevo.btx.mixin.gui.screens;

import ivangeevo.btx.block.entity.TileEntityMillStone;
import ivangeevo.btx.config.BTXConfig;
import ivangeevo.btx.gui.BTXScreens;
import ivangeevo.btx.gui.millstone.MenuMillStone;
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
}
