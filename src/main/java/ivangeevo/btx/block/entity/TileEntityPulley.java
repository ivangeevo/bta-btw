package ivangeevo.btx.block.entity;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicAnchor;
import ivangeevo.btx.block.blocks.BlockLogicPulley;
import ivangeevo.btx.sound.BTXSoundTypes;
import ivangeevo.btx.util.utils.InventoryUtils;
import ivangeevo.btx.util.utils.MiscUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityPulley extends TileEntity implements Container {
	public static final int INVENTORY_SIZE = 4;
	public static final int TICKS_TO_UPDATE_ROPE_STATE = 20;
	private static final int STACK_SIZE_LIMIT = 64;
	private static final double MAX_PLAYER_INTERACTION_DIST = 64.0;

	private ItemStack[] contents = new ItemStack[INVENTORY_SIZE];
	private int updateRopeStateCounter = TICKS_TO_UPDATE_ROPE_STATE;
	private boolean hasAssociatedAnchorEntity = false;
	private boolean ropeJustAttachedToAnchor = false;
	public int mechanicalPowerIndicator = 0;

	@Override
	public @NotNull String getNameTranslationKey() {
		return "container.btx.pulley.name";
	}

	@Override
	public int getContainerSize() {
		return INVENTORY_SIZE;
	}

	@Override
	public @Nullable ItemStack getItem(int slot) {
		return contents[slot];
	}

	@Override
	public @Nullable ItemStack removeItem(int slot, int amount) {
		return InventoryUtils.decrementStackSize(this, slot, amount);
	}

	@Override
	public void setItem(int slot, @Nullable ItemStack itemStack) {
		contents[slot] = itemStack;

		if (itemStack != null && itemStack.stackSize > getMaxStackSize()) {
			itemStack.stackSize = getMaxStackSize();
		}

		setChanged();
	}

	@Override
	public void setChanged() {
		super.setChanged();

		if (worldObj != null) {
			updateNeighboringMatchers();
		}
	}

	@Override
	public int getMaxStackSize() {
		return STACK_SIZE_LIMIT;
	}

	@Override
	public void sort() {}

	@Override
	public boolean stillValid(@NotNull Player player) {
		if (worldObj == null || worldObj.getTileEntity(tilePos) != this) {
			return false;
		}

		return player.distanceToSqr(tilePos.x + 0.5, tilePos.y + 0.5, tilePos.z + 0.5) <= MAX_PLAYER_INTERACTION_DIST;
	}

	@Override
	public void readAdditionalData(@NotNull CompoundTag compoundTag) {
		ListTag nbtTagList = compoundTag.getList("Items");

		contents = new ItemStack[getContainerSize()];

		for (int i = 0; i < nbtTagList.getValue().size(); i++) {
			CompoundTag slotTag = (CompoundTag) nbtTagList.tagAt(i);

			int slot = slotTag.getByte("Slot") & 0xff;

			if (slot < contents.length) {
				contents[slot] = ItemStack.readItemStackFromNbt(slotTag);
			}
		}

		if (compoundTag.containsKey("iUpdateRopeStateCounter")) {
			updateRopeStateCounter = compoundTag.getInteger("iUpdateRopeStateCounter");
		}

		if (compoundTag.containsKey("m_bHasAssociatedAnchorEntity")) {
			hasAssociatedAnchorEntity = compoundTag.getBoolean("m_bHasAssociatedAnchorEntity");
		}
	}

	@Override
	public void writeAdditionalData(@NotNull CompoundTag compoundTag) {
		ListTag nbtTagList = new ListTag();

		for (int i = 0; i < contents.length; i++) {
			if (contents[i] != null) {
				CompoundTag slotTag = new CompoundTag();
				slotTag.putByte("Slot", (byte) i);

				contents[i].writeToNBT(slotTag);

				nbtTagList.addTag(slotTag);
			}
		}

		compoundTag.put("Items", nbtTagList);
		compoundTag.putInt("iUpdateRopeStateCounter", updateRopeStateCounter);
		compoundTag.putBoolean("m_bHasAssociatedAnchorEntity", hasAssociatedAnchorEntity);
	}

	@Override
	public void dropContents(World world, int x, int y, int z) {
		super.dropContents(world, x, y, z);

		InventoryUtils.ejectInventoryContents(world, new TilePos(x, y, z), this);
		InventoryUtils.clearInventoryContents(this);
	}

	@Override
	public void tick() {
		if (worldObj == null || worldObj.isClientSide) {
			return;
		}

		mechanicalPowerIndicator = isMechanicallyPowered() ? 1 : 0;

		updateRopeStateCounter--;

		if (updateRopeStateCounter <= 0) {
			if (!hasAssociatedAnchorEntity && !isRedstonePowered()) {
				if (isMechanicallyPowered()) {
					attemptToRetractRope();
				} else if (!attemptToDispenseRope() && ropeJustAttachedToAnchor) {
					ropeJustAttachedToAnchor = false;
					notifyAttachedAnchorOfEntityStateChange();
				}
			}

			updateRopeStateCounter = TICKS_TO_UPDATE_ROPE_STATE;
		}
	}

	//------------- Pulley state -------------//

	private boolean isMechanicallyPowered() {
		return BlockLogicPulley.isBlockOn(worldObj, tilePos);
	}

	private boolean isRedstonePowered() {
		return BlockLogicPulley.isRedstoneOn(worldObj, tilePos);
	}

	public boolean isRaising() {
		return !isRedstonePowered() && isMechanicallyPowered();
	}

	public boolean isLowering() {
		return !isRedstonePowered() && !isMechanicallyPowered()
			&& InventoryUtils.getFirstOccupiedStackOfItem(this, Items.ROPE.id) >= 0;
	}

	public int getUpdateRopeStateCounter() {
		return updateRopeStateCounter;
	}

	public void notifyPulleyEntityOfBlockStateChange() {
		updateRopeStateCounter = TICKS_TO_UPDATE_ROPE_STATE;
		ropeJustAttachedToAnchor = false;

		notifyAttachedAnchorOfEntityStateChange();
	}

	private void notifyAttachedAnchorOfEntityStateChange() {
		TilePos pos = tilePos.down(new TilePos());

		while (pos.y() >= 0) {
			if (BlockLogicAnchor.isLiftAnchor(worldObj, pos)) {
				if (BlockLogicAnchor.notifyAnchorBlockOfAttachedPulleyStateChange(worldObj, pos, this)) {
					hasAssociatedAnchorEntity = true;
				}

				return;
			}

			if (worldObj.getBlockType(pos) != Blocks.ROPE) {
				return;
			}

			pos = pos.down(new TilePos());
		}
	}

	//------------- Rope handling -------------//

	public boolean attemptToRetractRope() {
		TilePos pos = tilePos.down(new TilePos());

		if (worldObj.getBlockType(pos) != Blocks.ROPE) {
			return false;
		}

		TilePos below = pos.down(new TilePos());
		while (worldObj.getBlockType(below) == Blocks.ROPE) {
			pos = below;
			below = below.down(new TilePos());
		}

		addRopeToInventory();
		playRopeSound(pos);
		worldObj.setBlockTypeNotify(pos, Blocks.AIR);

		return true;
	}

	public boolean attemptToDispenseRope() {
		updateRopeStateCounter = TICKS_TO_UPDATE_ROPE_STATE;

		if (InventoryUtils.getFirstOccupiedStackOfItem(this, Items.ROPE.id) < 0) {
			return false;
		}

		TilePos pos = tilePos.down(new TilePos());

		while (pos.y() >= 0) {
			if (worldObj.canPlaceInsideBlock(pos)) {
				if (!worldObj.setBlockTypeDataNotify(pos, Blocks.ROPE, BlockLogicAnchor.MASK_ANCHORED_ROPE)) {
					return false;
				}

				playRopeSound(pos);
				removeRopeFromInventory();

				if (!hasAssociatedAnchorEntity && BlockLogicAnchor.isLiftAnchor(worldObj, pos.down(new TilePos()))) {
					ropeJustAttachedToAnchor = true;
				}

				return true;
			}

			if (worldObj.getBlockType(pos) != Blocks.ROPE) {
				return false;
			}

			pos = pos.down(new TilePos());
		}

		return false;
	}

	public void addRopeToInventory() {
		ItemStack ropeStack = new ItemStack(Items.ROPE);

		updateRopeStateCounter = TICKS_TO_UPDATE_ROPE_STATE;

		if (InventoryUtils.addItemStackToInventory(this, ropeStack)) {
			SoundUtils.playAtTileCenter(worldObj, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.ITEM_COLLECT, 0.05F,
				(worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.7F + 1.0F);
		} else {
			MiscUtils.ejectStackWithRandomOffset(worldObj, tilePos, ropeStack);
		}
	}

	public int getContainedRopeCount() {
		return InventoryUtils.countItemsInInventory(this, Items.ROPE.id, InventoryUtils.IGNORE_METADATA);
	}

	public void removeRopeFromInventory() {
		int slot = InventoryUtils.getFirstOccupiedStackOfItem(this, Items.ROPE.id);

		if (slot >= 0) {
			InventoryUtils.decrementStackSize(this, slot, 1);
		}
	}

	public void notifyOfLossOfAnchorEntity() {
		hasAssociatedAnchorEntity = false;
	}

	private void playRopeSound(TilePos pos) {
		SoundUtils.playAtTileCenter(worldObj, pos, SoundCategory.WORLD_SOUNDS, Blocks.ROPE.getSound().getStepSoundName(), 0.25F, 0.8F);
	}

	public boolean isPulleyBlock() {
		return worldObj != null && worldObj.getBlockType(tilePos) == BTXBlocks.PULLEY;
	}
}