package ivangeevo.btx.block.entity;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import ivangeevo.btx.util.utils.MiscUtils;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityVase extends TileEntity {
	private @Nullable ItemStack contents;

	public boolean isEmpty() {
		return this.contents == null;
	}

	public boolean tryInsert(@NotNull ItemStack stack) {
		if (this.contents != null || stack.stackSize <= 0) return false;

		this.contents = stack.splitStack(1);
		this.setChanged();
		return true;
	}

	public @Nullable ItemStack takeContents() {
		ItemStack taken = this.contents;
		this.contents = null;
		this.setChanged();
		return taken;
	}

	public void setContents(@Nullable ItemStack stack) {
		this.contents = stack;
		this.setChanged();
	}

	public @Nullable ItemStack getContents() {
		return this.contents;
	}

	@Override
	public void dropContents(World world, int x, int y, int z) {
		super.dropContents(world, x, y, z);
		MiscUtils.ejectStackWithRandomOffset(world, new TilePos(x, y, z), this.takeContents());
	}

	@Override
	public void readAdditionalData(@NotNull CompoundTag tag) {
		this.contents = null;
		ListTag items = tag.getList("Items");

		for (int i = 0; i < items.getValue().size(); i++) {
			CompoundTag itemTag = (CompoundTag) items.tagAt(i);
			if ((itemTag.getByte("Slot") & 0xff) == 0) {
				this.contents = ItemStack.readItemStackFromNbt(itemTag);
			}
		}
	}

	@Override
	public void writeAdditionalData(@NotNull CompoundTag tag) {
		ListTag items = new ListTag();

		if (this.contents != null) {
			CompoundTag itemTag = new CompoundTag();
			itemTag.putByte("Slot", (byte) 0);
			this.contents.writeToNBT(itemTag);
			items.addTag(itemTag);
		}

		tag.put("Items", items);
	}
}
