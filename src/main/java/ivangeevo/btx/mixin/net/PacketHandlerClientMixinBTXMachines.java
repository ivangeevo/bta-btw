package ivangeevo.btx.mixin.net;

import ivangeevo.btx.block.entity.TileEntityCauldron;
import ivangeevo.btx.block.entity.TileEntityCrucible;
import ivangeevo.btx.block.entity.TileEntityHopper;
import ivangeevo.btx.block.entity.TileEntityMillStone;
import ivangeevo.btx.config.BTXConfig;
import ivangeevo.btx.gui.BTXScreens;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.client.net.handler.PacketHandlerClient;
import net.minecraft.core.net.packet.PacketContainerOpen;
import net.minecraft.core.world.pos.TilePos;
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
        } else if (packetContainerOpen.inventoryType == BTXConfig.CRUCIBLE_SCREEN_ID) {
            TileEntityCrucible machine = new TileEntityCrucible();
            playerScreen.btx$displayCrucibleScreen(machine);
            playerLocal.containerMenu.containerId = packetContainerOpen.windowId;
        } else if (packetContainerOpen.inventoryType == BTXConfig.CAULDRON_SCREEN_ID) {
            TileEntityCauldron machine = new TileEntityCauldron();
            playerScreen.btx$displayCauldronScreen(machine);
            playerLocal.containerMenu.containerId = packetContainerOpen.windowId;
        } else if (packetContainerOpen.inventoryType == BTXConfig.HOPPER_SCREEN_ID) {
            TileEntityHopper machine = new TileEntityHopper();
            playerScreen.btx$displayHopperScreen(machine);
            playerLocal.containerMenu.containerId = packetContainerOpen.windowId;
        } else if (packetContainerOpen.inventoryType == BTXConfig.SOULFORGE_SCREEN_ID) {
            playerScreen.btx$displaySoulforgeScreen(new TilePos((int) Math.floor(playerLocal.x), (int) Math.floor(playerLocal.y), (int) Math.floor(playerLocal.z)));
            playerLocal.containerMenu.containerId = packetContainerOpen.windowId;
        }
    }
}
