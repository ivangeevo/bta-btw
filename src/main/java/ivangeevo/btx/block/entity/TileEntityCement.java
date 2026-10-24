package ivangeevo.btx.block.entity;

import com.mojang.nbt.tags.CompoundTag;
import ivangeevo.btx.block.blocks.BlockLogicCement;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntityDispatcher;
import net.minecraft.core.net.packet.Packet;
import net.minecraft.core.net.packet.PacketTileEntityData;
import org.jetbrains.annotations.NotNull;

public class TileEntityCement extends TileEntity {
	private int dryTime = 0;
	private int spreadDist = 0;

	public int getDryTime() {
		return dryTime;
	}

	public void setDryTime(int dryTime) {
		this.dryTime = dryTime;
		markForClient();
	}

	public int getSpreadDist() {
		return spreadDist;
	}

	public void setSpreadDist(int spreadDist) {
		this.spreadDist = spreadDist;
		markForClient();
	}

	private void markForClient() {
		if (worldObj != null) {
			worldObj.markBlockNeedsUpdate(tilePos);
		}
	}

	@Override
	public void readAdditionalData(@NotNull CompoundTag compoundTag) {
		if (compoundTag.containsKey("d")) {
			dryTime = compoundTag.getShort("d");
			spreadDist = compoundTag.getShort("s");

			if (worldObj != null && worldObj.isClientSide) {
				worldObj.markBlockNeedsUpdate(tilePos);
			}

			return;
		}

		dryTime = compoundTag.containsKey("dryTime") ? compoundTag.getInteger("dryTime") : BlockLogicCement.TICKS_TO_DRY;
		spreadDist = compoundTag.containsKey("spreadDist") ? compoundTag.getInteger("spreadDist") : BlockLogicCement.MAX_SPREAD_DIST;
	}

	@Override
	public void writeAdditionalData(@NotNull CompoundTag compoundTag) {
		compoundTag.putInt("dryTime", dryTime);
		compoundTag.putInt("spreadDist", spreadDist);
	}

	@Override
	public Packet getDescriptionPacket() {
		CompoundTag tag = new CompoundTag();

		tag.putString("id", TileEntityDispatcher.getIDFromClass(getClass()).toString());
		tag.putInt("x", tilePos.x);
		tag.putInt("y", tilePos.y);
		tag.putInt("z", tilePos.z);
		tag.putShort("d", (short) dryTime);
		tag.putShort("s", (short) spreadDist);

		PacketTileEntityData packet = new PacketTileEntityData();
		packet.tag = tag;

		return packet;
	}
}
