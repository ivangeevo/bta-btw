package ivangeevo.btx.block.entity;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicCompanionCube;
import ivangeevo.btx.block.blocks.BlockLogicMillStone;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.sound.BTXSoundTypes;
import ivangeevo.btx.util.utils.InventoryUtils;
import ivangeevo.btx.util.utils.MiscUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class TileEntityMillStone extends TileEntity implements Container {
	private static final int CONTAINER_SIZE = 3;
	private static final int STACK_SIZE_LIMIT = 64;
	private static final double MAX_PLAYER_INTERACTION_DIST = 64.0;
	private static final int TIME_TO_GRIND = 200;
	private static final int GRIND_SOUND_CHANCE = 30;
	private static final int IDLE_SOUND_CHANCE = 60;
	public static final int CRANK_PROGRESS = 20;

	private final Random random = new Random();

	private ItemStack[] contents = new ItemStack[3];
	private int grindCounter = 0;

	@Override
	public @NotNull String getNameTranslationKey() {
		return "container.millstone.name";
	}

	@Override
	public int getContainerSize() {
		return CONTAINER_SIZE;
	}

	@Override
	public @Nullable ItemStack getItem(int slot) {
		return this.contents[slot];
	}

	@Override
	public @Nullable ItemStack removeItem(int slot, int amount) {
		return InventoryUtils.decrementStackSize(this, slot, amount);
	}

	@Override
	public void setItem(int slot, @Nullable ItemStack itemStack) {
		this.contents[slot] = itemStack;
		if (itemStack != null && itemStack.stackSize > this.getMaxStackSize()) {
			itemStack.stackSize = this.getMaxStackSize();
		}
		this.setChanged();
	}

	@Override
	public int getMaxStackSize() {
		return STACK_SIZE_LIMIT;
	}

	@Override
	public void readAdditionalData(@NotNull CompoundTag compoundTag) {
		ListTag nbtTagList = compoundTag.getList("Items");
		this.contents = new ItemStack[this.getContainerSize()];

		for (int i = 0; i < nbtTagList.getValue().size(); i++) {
			CompoundTag compoundTag1 = (CompoundTag) nbtTagList.tagAt(i);
			byte byte0 = compoundTag1.getByte("Slot");
			if (byte0 >= 0 && byte0 < this.contents.length) {
				this.contents[byte0] = ItemStack.readItemStackFromNbt(compoundTag1);
			}
		}

		if (compoundTag.containsKey("GrindCounter")) {
			this.grindCounter = compoundTag.getInteger("GrindCounter");
		}
	}

	@Override
	public void writeAdditionalData(@NotNull CompoundTag compoundTag) {
		ListTag nbtTagList = new ListTag();

		for (int i = 0; i < this.contents.length; i++) {
			if (this.contents[i] != null) {
				CompoundTag compoundTag1 = new CompoundTag();
				compoundTag1.putByte("Slot", (byte) i);
				this.contents[i].writeToNBT(compoundTag1);
				nbtTagList.addTag(compoundTag1);
			}
		}

		compoundTag.put("Items", nbtTagList);
		compoundTag.putInt("GrindCounter", this.grindCounter);
	}

	public boolean stillValid(@NotNull Player player) {
		if (this.worldObj != null && this.worldObj.getTileEntity(this.tilePos.x, this.tilePos.y, this.tilePos.z) == this) {
			return player.distanceToSqr((double)this.tilePos.x + (double)0.5F, (double)this.tilePos.y + (double)0.5F, (double)this.tilePos.z + (double)0.5F) <= MAX_PLAYER_INTERACTION_DIST;
		} else {
			return false;
		}
	}

	@Override
	public void sort() {}

	protected boolean isPowered() {
		if (worldObj == null) return false;

		Block<?> block = this.worldObj.getBlockType(tilePos);

		return block == BTXBlocks.MILLSTONE
			&& block.getLogic() instanceof BlockLogicMillStone logic
			&& logic.isActive(worldObj, tilePos);
	}

	@Override
	public void tick() {
		if (this.worldObj == null || this.worldObj.isClientSide) {
			return;
		}

		if (this.findGrindableSlot() < 0) {
			this.grindCounter = 0;

			if (this.isPowered() && this.worldObj.rand.nextInt(IDLE_SOUND_CHANCE) == 0) {
				SoundUtils.playAtTileCenter(this.worldObj, this.tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.MILLSTONE_IDLE,
					1.0F, 0.5F + this.worldObj.rand.nextFloat() * 0.1F);
			}
			return;
		}

		if (this.isPowered()) {
			if (this.worldObj.rand.nextInt(GRIND_SOUND_CHANCE) == 0) {
				SoundUtils.playAtTileCenter(this.worldObj, this.tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.MILLSTONE_GRIND,
					1.0F, 0.75F + this.worldObj.rand.nextFloat() * 0.1F);
			}

			if (this.isWholeCompanionCubeNextToBeProcessed() && this.worldObj.rand.nextInt(10) == 0) {
				BlockLogicCompanionCube.playHurt(this.worldObj, this.tilePos, 2.0F);
			} else if (this.isNetherrackNextToBeProcessed() && this.worldObj.rand.nextInt(10) == 0) {
				SoundUtils.playAtTileCenter(this.worldObj, this.tilePos, SoundCategory.ENTITY_SOUNDS, BTXSoundTypes.MILLSTONE_NETHERRACK,
					0.25F, this.worldObj.rand.nextFloat() * 0.4F + 0.8F);
			}

			this.advanceGrind(1);
		}
	}

	public boolean crank() {
		if (this.findGrindableSlot() < 0) {
			return false;
		}
		this.advanceGrind(CRANK_PROGRESS);
		return true;
	}

	private int findGrindableSlot() {
		for (int i = 0; i < this.contents.length; ++i) {
			if (canGrind(this.contents[i])) {
				return i;
			}
		}
		return -1;
	}

	public static boolean canGrind(@Nullable ItemStack stack) {
		if (stack == null) return false;

		return BTXRecipes.MILLSTONE.findRecipe(stack) != null;
	}

	private void advanceGrind(int amount) {
		int slot = this.findGrindableSlot();
		if (slot < 0) {
			this.grindCounter = 0;
			return;
		}

		this.grindCounter += amount;
		if (this.grindCounter >= TIME_TO_GRIND) {
			this.grindCounter = 0;
			this.finishGrind(slot);
			this.setChanged();
		}
	}

	private void finishGrind(int slot) {
		List<ItemStack> outputs = BTXRecipes.MILLSTONE.findOutputs(this.contents[slot]);
		if (outputs != null) {
			if (this.contents[slot].getItem() == BTXBlocks.COMPANION_CUBE.asItem()) {
				BlockLogicCompanionCube.playDeathEffect(this.worldObj, this.tilePos);
			}

			this.removeItem(slot, 1);

			for (ItemStack output : outputs) {
				this.ejectStack(output);
			}
		}
	}

	public boolean containsWholeCompanionCube() {
		for (ItemStack stack : this.contents) {
			if (stack != null && stack.getItem().equals(BTXBlocks.COMPANION_CUBE.asItem())) {
				return true;
			}
		}
		return false;
	}

	public boolean isWholeCompanionCubeNextToBeProcessed() {
		int slot = this.findGrindableSlot();
		return slot >= 0 && this.contents[slot].getItem().equals(BTXBlocks.COMPANION_CUBE.asItem());
	}

	public boolean isNetherrackNextToBeProcessed() {
		int slot = this.findGrindableSlot();
		return slot >= 0 && this.contents[slot].getItem().equals(Blocks.NETHERRACK.asItem());
	}

	@Override
	public void setChanged() {
		super.setChanged();

		if (this.worldObj != null && !this.worldObj.isClientSide && this.containsWholeCompanionCube()) {
			BlockLogicCompanionCube.playWhine(this.worldObj, this.tilePos);
		}
	}

	public int getGrindCounter() {
		return this.grindCounter;
	}

	public void setGrindCounter(int progress) {
		this.grindCounter = progress;
	}

	public boolean isGrinding() {
		return this.grindCounter > 0;
	}

	public int getGrindProgressScaled(int scale) {
		return this.grindCounter * scale / TIME_TO_GRIND;
	}

	private void ejectStack(ItemStack stack) {
		if (worldObj == null) return;

		TilePos targetPos = new TilePos(tilePos.x, tilePos.y, tilePos.z);
		int directionChance = this.worldObj.rand.nextInt(4);

		targetPos.add(Direction.fromId(2 + directionChance)); // random direction

		MiscUtils.ejectStackWithRandomOffset(worldObj, targetPos, stack);
	}

	@Override
	public void dropContents(World world, int x, int y, int z) {
		super.dropContents(world, x, y, z);
		if (world.getTileEntity(x, y, z) == null) {
			return;
		}

		for (int slot = 0; slot < this.getContainerSize(); ++slot) {
			ItemStack stack = this.getItem(slot);
			if (stack == null) {
				continue;
			}

			float ox = this.random.nextFloat() * 0.8F + 0.1F;
			float oy = this.random.nextFloat() * 0.8F + 0.1F;
			float oz = this.random.nextFloat() * 0.8F + 0.1F;

			while (stack.stackSize > 0) {
				int count = Math.min(this.random.nextInt(21) + 10, stack.stackSize);
				stack.stackSize -= count;

				EntityItem item = new EntityItem(world, x + ox, y + oy, z + oz,
					new ItemStack(stack.itemID, count, stack.getMetadata()));
				float spread = 0.05F;
				item.xd = (float) this.random.nextGaussian() * spread;
				item.yd = (float) this.random.nextGaussian() * spread + 0.2F;
				item.zd = (float) this.random.nextGaussian() * spread;
				world.entityJoinedWorld(item);
			}
		}
	}
}
