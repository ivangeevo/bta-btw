package ivangeevo.btx.block.entity;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicCookingVessel;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.recipe.cooking.RecipeGroupBulk;
import ivangeevo.btx.util.utils.FacingUtils;
import ivangeevo.btx.util.utils.InventoryUtils;
import ivangeevo.btx.util.utils.MiscUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntityDispatcher;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Items;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.packet.Packet;
import net.minecraft.core.net.packet.PacketTileEntityData;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class TileEntityCookingVessel extends TileEntity implements Container {
	protected static final int INVENTORY_SIZE = 27;
	private static final int STACK_SIZE_LIMIT = 64;

	private static final int PRIMARY_FIRE_FACTOR = 5;
	private static final int SECONDARY_FIRE_FACTOR = 3;

	protected static final int STACK_SIZE_TO_DROP_WHEN_TIPPED = 8;

	private static final double MAX_PLAYER_INTERACTION_DIST = 64D;

	private static final int STOKED_TICKS_TO_COOLDOWN = 20;

	// the first number in this equation represents the minimum number of ticks to cook something (with max fire)
	private static final int TIME_TO_COOK = 150 * (PRIMARY_FIRE_FACTOR + (SECONDARY_FIRE_FACTOR * 8));

	// local vars

	protected ItemStack[] contents;
	protected int cookCounter;

	protected int stokedCooldownCounter;

	protected boolean containsValidIngredientsForState;
	private boolean forceValidateOnUpdate;

	protected int fireUnderType; // 0 = none, 1 = normal, 2 = stoked, -1 = requires validation

	public int scaledCookCounter; // used to communicate cook progress from server to client

	// state variable used to communicate basic inventory info to the client
	public short storageSlotsOccupied;

	public TileEntityCookingVessel() {
		contents = new ItemStack[INVENTORY_SIZE];

		cookCounter = 0;

		stokedCooldownCounter = 0;
		containsValidIngredientsForState = false;
		forceValidateOnUpdate = true;
		scaledCookCounter = 0;
		fireUnderType = 0;

		storageSlotsOccupied = 0;
	}

	@Override
	public Packet getDescriptionPacket() {
		CompoundTag tag = new CompoundTag();

		tag.putString("id", TileEntityDispatcher.getIDFromClass(getClass()).toString());
		tag.putInt("x", tilePos.x);
		tag.putInt("y", tilePos.y);
		tag.putInt("z", tilePos.z);

		tag.putShort("s", storageSlotsOccupied);

		PacketTileEntityData packet = new PacketTileEntityData(); // the no-arg constructor flags it as chunk data
		packet.tag = tag;

		return packet;
	}

	@Override
	public void readAdditionalData(@NotNull CompoundTag compoundTag) {
		if (!compoundTag.containsKey("Items") && compoundTag.containsKey("s")) {
			storageSlotsOccupied = compoundTag.getShort("s");
			return;
		}

		ListTag nbtTagList = compoundTag.getList("Items");

		contents = new ItemStack[getContainerSize()];

		for (int i = 0; i < nbtTagList.getValue().size(); i++) {
			CompoundTag compoundTag1 = (CompoundTag) nbtTagList.tagAt(i);

			int j = compoundTag1.getByte("Slot") & 0xff;

			if (j >= 0 && j < contents.length) {
				contents[j] = ItemStack.readItemStackFromNbt(compoundTag1);
			}
		}

		if (compoundTag.containsKey("m_iFireUnderType")) {
			fireUnderType = compoundTag.getInteger("m_iFireUnderType");
		} else {
			// legacy support to force compute of this state if it's not in the NBT

			fireUnderType = -1;
		}

		validateInventoryStateVariables();
	}

	@Override
	public void writeAdditionalData(@NotNull CompoundTag compoundTag) {
		ListTag nbtTagList = new ListTag();

		for (int i = 0; i < contents.length; i++) {
			if (contents[i] != null) {
				CompoundTag compoundTag1 = new CompoundTag();
				compoundTag1.putByte("Slot", (byte) i);

				contents[i].writeToNBT(compoundTag1);

				nbtTagList.addTag(compoundTag1);
			}
		}

		compoundTag.put("Items", nbtTagList);
		compoundTag.putBoolean("m_bContainsValidIngrediantsForState", containsValidIngredientsForState);
		compoundTag.putInt("m_iFireUnderType", fireUnderType);
	}

	@Override
	public void tick() {
		if (worldObj == null || worldObj.isClientSide) {
			return;
		}

		Block<?> block = worldObj.getBlockType(tilePos);

		if (block == null || !(block.getLogic() instanceof BlockLogicCookingVessel cookingBlock)) {
			// shouldn't happen
			return;
		}

		if (fireUnderType == -1) {
			// forced update of fire state for vessels saved without it (e.g. converted BTW 2.94 vessels)
			validateFireUnderType();
		}

		if (!cookingBlock.getMechanicallyPoweredFlag(worldObj, tilePos)) {
			// only cook if upright (not mechanically powered) and if there's a fire under
			if (fireUnderType > 0) {
				if (forceValidateOnUpdate) {
					validateContentsForState();
					forceValidateOnUpdate = false;
				}

				if (fireUnderType == 2) {
					if (stokedCooldownCounter <= 0) {
						cookCounter = 0;
					}

					stokedCooldownCounter = STOKED_TICKS_TO_COOLDOWN;

					performStokedFireUpdate(getCurrentFireFactor());
				} else if (stokedCooldownCounter > 0) {
					// this prevents the vessel from going back into regular cook mode if the fire beneath it is
					// momentarily not stoked

					stokedCooldownCounter--;

					if (stokedCooldownCounter <= 0) {
						// reset the cook counter so that time spent rendering does not translate into cook time
						cookCounter = 0;
					}
				} else {
					performNormalFireUpdate(getCurrentFireFactor());
				}
			} else {
				cookCounter = 0;
			}
		} else {
			cookCounter = 0;

			int tiltFacing = cookingBlock.getTiltFacing(worldObj, tilePos);

			attemptToEjectStackFromInv(tiltFacing);
		}

		scaledCookCounter = (cookCounter * 1000) / TIME_TO_COOK;
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
		onInventoryChanged();
	}

	@Override
	public int getMaxStackSize() {
		return STACK_SIZE_LIMIT;
	}

	@Override
	public void sort() {}

	public void onInventoryChanged() {
		forceValidateOnUpdate = true;

		if (worldObj != null) {
			updateNeighboringMatchers();

			if (validateInventoryStateVariables()) {
				worldObj.markBlockNeedsUpdate(tilePos);
			}
		}
	}

	public boolean stillValid(@NotNull Player player) {
		if (worldObj == null || worldObj.getTileEntity(tilePos) != this) {
			return false;
		}

		Block<?> block = worldObj.getBlockType(tilePos);

		if (!(block.getLogic() instanceof BlockLogicCookingVessel cookingBlock)) {
			return false;
		}

		if (cookingBlock.isOpenSideBlocked(worldObj, tilePos)) {
			return false;
		}

		return player.distanceToSqr(tilePos.x + 0.5D, tilePos.y + 0.5D, tilePos.z + 0.5D) <= MAX_PLAYER_INTERACTION_DIST;
	}

	@Override
	public void dropContents(World world, int x, int y, int z) {
		super.dropContents(world, x, y, z);
		InventoryUtils.ejectInventoryContents(world, new TilePos(x, y, z), this);
	}

	public abstract void validateContentsForState();

	protected abstract RecipeGroupBulk getCraftingManager(int fireType);

	/*
	 * FireUnderType is 0 for none, 1 for normal, 2 for stoked
	 */
	public int getCurrentFireFactor() {
		int fireFactor = 0;

		if (fireUnderType > 0) {
			fireFactor = PRIMARY_FIRE_FACTOR;

			TilePos tempPos = new TilePos();
			int tempY = tilePos.y - 1;

			for (int tempX = tilePos.x - 1; tempX <= tilePos.x + 1; tempX++) {
				for (int tempZ = tilePos.z - 1; tempZ <= tilePos.z + 1; tempZ++) {
					if (tempX != tilePos.x || tempZ != tilePos.z) {
						Block<?> tempBlock = worldObj.getBlockType(tempPos.set(tempX, tempY, tempZ));

						if (fireUnderType == 1) {
							if (isNormalFire(tempBlock)) {
								fireFactor += SECONDARY_FIRE_FACTOR;
							}
						} else if (tempBlock == BTXBlocks.STOKED_FIRE) {
							fireFactor += SECONDARY_FIRE_FACTOR;
						}
					}
				}
			}
		}

		return fireFactor;
	}

	private static boolean isNormalFire(@Nullable Block<?> block) {
		return block == Blocks.FIRE || block == Blocks.FIRE_SULFURIC;
	}

	public void validateFireUnderType() {
		if (worldObj == null) {
			return;
		}

		int newType = 0;

		Block<?> blockUnder = worldObj.getBlockType(tilePos.down(new TilePos()));

		if (isNormalFire(blockUnder)) {
			newType = 1;
		} else if (blockUnder == BTXBlocks.STOKED_FIRE) {
			newType = 2;
		}

		if (newType != fireUnderType) {
			fireUnderType = newType;

			validateContentsForState();
		}
	}

	private void performNormalFireUpdate(int fireFactor) {
		if (containsValidIngredientsForState) {
			cookCounter += fireFactor;

			if (cookCounter >= TIME_TO_COOK) {
				attemptToCookNormal();

				// reset the cook counter to start the next item
				cookCounter = 0;
			}
		} else {
			cookCounter = 0;
		}
	}

	private void performStokedFireUpdate(int fireFactor) {
		if (containsValidIngredientsForState) {
			cookCounter += fireFactor;

			if (cookCounter >= TIME_TO_COOK) {
				if (doesContainExplosives()) {
					blowUp();
				} else {
					attemptToCookStoked();
				}

				cookCounter = 0;
			}
		} else {
			cookCounter = 0;
		}
	}

	protected boolean attemptToCookNormal() {
		return attemptToCookWithManager(getCraftingManager(1));
	}

	protected boolean attemptToCookStoked() {
		return attemptToCookWithManager(getCraftingManager(2));
	}

	private boolean attemptToCookWithManager(RecipeGroupBulk manager) {
		if (manager != null) {
			if (manager.getCraftingResult(this) != null) {
				List<ItemStack> outputList = manager.consumeIngredientsAndReturnResult(this);

				assert (outputList != null && !outputList.isEmpty());

				for (ItemStack output : outputList) {
					ItemStack cookedStack = output.copy();

					if (!InventoryUtils.addItemStackToInventory(this, cookedStack)) {
						MiscUtils.ejectStackWithRandomOffset(worldObj, tilePos.up(new TilePos()), cookedStack);
					}
				}

				return true;
			}
		}

		return false;
	}

	public int getCookProgressScaled(int scale) {
		return (scaledCookCounter * scale) / 1000;
	}

	public boolean isCooking() {
		return (scaledCookCounter > 0);
	}

	protected boolean doesContainExplosives() {
		return InventoryUtils.getFirstOccupiedStackOfItem(this, BTXItems.HELLFIRE_DUST.id) >= 0
			|| InventoryUtils.getFirstOccupiedStackOfItem(this, Blocks.TNT.asItem().id) >= 0
			|| InventoryUtils.getFirstOccupiedStackOfItem(this, Items.SULFUR.id) >= 0
			// TODO: Add blasting oil if it gets added at some point
			/**|| InventoryUtils.getFirstOccupiedStackOfItem(this, BTXItems.BLASTING_OIL.id) >= 0**/;
	}

	private void blowUp() {
		int hellfireCount = InventoryUtils.countItemsInInventory(this, BTXItems.HELLFIRE_DUST.id, -1);

		float explosionSize = (hellfireCount * 10.0F) / 64.0F;

		explosionSize += (InventoryUtils.countItemsInInventory(this, Items.SULFUR.id, -1) * 10.0F) / 64.0F;

		// TODO: Add blasting oil if it gets added at some point
		//explosionSize += (InventoryUtils.countItemsInInventory(this, BTXItems.BLASTING_OIL.id, -1) * 10.0F) / 64.0F;

		int tntCount = InventoryUtils.countItemsInInventory(this, Blocks.TNT.asItem().id, -1);

		if (tntCount > 0) {
			if (explosionSize < 4.0F) {
				explosionSize = 4.0F;
			}

			explosionSize += tntCount;
		}

		if (explosionSize < 2.0F) {
			explosionSize = 2.0F;
		} else if (explosionSize > 10.0F) {
			explosionSize = 10.0F;
		}

		InventoryUtils.clearInventoryContents(this);
		worldObj.setBlockTypeNotify(tilePos, Blocks.AIR);
		worldObj.createExplosion(null, tilePos.x, tilePos.y, tilePos.z, explosionSize, true, false);
	}

	private void attemptToEjectStackFromInv(int tiltFacing) {
		int stackIndex = InventoryUtils.getFirstOccupiedStackNotOfItem(this, Items.BRICK_CLAY.id);

		if (stackIndex >= 0 && stackIndex < getContainerSize()) {
			ItemStack invStack = getItem(stackIndex);

			int ejectStackSize = Math.min(STACK_SIZE_TO_DROP_WHEN_TIPPED, invStack.stackSize);

			ItemStack ejectStack = invStack.copy();
			ejectStack.stackSize = ejectStackSize;

			TilePos targetPos = new TilePos();
			ivangeevo.btx.block.blocks.BlockLogicAxle.addFacingAsOffset(tilePos, tiltFacing, targetPos);

			boolean ejectIntoWorld = false;

			if (worldObj == null) return;

			if (worldObj.isAirBlock(targetPos)) {
				ejectIntoWorld = true;
			} else if (worldObj.getBlockType(targetPos).getMaterial().isReplaceable()) {
				ejectIntoWorld = true;
			} else {
				Block<?> targetBlock = worldObj.getBlockType(targetPos);

				if (!targetBlock.getMaterial().isSolid()) {
					ejectIntoWorld = true;
				}
			}

			if (ejectIntoWorld) {
				ejectStack(ejectStack, tiltFacing);

				removeItem(stackIndex, ejectStackSize);
			}
		}
	}

	private void ejectStack(ItemStack stack, int facing) {
		Direction direction = FacingUtils.directionFromFacing(facing);

		double x = tilePos.x + 0.5D + direction.offsetX() * 0.5D;
		double y = tilePos.y + 0.25D + direction.offsetY() * 0.5D;
		double z = tilePos.z + 0.5D + direction.offsetZ() * 0.5D;

		EntityItem entityItem = new EntityItem(worldObj, x, y, z, stack);

		entityItem.xd = direction.offsetX() * 0.1D;
		entityItem.yd = direction.offsetY() * 0.1D;
		entityItem.zd = direction.offsetZ() * 0.1D;

		entityItem.pickupDelay = 10;

		worldObj.entityJoinedWorld(entityItem);
	}

	private boolean validateInventoryStateVariables() {
		boolean stateChanged = false;

		short currentSlotsOccupied = (short) (InventoryUtils.getNumOccupiedStacks(this));

		if (currentSlotsOccupied != storageSlotsOccupied) {
			storageSlotsOccupied = currentSlotsOccupied;

			stateChanged = true;
		}

		return stateChanged;
	}
}
