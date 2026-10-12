package ivangeevo.btx.mixin.net;

import ivangeevo.btx.networking.ClientDataChangeHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.net.handler.PacketHandlerClient;
import net.minecraft.client.world.WorldClientMP;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.net.packet.PacketChunkBlocksUpdate;
import net.minecraft.core.world.pos.TilePos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PacketHandlerClient.class)
public abstract class PacketHandlerClientMixin {

	@Shadow
	private WorldClientMP worldClientMP;

	@Unique private Block<?>[] btx$oldBlocks;
	@Unique private int[] btx$oldData;

	// Capture the state BEFORE vanilla applies the batch.
	//@Inject(method = "handleChunkBlocksUpdate", at = @At("HEAD"))
	private void btx$captureChunkBlocks(PacketChunkBlocksUpdate packet, CallbackInfo ci) {
		int xBase = packet.xChunk * 16;
		int zBase = packet.zChunk * 16;

		btx$oldBlocks = new Block<?>[packet.size];
		btx$oldData = new int[packet.size];

		TilePos pos = new TilePos();
		for (int k = 0; k < packet.size; k++) {
			int coord = packet.coordinateArray[k];
			pos.set((coord & 15) + xBase, coord >> 8 & 0xFF, (coord >> 4 & 15) + zBase);
			btx$oldBlocks[k] = worldClientMP.getBlockType(pos);
			btx$oldData[k] = worldClientMP.getBlockData(pos);
		}
	}

	// Dispatch AFTER vanilla applied everything, so section rebuilds see the new state.
	//@Inject(method = "handleChunkBlocksUpdate", at = @At("RETURN"))
	private void btx$dispatchChunkBlocks(PacketChunkBlocksUpdate packet, CallbackInfo ci) {
		if (btx$oldBlocks == null) return;

		int xBase = packet.xChunk * 16;
		int zBase = packet.zChunk * 16;

		for (int k = 0; k < packet.size; k++) {
			int coord = packet.coordinateArray[k];
			int type = packet.typeArray[k] & 16383;
			int data = packet.metadataArray[k];
			TilePos pos = new TilePos((coord & 15) + xBase, coord >> 8 & 0xFF, (coord >> 4 & 15) + zBase);

			ClientDataChangeHelper.notifyChange(
				worldClientMP, pos,
				btx$oldBlocks[k], btx$oldData[k],
				Blocks.getBlock(type), data);
		}

		btx$oldBlocks = null;
		btx$oldData = null;
	}
}
