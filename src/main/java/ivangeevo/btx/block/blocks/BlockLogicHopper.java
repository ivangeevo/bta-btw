package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.util.ItemOnTopCollision;
import ivangeevo.btx.block.entity.TileEntityHopper;
import ivangeevo.btx.gui.BTXScreens;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.hopper.RecipeEntryHopper;
import ivangeevo.btx.sound.BTXSoundTypes;
import ivangeevo.btx.util.utils.InventoryUtils;
import ivangeevo.btx.util.utils.MechPowerUtils;
import ivangeevo.btx.util.utils.MiscUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import ivangeevo.btx.util.utils.WorldUtils;
import ivangeevo.btx.world.TickPersistence;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;

import java.util.Random;

public class BlockLogicHopper
	extends BlockLogic
	implements MechanicalDevice, ItemOnTopCollision, BlockLogic.MatcherEntityEquivalency<TileEntityHopper>
{

	private static final int TICK_RATE = 10;

	private static final int ON_BIT = 1;
	private static final int FILTER_BIT = 8;

	private static final double COLLECTION_HEIGHT = 1.0;

	public BlockLogicHopper(@NotNull Block<?> block) {
		super(block, Materials.WOOD);
	}

	public static BlockLogicSupplier<BlockLogicHopper> properties() {
		return block -> new BlockLogicHopper(
			block
				.setTicking(true)
				.withEntity(TileEntityHopper::new)
				.withSound(BlockSounds.WOOD)
				.withHardness(2.0F)
				.withTags(BlockTags.MINEABLE_BY_AXE)
		);
	}

	@Override
	public int tickDelay() {
		return TICK_RATE;
	}

	@Override
	public boolean isSolidRender() {
		return false;
	}

	@Override
	public boolean isCubeShaped() {
		return false;
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		super.onPlacedByWorld(world, tilePos);
		world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		boolean receivingPower = isInputtingMechanicalPower(world, tilePos);

		if (isBlockOn(world, tilePos) != receivingPower && !WorldUtils.isUpdatePendingThisTick(world, tilePos, this.block)) {
			world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
		}

		// the change in state may unblock the hopper, and this forces a recheck
		if (world.getTileEntity(tilePos) instanceof TileEntityHopper hopper) {
			hopper.outputBlocked = false;
		}
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		if (isRandomTick) {
			// random ticks only revalidate a power state that got out of sync (e.g. a lost scheduled tick)
			if (isBlockOn(world, tilePos) != isInputtingMechanicalPower(world, tilePos)
				&& !((TickPersistence) world).btx$isUpdateScheduled(tilePos, this.block)) {
				world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
			}

			return;
		}

		boolean receivingPower = isInputtingMechanicalPower(world, tilePos);

		if (isBlockOn(world, tilePos) != receivingPower) {
			// CE: HOPPER_CLOSE_EFFECT_ID is "mob.irongolem.walk", which BTA doesn't have
			//TODO: Fix CE sound not present
			//SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, "random.chestclosed", 0.5F, 1.25F);

			setBlockOn(world, tilePos, receivingPower);
		}
	}

	@Override
	public boolean onInteracted(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Player player, @Nullable Side side, double xHit, double yHit) {
		if (!world.isClientSide && world.getTileEntity(tilePos) instanceof TileEntityHopper hopper) {
			((BTXScreens) player).btx$displayHopperScreen(hopper);
		}

		return true;
	}

	@Override
	public ItemStack @Nullable [] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		return switch (dropCause) {
			case PICK_BLOCK, EXPLOSION, PROPER_TOOL, IMPROPER_TOOL, SILK_TOUCH, WORLD ->
				new ItemStack[]{new ItemStack(BTXBlocks.HOPPER)};
			default -> null;
		};
	}

	@Override
	public void onEntityCollision(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Entity entity) {
		// don't collect items on the client, as it's dependent on the state of the inventory
		if (!world.isClientSide && entity instanceof EntityItem entityItem) {
			onEntityItemCollidedWithBlock(world, tilePos, entityItem);
		}
	}

	@Override
	public @NotNull Class<TileEntityHopper> getEntityEquivalencyClass() {
		return TileEntityHopper.class;
	}

	@Override
	public boolean isEquivalentEntityForMatcherImpl(TileEntityHopper thisHopper, TileEntityHopper thatHopper) {
		return thisHopper.getMatcherLevel() == thatHopper.getMatcherLevel();
	}


	@Override
	public boolean canOutputMechanicalPower() {
		return false;
	}

	@Override
	public boolean canInputMechanicalPower() {
		return true;
	}

	@Override
	public boolean isInputtingMechanicalPower(World world, TilePosc tilePos) {
		return MechPowerUtils.isBlockPoweredByAxle(world, tilePos, this)
			|| MechPowerUtils.isBlockPoweredByHandCrank(world, tilePos);
	}

	@Override
	public boolean canInputAxlePowerToFacing(World world, TilePosc tilePos, Direction facing) {
		return facing != Direction.DOWN && facing != Direction.UP;
	}

	@Override
	public boolean isOutputtingMechanicalPower(World world, TilePosc tilePos) {
		return false;
	}

	@Override
	public void overpower(World world, TilePosc tilePos) {
		breakHopper(world, tilePos);
	}

	public boolean isBlockOn(WorldSource source, TilePosc tilePos) {
		return (source.getBlockData(tilePos) & ON_BIT) > 0;
	}

	public void setBlockOn(World world, TilePosc tilePos, boolean on) {
		int data = world.getBlockData(tilePos) & ~ON_BIT;

		if (on) {
			data |= ON_BIT;
		}

		world.setBlockData(tilePos, data);
	}

	public boolean hasFilter(WorldSource source, TilePosc tilePos) {
		return (source.getBlockData(tilePos) & FILTER_BIT) > 0;
	}

	public void setHasFilter(World world, TilePosc tilePos, boolean hasFilter) {
		int oldData = world.getBlockData(tilePos);
		int data = oldData & ~FILTER_BIT;

		if (hasFilter) {
			data |= FILTER_BIT;
		}

		if (data != oldData) {
			world.setBlockData(tilePos, data);
		}
	}

	public void breakHopper(World world, TilePosc tilePos) {
		if (world.getBlockType(tilePos) != this.block) {
			return;
		}

		for (int i = 0; i < 2; i++) {
			MiscUtils.ejectSingleItemWithRandomOffset(world, tilePos, BTXBlocks.SIDING_OAK.id(), 0);
		}

		MiscUtils.ejectSingleItemWithRandomOffset(world, tilePos, BTXItems.GEAR.id, 0);

		world.playSoundEffect(null, SoundCategory.WORLD_SOUNDS,
			tilePos.x() + 0.5, tilePos.y() + 0.5, tilePos.z() + 0.5, BTXSoundTypes.MECHANICAL_BREAK, 0.5F, 0.6F + world.rand.nextFloat() * 0.25F);

		world.setBlockTypeNotify(tilePos, Blocks.AIR);
	}

	public boolean collectStack(World world, TilePosc tilePos, ItemStack stack) {
		if (!(world.getTileEntity(tilePos) instanceof TileEntityHopper hopper) || !hopper.canCurrentFilterProcessItem(stack)) {
			return false;
		}

		if (InventoryUtils.addItemStackToInventoryInSlotRange(hopper, stack, 0, TileEntityHopper.STORAGE_SLOTS - 1)) {
			playCollectionPop(world, tilePos);
			return true;
		}

		return false;
	}

	private void onEntityItemCollidedWithBlock(World world, TilePosc tilePos, EntityItem entityItem) {
		if (!entityItem.isAlive() || entityItem.item == null || entityItem.item.stackSize <= 0) {
			return;
		}

		// check if item is within the collection zone
		AABBd collectionZone = new AABBd(
			tilePos.x(), tilePos.y() + COLLECTION_HEIGHT, tilePos.z(),
			tilePos.x() + 1, tilePos.y() + COLLECTION_HEIGHT + 0.05, tilePos.z() + 1);

		if (!entityItem.bb.intersectsAABB(collectionZone)) {
			return;
		}

		if (!(world.getTileEntity(tilePos) instanceof TileEntityHopper hopper)) {
			return;
		}

		ItemStack targetStack = entityItem.item;

		if (!hopper.canCurrentFilterProcessItem(targetStack)) {
			return;
		}

		RecipeEntryHopper recipe = BTXRecipes.HOPPER.findRecipe(targetStack, hopper.getFilterStack());

		if (recipe == null) {
			int sizeBefore = targetStack.stackSize;

			if (InventoryUtils.addItemStackToInventoryInSlotRange(hopper, targetStack, 0, TileEntityHopper.STORAGE_SLOTS - 1)) {
				playCollectionPop(world, tilePos);
				entityItem.remove();
			} else if (targetStack.stackSize != sizeBefore) {
				playCollectionPop(world, tilePos);
			}

			return;
		}

		if (recipe.containsSouls()) {
			convertItemAndIncrementSouls(world, tilePos, hopper, entityItem, recipe);
			return;
		}

		processFilterRecipe(world, tilePos, hopper, entityItem, recipe);
	}

	private void processFilterRecipe(World world, TilePosc tilePos, TileEntityHopper hopper, EntityItem entityItem, RecipeEntryHopper recipe) {
		ItemStack targetStack = entityItem.item;
		ItemStack hopperOutput = recipe.getHopperOutput();

		int quantityProcessed;

		if (hopperOutput == null) {
			// nothing goes into the hopper, so the whole stack gets filtered
			quantityProcessed = targetStack.stackSize;
		} else {
			int perItem = Math.max(1, hopperOutput.stackSize);
			int quantityToAttemptToSwallow = targetStack.stackSize * perItem;

			int availableSpots = InventoryUtils.getMaxNumberOfItemsForTransferInRange(hopper, hopperOutput,
				quantityToAttemptToSwallow, 0, TileEntityHopper.STORAGE_SLOTS - 1);

			// makes sure the output is a multiple of the input
			quantityProcessed = availableSpots / perItem;

			if (quantityProcessed <= 0) {
				return;
			}

			int quantityToSwallow = quantityProcessed * perItem;

			while (quantityToSwallow > 0) {
				ItemStack stackToTransfer = hopperOutput.copy();
				stackToTransfer.stackSize = Math.min(quantityToSwallow, stackToTransfer.getMaxStackSize());

				quantityToSwallow -= stackToTransfer.stackSize;

				InventoryUtils.addItemStackToInventoryInSlotRange(hopper, stackToTransfer, 0, TileEntityHopper.STORAGE_SLOTS - 1);
			}
		}

		targetStack.stackSize -= quantityProcessed;

		if (targetStack.stackSize <= 0) {
			entityItem.remove();
		}

		playCollectionPop(world, tilePos);

		ItemStack filteredOutput = recipe.getFilteredOutput();

		if (filteredOutput != null) {
			spawnOutputOnTop(world, entityItem, filteredOutput, quantityProcessed * filteredOutput.stackSize);
		}
	}

	private void convertItemAndIncrementSouls(World world, TilePosc tilePos, TileEntityHopper hopper, EntityItem entityItem, RecipeEntryHopper recipe) {
		int count = entityItem.item.stackSize;

		ItemStack filteredOutput = recipe.getFilteredOutput();

		if (filteredOutput != null) {
			spawnOutputOnTop(world, entityItem, filteredOutput, count * filteredOutput.stackSize);
		}

		hopper.incrementContainedSoulCount(count);

		SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.ENTITY_SOUNDS, BTXSoundTypes.HOPPER_SOUL_CAPTURE, 0.5F,
			2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

		entityItem.remove();
	}

	private void spawnOutputOnTop(World world, EntityItem source, ItemStack output, int totalCount) {
		while (totalCount > 0) {
			ItemStack stack = output.copy();
			stack.stackSize = Math.min(totalCount, stack.getMaxStackSize());

			totalCount -= stack.stackSize;

			EntityItem outputEntity = new EntityItem(world, source.x, source.y, source.z, stack);
			outputEntity.pickupDelay = 10;

			world.entityJoinedWorld(outputEntity);
		}
	}

	private static void playCollectionPop(World world, TilePosc tilePos) {
		SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.ITEM_COLLECT, 0.25F,
			((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
	}
}
