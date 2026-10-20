package ivangeevo.btx.block.entity;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicHopper;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.util.utils.InventoryUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicChest;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntityDispatcher;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.monster.MobGhast;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.entity.vehicle.EntityMinecart;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.packet.Packet;
import net.minecraft.core.net.packet.PacketTileEntityData;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;

import java.util.List;

public class TileEntityHopper extends TileEntity implements Container {
	public static final int STORAGE_SLOTS = 18;
	public static final int FILTER_SLOT = 18;
	private static final int INVENTORY_SIZE = STORAGE_SLOTS + 1;
	private static final int STACK_SIZE_LIMIT = 64;
	private static final double MAX_PLAYER_INTERACTION_DIST = 64.0;

	private static final int STACK_SIZE_TO_EJECT = 8;
	private static final int TIME_TO_EJECT = 3;

	private static final int MAX_CONTAINED_SOUL_COUNT = 7;

	private ItemStack[] contents = new ItemStack[INVENTORY_SIZE];

	private int ejectCounter = 0;
	private int containedSoulCount = 0;

	public boolean outputBlocked = false;

	/** Used by MenuHopper to send the power state to the client. 0 for off, 1 for on. */
	public int mechanicalPowerIndicator = 0;

	/** Synced to clients for rendering. */
	public int filterItemId = 0;
	public short storageSlotsOccupied = 0;

	private int lastMatcherLevel = -1;

	@Override
	public @NotNull String getNameTranslationKey() {
		return "container.hopper.name";
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
	public void setChanged() {
		super.setChanged();

		if (worldObj == null || worldObj.isClientSide) {
			return;
		}

		outputBlocked = false; // the change in inventory may allow the hopper to eject again

		if (validateInventoryStateVariables()) {
			worldObj.markBlockNeedsUpdate(tilePos);
		}

		if (getBlockLogic() instanceof BlockLogicHopper hopper) {
			hopper.setHasFilter(worldObj, tilePos, filterItemId > 0);
		}

		int matcherLevel = getMatcherLevel();

		if (matcherLevel != lastMatcherLevel) {
			lastMatcherLevel = matcherLevel;
			updateNeighboringMatchers();
		}
	}

	@Override
	public void readAdditionalData(@NotNull CompoundTag compoundTag) {
		// description packet (see getDescriptionPacket): only the render state
		if (!compoundTag.containsKey("Items") && compoundTag.containsKey("s")) {
			storageSlotsOccupied = compoundTag.getShort("s");
			filterItemId = compoundTag.getInteger("f");
			return;
		}

		ListTag nbtTagList = compoundTag.getList("Items");

		contents = new ItemStack[getContainerSize()];

		for (int i = 0; i < nbtTagList.getValue().size(); i++) {
			CompoundTag slotTag = (CompoundTag) nbtTagList.tagAt(i);

			int slot = slotTag.getByte("Slot") & 0xff;

			if (slot < contents.length) {
				contents[slot] = ItemStack.readItemStackFromNbt(slotTag);
			}
		}

		if (compoundTag.containsKey("grindCounter")) {
			ejectCounter = compoundTag.getInteger("grindCounter");
		}

		if (compoundTag.containsKey("iHoppeContainedSoulCount")) {
			containedSoulCount = compoundTag.getInteger("iHoppeContainedSoulCount");
		}

		validateInventoryStateVariables();
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
		compoundTag.putInt("grindCounter", ejectCounter);
		compoundTag.putInt("iHoppeContainedSoulCount", containedSoulCount);
	}

	@Override
	public Packet getDescriptionPacket() {
		CompoundTag tag = new CompoundTag();

		tag.putString("id", TileEntityDispatcher.getIDFromClass(getClass()).toString());
		tag.putInt("x", tilePos.x);
		tag.putInt("y", tilePos.y);
		tag.putInt("z", tilePos.z);
		tag.putShort("s", storageSlotsOccupied);
		tag.putInt("f", filterItemId);

		PacketTileEntityData packet = new PacketTileEntityData(); // the no-arg constructor flags it as chunk data
		packet.tag = tag;

		return packet;
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

		if (!(getBlockLogic() instanceof BlockLogicHopper hopper)) {
			return;
		}

		if (hopper.isBlockOn(worldObj, tilePos)) {
			mechanicalPowerIndicator = 1;

			containedSoulCount = 0;

			if (!outputBlocked) {
				ejectCounter++;

				if (ejectCounter >= TIME_TO_EJECT) {
					attemptToEjectStackFromInv();

					ejectCounter = 0;
				}
			} else {
				ejectCounter = 0;
			}
		} else {
			mechanicalPowerIndicator = 0;

			ejectCounter = 0;

			if (isSoulFilter(getFilterStack())) {
				if (containedSoulCount > MAX_CONTAINED_SOUL_COUNT) {
					hopperSoulOverload();
				}
			} else {
				containedSoulCount = 0;
			}
		}
	}

	public @Nullable ItemStack getFilterStack() {
		return contents[FILTER_SLOT];
	}

	public boolean canCurrentFilterProcessItem(ItemStack stack) {
		return BTXRecipes.HOPPER_FILTER.canFilterProcess(getFilterStack(), stack);
	}

	public boolean isEjecting() {
		return mechanicalPowerIndicator > 0;
	}

	public void incrementContainedSoulCount(int numSouls) {
		containedSoulCount += numSouls;
	}

	public void resetContainedSoulCount() {
		containedSoulCount = 0;
	}

	public int getMatcherLevel() {
		int occupied = 0;
		float fullness = 0.0F;

		for (int slot = 0; slot < STORAGE_SLOTS; slot++) {
			ItemStack stack = contents[slot];

			if (stack != null) {
				fullness += (float) stack.stackSize / (float) Math.min(getMaxStackSize(), stack.getMaxStackSize());
				occupied++;
			}
		}

		fullness /= (float) STORAGE_SLOTS;

		return (int) Math.floor(fullness * 14.0F) + (occupied > 0 ? 1 : 0);
	}

	private static boolean isSoulFilter(@Nullable ItemStack filter) {
		return filter != null && filter.itemID == Blocks.SOULSAND.id();
	}

	private @Nullable Object getBlockLogic() {
		if (worldObj == null) return null;

		Block<?> block = worldObj.getBlockType(tilePos);

		return block == BTXBlocks.HOPPER ? block.getLogic() : null;
	}

	private boolean validateInventoryStateVariables() {
		boolean stateChanged = false;

		ItemStack filterStack = getFilterStack();
		int currentFilterId = filterStack != null && filterStack.stackSize > 0 ? filterStack.itemID : 0;

		if (currentFilterId != filterItemId) {
			filterItemId = currentFilterId;

			stateChanged = true;
		}

		short numSlotsOccupied = (short) InventoryUtils.getNumOccupiedStacksInRange(this, 0, STORAGE_SLOTS - 1);

		if (numSlotsOccupied != storageSlotsOccupied) {
			storageSlotsOccupied = numSlotsOccupied;

			stateChanged = true;
		}

		return stateChanged;
	}

	private void attemptToEjectStackFromInv() {
		int stackIndex = InventoryUtils.getRandomOccupiedStackInRange(this, worldObj.rand, 0, STORAGE_SLOTS - 1);

		if (stackIndex < 0) {
			return;
		}

		ItemStack invStack = contents[stackIndex];

		int ejectStackSize = Math.min(STACK_SIZE_TO_EJECT, invStack.stackSize);

		// a full copy, so item data (enchantments, names, bucket state...) survives the trip
		ItemStack ejectStack = invStack.copy();
		ejectStack.stackSize = ejectStackSize;

		TilePos targetPos = new TilePos(tilePos.x, tilePos.y - 1, tilePos.z);

		boolean ejectIntoWorld = false;

		if (worldObj.isAirBlock(targetPos)) {
			ejectIntoWorld = true;
		} else {
			Block<?> targetBlock = worldObj.getBlockType(targetPos);

			if (targetBlock == null || targetBlock.hasTag(BlockTags.PLACE_OVERWRITES) || !targetBlock.getMaterial().isSolid()) {
				ejectIntoWorld = true;
			} else {
				TileEntity targetTileEntity = worldObj.getTileEntity(targetPos);

				if (targetTileEntity instanceof Container) {
					Container targetInventory = (Container) targetTileEntity;

					int minSlotToAddTo = 0;
					int maxSlotToAddTo = targetInventory.getContainerSize() - 1;
					boolean canProcessStack = true;

					if (targetBlock == Blocks.FURNACE_STONE_IDLE || targetBlock == Blocks.FURNACE_STONE_ACTIVE
						|| targetBlock == Blocks.FURNACE_BLAST_IDLE || targetBlock == Blocks.FURNACE_BLAST_ACTIVE) {
						maxSlotToAddTo = 0;
					} else if (targetTileEntity instanceof TileEntityHopper targetHopper) {
						maxSlotToAddTo = STORAGE_SLOTS - 1;

						if (targetHopper.filterItemId > 0) {
							canProcessStack = false;
						}
					} else if (targetBlock.getLogic() instanceof BlockLogicChest) {
						// the combined inventory of a double chest
						targetInventory = BlockLogicChest.getInventory(worldObj, targetPos);
						maxSlotToAddTo = targetInventory.getContainerSize() - 1;
					}

					if (canProcessStack) {
						int numItemsStored;

						if (InventoryUtils.addItemStackToInventoryInSlotRange(targetInventory, ejectStack, minSlotToAddTo, maxSlotToAddTo)) {
							numItemsStored = ejectStackSize;
						} else {
							numItemsStored = ejectStackSize - ejectStack.stackSize;
						}

						if (numItemsStored > 0) {
							removeItem(stackIndex, numItemsStored);

							playCollectionPop();
						}
					} else {
						outputBlocked = true;
					}
				} else {
					outputBlocked = true;
				}
			}
		}

		if (ejectIntoWorld) {
			// test for a storage cart below the hopper
			List<EntityMinecart> carts = worldObj.getEntitiesWithinAABB(EntityMinecart.class,
				new AABBd(tilePos.x + 0.4, tilePos.y - 0.5, tilePos.z + 0.4, tilePos.x + 0.6, tilePos.y, tilePos.z + 0.6));

			for (EntityMinecart cart : carts) {
				if (cart.getType() == EntityMinecart.CHEST_CART && isCartAlignedWithNozzle(cart)) {
					int numItemsStored;

					if (InventoryUtils.addItemStackToInventory(cart, ejectStack)) {
						numItemsStored = ejectStackSize;
					} else {
						numItemsStored = ejectStackSize - ejectStack.stackSize;
					}

					if (numItemsStored > 0) {
						removeItem(stackIndex, numItemsStored);

						playCollectionPop();
					}

					ejectIntoWorld = false;

					break;
				}
			}
		}

		if (ejectIntoWorld) {
			ejectStack(ejectStack);

			removeItem(stackIndex, ejectStackSize);
		}
	}

	private boolean isCartAlignedWithNozzle(EntityMinecart cart) {
		double x = tilePos.x;
		double y = tilePos.y;
		double z = tilePos.z;

		return cart.bb.intersectsAABB(new AABBd(x, y - 0.5, z, x + 0.25, y, z + 1.0))
			&& cart.bb.intersectsAABB(new AABBd(x + 0.75, y - 0.5, z, x + 1.0, y, z + 1.0))
			&& cart.bb.intersectsAABB(new AABBd(x, y - 0.5, z, x + 1.0, y, z + 0.25))
			&& cart.bb.intersectsAABB(new AABBd(x, y - 0.5, z + 0.75, x + 1.0, y, z + 1.0));
	}

	private void ejectStack(ItemStack stack) {
		float xOffset = worldObj.rand.nextFloat() * 0.1F + 0.45F;
		float yOffset = -0.35F;
		float zOffset = worldObj.rand.nextFloat() * 0.1F + 0.45F;

		EntityItem entityItem = new EntityItem(worldObj, tilePos.x + xOffset, tilePos.y + yOffset, tilePos.z + zOffset, stack);

		entityItem.xd = 0.0;
		entityItem.yd = -0.01;
		entityItem.zd = 0.0;

		entityItem.pickupDelay = 10;

		worldObj.entityJoinedWorld(entityItem);
	}

	private void playCollectionPop() {
		SoundUtils.playAtTileCenter(worldObj, tilePos, SoundCategory.WORLD_SOUNDS, "random.pop", 0.25F,
			((worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
	}

	private void hopperSoulOverload() {
		if (spawnGhast()) {
			SoundUtils.playAtTileCenter(worldObj, tilePos, SoundCategory.ENTITY_SOUNDS, "mob.ghast.scream", 1.0F,
				worldObj.rand.nextFloat() * 0.4F + 0.8F);
		}

		if (getBlockLogic() instanceof BlockLogicHopper hopper) {
			hopper.breakHopper(worldObj, tilePos);
		}
	}

	private boolean spawnGhast() {
		MobGhast ghast = new MobGhast(worldObj);

		for (int i = 0; i < 200; i++) {
			double xPos = tilePos.x + (worldObj.rand.nextDouble() - worldObj.rand.nextDouble()) * 10.0;
			double yPos = tilePos.y + worldObj.rand.nextInt(21) - 10;
			double zPos = tilePos.z + (worldObj.rand.nextDouble() - worldObj.rand.nextDouble()) * 10.0;

			ghast.moveTo(xPos, yPos, zPos, worldObj.rand.nextFloat() * 360.0F, 0.0F);

			if (worldObj.areBlocksLoaded(ghast.bb)
				&& worldObj.getCubes(ghast, ghast.bb).isEmpty()
				&& !worldObj.getIsAnyLiquid(ghast.bb)) {
				worldObj.entityJoinedWorld(ghast);

				return true;
			}
		}

		return false;
	}
}
