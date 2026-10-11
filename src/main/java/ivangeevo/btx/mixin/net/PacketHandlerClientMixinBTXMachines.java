package ivangeevo.btx.mixin.net;

import ivangeevo.btx.block.entity.TileEntityMillStone;
import ivangeevo.btx.config.BTXConfig;
import ivangeevo.btx.gui.BTXScreens;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.client.net.handler.PacketHandlerClient;
import net.minecraft.core.net.packet.PacketContainerOpen;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PacketHandlerClient.class)
public abstract class PacketHandlerClientMixinBTXMachines {
    @Final @Shadow private Minecraft mc;

	@Inject(method = "handleContainerOpen(Lnet/minecraft/core/net/packet/PacketContainerOpen;)V", at = @At("TAIL"))
    private void handleBTXMachines(@NotNull PacketContainerOpen packetContainerOpen, CallbackInfo ci) {
        PlayerLocal playerLocal = this.mc.thePlayer;
        BTXScreens playerScreen = (BTXScreens) playerLocal;
        if (packetContainerOpen.inventoryType == BTXConfig.MILLSTONE_SCREEN_ID) {
            TileEntityMillStone machine = new TileEntityMillStone();
            playerScreen.btx$displayMillStoneScreen(machine);
            playerLocal.containerMenu.containerId = packetContainerOpen.windowId;
        }
    }
}
