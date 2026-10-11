package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.networking.ClientDataChangeListener;
import ivangeevo.btx.util.FacingUtils;
import ivangeevo.btx.util.MechPowerUtils;
import ivangeevo.btx.util.MiscUtils;
import net.minecraft.core.block.*;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.enums.EnumDropCause;
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
import org.joml.primitives.AABBdc;

import java.util.Random;

public class BlockLogicAxle extends BlockLogic implements MechanicalDevice, ClientDataChangeListener {
	protected static final double AXLE_WIDTH = 0.25;
	protected static final double AXLE_HALF_WIDTH = (AXLE_WIDTH / 2);

	public static final int TICK_RATE = 1;

	private static final int[][] axleFacingsForAlignment = new int[][]{{0,1}, {2, 3}, {4, 5}};
	private static final int[][] FACING_OFFSETS = new int[][]{
		{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}
	};

	public BlockLogicAxle(@NotNull Block<?> block) {
		super(block, Materials.WOOD);
	}

	public static BlockLogicSupplier<BlockLogicAxle> properties() {
		return block -> new BlockLogicAxle(
			block
				.withSound(BlockSounds.WOOD)
				.withHardness(2.0F)
				.withTags(BlockTags.MINEABLE_BY_AXE)
		);
	}

	public int getTickRate() {
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
	public void onPlacedOnSide(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		setAxisAlignmentBasedOnFacing(world, tilePos, facingFromSide(side));
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		super.onPlacedByWorld(world, tilePos);
		world.scheduleBlockUpdate(tilePos, this.block, this.getTickRate());
		markNearbyForRenderUpdate(world, tilePos);
	}

	@Override
	public void onRemoved(@NotNull World world, @NotNull TilePosc tilePos, int data) {
		super.onRemoved(world, tilePos, data);
		markNearbyForRenderUpdate(world, tilePos);
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		// Should only be called when the block is first added, to force it to validate
		// its power level for the first time. All following changes are instantaneous

		setPowerLevel(world, tilePos, 0);
		validatePowerLevel(world, tilePos);
	}

	@Override
	public void animationTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand) {
		if (clientCheckIfPowered(world, tilePos)) {
			emitAxleParticles(world, tilePos, rand);
		}
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		validatePowerLevel(world, tilePos);
	}

	@Override
	public @NotNull AABBdc getSelectionAABB(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		int axis = getAxisAlignment(source, tilePos);
		double x = tilePos.x();
		double y = tilePos.y();
		double z = tilePos.z();

		return switch (axis) {
			case 0 -> new AABBd(x + 0.5D - AXLE_HALF_WIDTH, y, z + 0.5D - AXLE_HALF_WIDTH,
				x + 0.5D + AXLE_HALF_WIDTH, y + 1D, z + 0.5D + AXLE_HALF_WIDTH);
			case 1 -> new AABBd(x + 0.5D - AXLE_HALF_WIDTH, y + 0.5D - AXLE_HALF_WIDTH, z,
				x + 0.5D + AXLE_HALF_WIDTH, y + 0.5D + AXLE_HALF_WIDTH, z + 1D);
			default -> new AABBd(x, y + 0.5D - AXLE_HALF_WIDTH, z + 0.5D - AXLE_HALF_WIDTH,
				x + 1D, y + 0.5D + AXLE_HALF_WIDTH, z + 0.5D + AXLE_HALF_WIDTH);
		};
	}

	@Override
	public @Nullable AABBdc getCollisionAABB(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return this.getSelectionAABB(source, tilePos);
	}

	@Override
	public @NotNull ItemStack @Nullable [] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		switch (dropCause) {
			case PICK_BLOCK, EXPLOSION, PROPER_TOOL, SILK_TOUCH -> {
				return new ItemStack[]{new ItemStack(BTXBlocks.AXLE)};
			}
			default -> {
				return null;
			}
		}
	}

	@Override
	public boolean canOutputMechanicalPower() {
		return false;
	}

	@Override
	public boolean canInputMechanicalPower() {
		return false;
	}

	@Override
	public boolean isInputtingMechanicalPower(World world, TilePosc tilePos) {
		return false;
	}

	@Override
	public boolean isOutputtingMechanicalPower(World world, TilePosc tilePos) {
		return false;
	}

	@Override
	public int getMechanicalPowerLevelProvidedToAxleAtFacing(World world, TilePosc tilePos, Direction facing) {
		if ((FacingUtils.facingFromDirection(facing) >> 1) == getAxisAlignment(world, tilePos)) {
			return getPowerLevel(world, tilePos);
		}
		return 0;
	}

	public boolean toggleFacing(World world, TilePosc tilePos, boolean reverse) {
		int axisAlignment = getAxisAlignment(world, tilePos);

		axisAlignment += reverse ? 2 : 1; // +2 mod 3 == -1 mod 3
		axisAlignment %= 3;

		setAxisAlignment(world, tilePos, axisAlignment);
		markNearbyForRenderUpdate(world, tilePos);

		setPowerLevel(world, tilePos, 0);
		validatePowerLevel(world, tilePos);

		// Re-send final data to clients (matches original markBlockForUpdate).
		world.markBlocksDirty(tilePos, tilePos);
		return true;
	}

	public int getAxisAlignment(WorldSource source, TilePosc tilePos) {
		return (source.getBlockData(tilePos) >> 2);
	}

	public void setAxisAlignment(World world, TilePosc tilePos, int axisAlignment) {
		int data = world.getBlockData(tilePos) & 3;

		data |= (axisAlignment << 2);

		world.setBlockDataNotify(tilePos, data);
	}

	public int getAxisAlignmentFromMetadata(int data) {
		return (data >> 2);
	}

	public void setAxisAlignmentBasedOnFacing(World world, TilePosc tilePos, int facing) {
		int data = setAxisAlignmentInMetadataBasedOnFacing(world.getBlockData(tilePos), facing);
		setDataWithNotify(world, tilePos, data);
	}

	public int setAxisAlignmentInMetadataBasedOnFacing(int data, int facing) {
		int axis = switch (facing) {
			case 0, 1 -> 0;
			case 2, 3 -> 1;
			default -> 2;
		};

		data &= 3; // filter out any old alignment

		data |= (axis << 2);

		return data;
	}

	public int getPowerLevel(WorldSource source, TilePosc tilePos) {
		return getPowerLevelFromMetadata(source.getBlockData(tilePos));
	}

	public int getPowerLevelFromMetadata(int data) {
		return data & 3;
	}

	public void setPowerLevelWithoutNotify(World world, TilePosc tilePos, int powerLevel) {
		int data = setPowerLevelInMetadata(world.getBlockData(tilePos), powerLevel);
		world.setBlockData(tilePos, data);
	}

	public void setPowerLevel(World world, TilePosc tilePos, int powerLevel) {
		int data = setPowerLevelInMetadata(world.getBlockData(tilePos), powerLevel);
		world.setBlockData(tilePos, data);
		world.notifyBlockChange(tilePos, this.block);

	}

	public int setPowerLevelInMetadata(int data, int powerLevel) {
		powerLevel &= 3;
		data &= 12;
		data |= powerLevel;
		return data;
	}

	public boolean isAxleOrientedTowardsFacing(WorldSource source, TilePosc tilePos, int facing) {
		int axis = getAxisAlignment(source, tilePos);

		return switch (axis) {
			case 0 -> facing == 0 || facing == 1;
			case 1 -> facing == 2 || facing == 3;
			default -> facing == 4 || facing == 5;
		};
	}

	public void breakAxle(World world, TilePosc tilePos) {
		if (world.getBlockType(tilePos) == this.block) {
			dropComponentItemsOnBadBreak(world, tilePos, world.getBlockData(tilePos), 1F);
			world.playSoundEffect(null, SoundCategory.ENTITY_SOUNDS,
				tilePos.x() + 0.5, tilePos.y() + 0.5, tilePos.z() + 0.5, "random.explode", 0.2F, 1.25F);
			world.setBlockTypeNotify(tilePos, Blocks.AIR);
			markNearbyForRenderUpdate(world, tilePos); // harmless if onRemoved also does it
		}
	}

	public boolean dropComponentItemsOnBadBreak(World world, TilePosc tilePos, int data, float chanceOfDrop) {
		dropItemsIndividually(world, tilePos, BTXItems.HEMP_FIBERS.id, 4, 0, chanceOfDrop);
		dropItemsIndividually(world, tilePos, Items.STICK.id, 2, 0, chanceOfDrop);
		return true;
	}

	protected void dropItemsIndividually(World world, TilePosc tilePos, int itemId, int count, int damage, float chanceOfDrop) {
		for (int i = 0; i < count; i++) {
			if (world.rand.nextFloat() <= chanceOfDrop) {
				MiscUtils.ejectSingleItemWithRandomOffset(world, tilePos, itemId, damage);
			}
		}
	}

	protected void validatePowerLevel(World world, TilePosc tilePos) {
		int currentPower = getPowerLevel(world, tilePos);
		int axis = getAxisAlignment(world, tilePos);

		int maxNeighborPower = 0;
		int greaterPowerNeighbors = 0;

		TilePos tempSourcePos = new TilePos();

		for (int i = 0; i < 2; i++) {
			int facing = axleFacingsForAlignment[axis][i];
			addFacingAsOffset(tilePos, facing, tempSourcePos);

			Block<?> neighbor = world.getBlockType(tempSourcePos);

			if (neighbor.getLogic() instanceof MechanicalDevice device) {

				int neighborPower =
					device.getMechanicalPowerLevelProvidedToAxleAtFacing(
						world, tempSourcePos, Direction.fromId(facing).opposite()
					);

				maxNeighborPower = Math.max(maxNeighborPower, neighborPower);

				if (neighborPower > currentPower) {
					greaterPowerNeighbors++;
				}
			}
		}

		// Power coming from both directions destroys the axle.
		if (greaterPowerNeighbors >= 2) {
			breakAxle(world, tilePos);
			return;
		}

		int newPower = 0;

		if (maxNeighborPower > currentPower) {
			// Power level 1 cannot propagate any further.
			if (maxNeighborPower == 1) {
				breakAxle(world, tilePos);
				return;
			}

			newPower = maxNeighborPower - 1;
		}

		if (newPower != currentPower) {
			setPowerLevel(world, tilePos, newPower);
		}
	}

	private void emitAxleParticles(World world, TilePosc tilePos, Random random) {
		for (int counter = 0; counter < 2; counter++) {
			float smokeX = (float)tilePos.x() + random.nextFloat();
			float smokeY = (float)tilePos.y() + random.nextFloat() * 0.5F + 0.625F;
			float smokeZ = (float)tilePos.z() + random.nextFloat();

			world.spawnParticle("smoke", smokeX, smokeY, smokeZ, 0.0F, 0.0F, 0.0F, 0, false);
		}
	}

	public void overpower(World world, TilePosc tilePos) {
		int axis = getAxisAlignment(world, tilePos);
		overPowerBlockToFacing(world, tilePos, axis, axleFacingsForAlignment[axis][0]);
		overPowerBlockToFacing(world, tilePos, axis, axleFacingsForAlignment[axis][1]);
	}

	private void overPowerBlockToFacing(World world, TilePosc tilePos, int sourceAxis, int facing) {
		TilePos targetPos = new TilePos();
		addFacingAsOffset(tilePos, facing, targetPos);

		Block<?> targetBlock = world.getBlockType(targetPos);

		if (isBlockAxle(targetBlock)) {
			if (getAxisAlignment(world, targetPos) == sourceAxis) {
				// BUG FIX: recurse from targetPos, not tilePos (was infinite recursion)
				overPowerBlockToFacing(world, targetPos, sourceAxis, facing);
			}
		} else if (targetBlock.getLogic() instanceof MechanicalDevice mechDevice) {
			if (mechDevice.canInputAxlePowerToFacing(world, targetPos,
				Direction.fromId(getOppositeFacing(facing)))) {
				mechDevice.overpower(world, targetPos);
			}
		}
	}
	public static void addFacingAsOffset(TilePosc tilePos, int facing, TilePos dest) {
		int[] offset = FACING_OFFSETS[facing];
		dest.set(tilePos.x() + offset[0], tilePos.y() + offset[1], tilePos.z() + offset[2]);
	}

	public static int getOppositeFacing(int facing) {
		return facing ^ 1;
	}

	public static boolean isBlockAxle(@Nullable Block<?> block) {
		return block != null && (block == BTXBlocks.AXLE || block == BTXBlocks.AXLE_POWER_SOURCE);
	}

	/**
	// TODO: VERIFY: setBlockTypeData(pos, block, data) name and that it does not notify by itself.
	public static void swapAxleType(World world, TilePosc tilePos, Block<?> newBlock) {
		int axis = world.getBlockData(tilePos) >> 2;

		world.setBlockTypeData(tilePos, newBlock, axis << 2);
		world.notifyBlockChange(tilePos, newBlock);
	}
	 **/

	public static void swapAxleType(World world, TilePosc tilePos, Block<?> newBlock) {
		int data = world.getBlockData(tilePos);

		world.setBlockTypeData(tilePos, newBlock, data);
		world.notifyBlockChange(tilePos, newBlock);
	}

	public static int facingFromSide(Side side) {
		return switch (side) {
			case BOTTOM -> 0;
			case TOP -> 1;
			case NORTH -> 2;
			case SOUTH -> 3;
			case WEST -> 4;
			default -> 5;
		};
	}

	protected void setDataWithNotify(World world, TilePosc tilePos, int data) {
		world.setBlockDataNotify(tilePos, data);
	}

	public boolean clientCheckIfPowered(WorldSource worldSource, TilePosc tilePos) {
		int axis = getAxisAlignment(worldSource, tilePos);
		TilePos targetPos = new TilePos();

		for (int i = 0; i < 2; i++) {
			int[] offset = FACING_OFFSETS[axleFacingsForAlignment[axis][i]];
			targetPos.set(tilePos.x(), tilePos.y(), tilePos.z());

			for (int distance = 1; distance <= 3; distance++) {
				targetPos.set(targetPos.x() + offset[0], targetPos.y() + offset[1], targetPos.z() + offset[2]);

				Block<?> neighbor = worldSource.getBlockType(targetPos);

				// Keep walking through aligned plain axles.
				if (neighbor == BTXBlocks.AXLE && getAxisAlignment(worldSource, targetPos) == axis) {
					continue;
				}

				if (neighbor == BTXBlocks.AXLE_POWER_SOURCE && getAxisAlignment(worldSource, targetPos) == axis) {
					return true;
				}

				if (MechPowerUtils.isPoweredGearBox(worldSource, targetPos)) {
					return true;
				}

				break; // first non-axle block ends this direction
			}
		}

		return false;
	}

	/** Client power is found by scanning up to 3 blocks, so everything in that range must re-render **/
	private static void markNearbyForRenderUpdate(World world, TilePosc pos) {
		TilePos min = new TilePos();
		TilePos max = new TilePos();
		min.set(pos.x() - 3, pos.y() - 3, pos.z() - 3);
		max.set(pos.x() + 3, pos.y() + 3, pos.z() + 3);
		world.markBlocksDirty(min, max);
	}

	@Override public void clientBlockAdded(World world, TilePosc tilePos) {
		BlockLogicGearBox.markRangeForRenderUpdate(world, tilePos, 3);
	}

	@Override public void clientBlockRemoved(World world, TilePosc tilePos, int oldData) {
		BlockLogicGearBox.markRangeForRenderUpdate(world, tilePos, 3);
	}

	@Override
	public void clientNotificationOfDataChange(World world, TilePosc pos, int oldData, int newData) {

	}
}
