package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.turntable.TurntableInteraction;
import ivangeevo.btx.block.entity.TileEntityBlockDispenser;
import ivangeevo.btx.gui.BTXScreens;
import ivangeevo.btx.sound.BTXSoundTypes;
import ivangeevo.btx.util.utils.FacingUtils;
import ivangeevo.btx.util.utils.InventoryUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import ivangeevo.btx.util.utils.WorldUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.BlockLogicVeryRotatable;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.animal.MobChicken;
import net.minecraft.core.entity.animal.MobSheep;
import net.minecraft.core.entity.animal.MobWolf;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.entity.vehicle.EntityBoat;
import net.minecraft.core.entity.vehicle.EntityMinecart;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.IPlaceable;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class BlockLogicBlockDispenser extends BlockLogicVeryRotatable implements TurntableInteraction {
	public static final int MASK_POWERED = 8;
	private static final int TICK_RATE = 4;
	private static final int DISPENSER_SMOKE_EVENT = 2000;
	private static final int BLOCK_BREAK_EVENT = 2001;

	private static final Set<Block<?>> UNSWALLOWABLE = Set.of(
		Blocks.FIRE, Blocks.FIRE_COLD, Blocks.FIRE_SULFURIC, BTXBlocks.STOKED_FIRE,
		Blocks.PORTAL_NETHER, Blocks.PORTAL_DRIFT,
		Blocks.MOBSPAWNER, Blocks.MOBSPAWNER_DEACTIVATED,
		Blocks.PISTON_HEAD, Blocks.PISTON_HEAD_STEEL, Blocks.PISTON_MOVING
	);

	public BlockLogicBlockDispenser(@NotNull Block<?> block) {
		super(block, Materials.STONE);
	}

	public static BlockLogicSupplier<BlockLogicBlockDispenser> properties() {
		return block -> new BlockLogicBlockDispenser(
			block
				.setTicking(true)
				.withEntity(TileEntityBlockDispenser::new)
				.withSound(BlockSounds.STONE)
				.withHardness(3.5F)
				.withTags(BlockTags.MINEABLE_BY_PICKAXE)
		);
	}

	@Override
	public int tickDelay() {
		return TICK_RATE;
	}

	@Override
	public boolean onInteracted(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Player player, @Nullable Side side, double xHit, double yHit) {
		if (!world.isClientSide && world.getTileEntity(tilePos) instanceof TileEntityBlockDispenser dispenser) {
			((BTXScreens) player).btx$displayBlockDispenserScreen(dispenser);
		}
		return true;
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		world.scheduleBlockUpdate(tilePos, this.block, tickDelay());
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		if (!isCurrentStateValid(world, tilePos) && !WorldUtils.isUpdateScheduled(world, tilePos, this.block)) {
			world.scheduleBlockUpdate(tilePos, this.block, tickDelay());
		}
	}

	@Override
	public void onMoveComplete(@NotNull World world, @NotNull TilePosc tilePos) {
		onNeighborChanged(world, tilePos, this.block);
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		if (isRandomTick) {
			// verify a tick is already scheduled, so a block dispenser can't jam after chunk load
			onNeighborChanged(world, tilePos, this.block);
			return;
		}

		boolean powered = isReceivingRedstonePower(world, tilePos);

		if (powered == isRedstoneOn(world, tilePos)) {
			return;
		}

		world.setBlockDataNotify(tilePos, (world.getBlockData(tilePos) & ~MASK_POWERED) | (powered ? MASK_POWERED : 0));

		if (powered) {
			dispense(world, tilePos);
		} else {
			swallow(world, tilePos);
		}
	}

	//------------- Dispensing -------------//

	private void dispense(World world, TilePosc tilePos) {
		if (!(world.getTileEntity(tilePos) instanceof TileEntityBlockDispenser dispenser)) {
			return;
		}

		Direction direction = getDirection(world, tilePos);
		TilePos targetPos = tilePos.add(direction, new TilePos());
		Block<?> targetBlock = world.getBlockType(targetPos);

		if (targetBlock == Blocks.AIR || targetBlock.hasTag(BlockTags.PLACE_OVERWRITES) || !targetBlock.getMaterial().isSolid()) {
			ItemStack stack = dispenser.getCurrentItemToDispense();

			if (stack != null && useStack(world, tilePos, targetPos, direction, stack)) {
				dispenser.onDispenseCurrentSlot();
				world.playBlockEvent(tilePos, DISPENSER_SMOKE_EVENT, direction.id);
				return;
			}
		}

		SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.BLOCK_DISPENSER_CLICK, 1.0F, 1.2F);
	}

	private boolean useStack(World world, TilePosc tilePos, TilePosc targetPos, Direction direction, ItemStack stack) {
		if (stack.getItem() instanceof IPlaceable placeable) {
			return placeable.placeWithoutShift(stack, world, null, targetPos, direction.side().opposite(), 0.5, 0.5);
		}

		spitOutItem(world, tilePos, stack);
		SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.BLOCK_DISPENSER_CLICK, 1.0F, 1.0F);
		return true;
	}

	public void spitOutItem(World world, TilePosc tilePos, ItemStack stack) {
		Direction direction = getDirection(world, tilePos);
		double x = tilePos.x() + direction.offsetX() * 0.5 + 0.5;
		double y = tilePos.y() + direction.offsetY() + 0.2;
		double z = tilePos.z() + direction.offsetZ() * 0.5 + 0.5;

		// slight upwards trajectory when fired sideways
		double yHeading = direction.offsetY() != 0 ? direction.offsetY() : 0.1;

		EntityItem entityItem = new EntityItem(world, x, y, z, stack);
		double velocity = world.rand.nextDouble() * 0.1 + 0.2;

		entityItem.xd = direction.offsetX() * velocity + world.rand.nextGaussian() * 0.045;
		entityItem.yd = yHeading * velocity + 0.2 + world.rand.nextGaussian() * 0.045;
		entityItem.zd = direction.offsetZ() * velocity + world.rand.nextGaussian() * 0.045;

		world.entityJoinedWorld(entityItem);
	}

	//------------- Swallowing -------------//

	private void swallow(World world, TilePosc tilePos) {
		if (!(world.getTileEntity(tilePos) instanceof TileEntityBlockDispenser dispenser)) {
			return;
		}

		TilePos targetPos = tilePos.add(getDirection(world, tilePos), new TilePos());

		if (swallowEntity(world, tilePos, targetPos, dispenser)) {
			return;
		}

		Block<?> targetBlock = world.getBlockType(targetPos);

		if (world.isAirBlock(targetPos) || targetBlock == BTXBlocks.CEMENT || targetBlock.getHardness() < 0.0F || targetBlock.getMaterial().isLiquid()
			|| UNSWALLOWABLE.contains(targetBlock)) {
			return;
		}

		int targetData = world.getBlockData(targetPos);
		TileEntity targetTileEntity = world.getTileEntity(targetPos);

		ItemStack[] result = targetBlock.getLogic().getBreakResult(world, EnumDropCause.SILK_TOUCH, targetPos, targetData, targetTileEntity);

		if (result == null || result.length == 0) {
			result = targetBlock.getLogic().getBreakResult(world, EnumDropCause.PROPER_TOOL, targetPos, targetData, targetTileEntity);
		}

		if (result == null) {
			return;
		}

		boolean swallowed = false;

		for (ItemStack stack : result) {
			if (stack != null && dispenser.addStack(stack.copy())) {
				swallowed = true;
			}
		}

		if (!swallowed) {
			return;
		}

		// a swallowed block dispenser loses its contents to prevent dupes
		if (targetTileEntity instanceof TileEntityBlockDispenser swallowedDispenser) {
			InventoryUtils.clearInventoryContents(swallowedDispenser);
		}

		world.playBlockEvent(targetPos, BLOCK_BREAK_EVENT, targetBlock.id());
		world.setBlockTypeNotify(targetPos, Blocks.AIR);
	}

	private boolean swallowEntity(World world, TilePosc tilePos, TilePosc targetPos, TileEntityBlockDispenser dispenser) {
		AABBd area = new AABBd(targetPos.x(), targetPos.y(), targetPos.z(), targetPos.x() + 1, targetPos.y() + 1, targetPos.z() + 1);
		List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, area);

		for (Entity entity : entities) {
			if (!entity.removed && swallowEntity(world, tilePos, entity, dispenser)) {
				return true;
			}
		}

		return false;
	}

	private boolean swallowEntity(World world, TilePosc tilePos, Entity entity, TileEntityBlockDispenser dispenser) {
		if (entity instanceof EntityMinecart minecart) {
			minecart.ejectRider();
			int type = minecart.getType();
			int meta = minecart.getMeta();
			minecart.remove();

			dispenser.addStack(new ItemStack(Items.MINECART));

			if (type == 1) {
				dispenser.addStack((meta & 1) != 0
					? new ItemStack(Blocks.CHEST_PLANKS_OAK_PAINTED, 1, meta & 240)
					: new ItemStack(Blocks.CHEST_PLANKS_OAK));
			} else if (type == 2) {
				dispenser.addStack(new ItemStack(Blocks.FURNACE_STONE_IDLE));
			}

			SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.BLOCK_DISPENSER_CLICK, 1.0F, 1.2F);
			return true;
		}

		if (entity instanceof EntityBoat boat) {
			boat.ejectRider();
			boat.remove();
			dispenser.addStack(new ItemStack(Items.BOAT));

			SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.BLOCK_DISPENSER_CLICK, 1.0F, 1.2F);
			return true;
		}

		if (entity instanceof MobWolf wolf) {
			world.playSoundAtEntity(null, wolf, BTXSoundTypes.BLOCK_DISPENSER_EAT_WOLF, 1.0F,
				(world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
			wolf.remove();
			dispenser.addStack(new ItemStack(BTXBlocks.COMPANION_CUBE));

			for (int i = 0; i < 2; i++) {
				spitOutItem(world, tilePos, new ItemStack(Items.STRING));
			}
			return true;
		}

		if (entity instanceof MobChicken chicken) {
			world.playSoundAtEntity(null, chicken, BTXSoundTypes.BLOCK_DISPENSER_EAT_CHICKEN, 1.0F,
				(world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
			chicken.remove();
			dispenser.addStack(new ItemStack(Items.EGG_CHICKEN));
			return true;
		}

		if (entity instanceof MobSheep sheep && !sheep.getSheared()) {
			sheep.setSheared(true);
			dispenser.addStack(new ItemStack(Blocks.WOOL, 2, sheep.getFleeceColor().blockMeta));

			for (int i = 0; i < 2; i++) {
				spitOutItem(world, tilePos, new ItemStack(Items.STRING));
			}
			return true;
		}

		return false;
	}

	//------------- State -------------//

	public static Direction getDirection(WorldSource source, TilePosc tilePos) {
		return metaToDirection(source.getBlockData(tilePos));
	}

	public boolean isRedstoneOn(WorldSource source, TilePosc tilePos) {
		return (source.getBlockData(tilePos) & MASK_POWERED) != 0;
	}

	private boolean isReceivingRedstonePower(World world, TilePosc tilePos) {
		return world.hasDirectSignal(tilePos) || world.hasDirectSignal(new TilePos(tilePos.x(), tilePos.y() + 1, tilePos.z()));
	}

	private boolean isCurrentStateValid(World world, TilePosc tilePos) {
		return isRedstoneOn(world, tilePos) == isReceivingRedstonePower(world, tilePos);
	}

	//------------- Turntable -------------//

	@Override
	public boolean canRotateOnTurntable(WorldSource source, TilePosc tilePos) {
		return true;
	}

	@Override
	public int rotateOnTurntable(World world, TilePosc pos, boolean reverse, int craftingCounter) {
		int data = world.getBlockData(pos);
		int facing = FacingUtils.facingFromDirection(metaToDirection(data));
		int rotated = FacingUtils.rotateFacingAroundY(facing, reverse);

		if (rotated != facing) {
			world.setBlockDataNotify(pos, setDirection(data, FacingUtils.directionFromFacing(rotated)));
		}
		return craftingCounter;
	}
}
