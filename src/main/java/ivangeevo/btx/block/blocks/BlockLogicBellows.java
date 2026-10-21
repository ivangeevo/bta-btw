package ivangeevo.btx.block.blocks;

import ivangeevo.btx.sound.BTXSoundTypes;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.util.utils.MechPowerUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import ivangeevo.btx.util.utils.WorldUtils;
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
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

import java.util.List;
import java.util.Random;

public class BlockLogicBellows extends BlockLogic implements MechanicalDevice {
	private static final int BELLOWS_TICK_RATE = 35;
	public static final double BELLOWS_CONTRACTED_HEIGHT = 0.6875D;

	public BlockLogicBellows(@NotNull Block<?> block) {
		super(block, Materials.WOOD);
	}

	public static BlockLogicSupplier<BlockLogicBellows> properties() {
		return block -> new BlockLogicBellows(
			block
				.setTicking(true)
				.withSound(BlockSounds.WOOD)
				.withHardness(2.0F)
				.withTags(BlockTags.MINEABLE_BY_AXE)
		);
	}

	@Override
	public int tickDelay() {
		return BELLOWS_TICK_RATE;
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
	public @NotNull AABBdc getBoundsFromState(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return new AABBd(0, 0, 0, 1, isBlockMechanicalOn(source, tilePos) ? BELLOWS_CONTRACTED_HEIGHT : 1.0D, 1);
	}

	@Override
	public @NotNull ItemStack @Nullable [] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		return switch (dropCause) {
			case PICK_BLOCK, EXPLOSION, PROPER_TOOL, SILK_TOUCH -> new ItemStack[]{new ItemStack(this.block)}; // meta 0: no facing/on state
			default -> null;
		};
	}

	//------------- Placement -------------//

	@Override
	public int getPlacedData(@Nullable Player player, @NotNull ItemStack itemStack, @NotNull World world,
							 @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		int facing = 2;
		if (player != null) {
			int placing = MathHelper.floor(player.yRot * 4.0F / 360.0F + 0.5D) & 3; // 0:+z 1:-x 2:-z 3:+x
			facing = switch (placing) {
				case 0 -> 2;
				case 1 -> 5;
				case 2 -> 3;
				default -> 4;
			};
		}

		return setFacing(0, facing);
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		super.onPlacedByWorld(world, tilePos);
		world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
	}

	// The guard only avoids queueing while already valid or mid-tick.
	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		boolean updateScheduled = WorldUtils.isUpdateScheduled(world, tilePos, this.block);

		if (!updateScheduled) {
			if (!isCurrentStateValid(world, tilePos)
				&& !WorldUtils.isUpdatePendingThisTick(world, tilePos, this.block)) {
				world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
				setContinuousMechanicalStateChange(world, tilePos, true);
			}
		} else if (isContinuousMechanicalStateChange(world, tilePos)
			&& isCurrentStateValid(world, tilePos)) {
			// power flipped back while we were waiting
			setContinuousMechanicalStateChange(world, tilePos, false);
		}
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		if (isRandomTick) {
			// rescue a bellows stuck in a wrong state after chunk load
			if (!isCurrentStateValid(world, tilePos) && !WorldUtils.isUpdateScheduled(world, tilePos, this.block)) {
				world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
				setContinuousMechanicalStateChange(world, tilePos, true);
			}

			return;
		}

		boolean receiving = isInputtingMechanicalPower(world, tilePos);
		boolean on = isBlockMechanicalOn(world, tilePos);
		boolean continuous = isContinuousMechanicalStateChange(world, tilePos);

		if (on != receiving) {
			if (continuous) {
				setContinuousMechanicalStateChange(world, tilePos, false);
				setBlockMechanicalOn(world, tilePos, receiving);

				if (receiving) {
					blow(world, tilePos);
					SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.BELLOWS_BLOW, 0.25F, world.rand.nextFloat() * 0.4F + 2.0F);
				} else {
					liftCollidingEntities(world, tilePos);
					SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.BELLOWS_RETRACT, 1.0F, world.rand.nextFloat() * 0.4F + 2.0F);
				}
			} else {
				// power flow was interrupted during the transition: check again later
				world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
				setContinuousMechanicalStateChange(world, tilePos, true);
			}
		} else if (continuous) {
			setContinuousMechanicalStateChange(world, tilePos, false);
		}
	}

	//------------- MechanicalDevice -------------//

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
		return MechPowerUtils.isBlockPoweredByAxle(world, tilePos, this) ||
			   MechPowerUtils.isBlockPoweredByHandCrank(world, tilePos);
	}

	@Override
	public boolean canInputAxlePowerToFacing(World world, TilePosc tilePos, Direction facing) {
		Direction front = getFrontDirection(world.getBlockData(tilePos));
		return facing != front && facing != Direction.UP;
	}

	@Override
	public boolean isOutputtingMechanicalPower(World world, TilePosc tilePos) {
		return false;
	}

	//------------- Class Specific Methods -------------//

	public boolean isContinuousMechanicalStateChange(WorldSource source, TilePosc tilePos) {
		return (source.getBlockData(tilePos) & 8) > 0;
	}

	public void setContinuousMechanicalStateChange(World world, TilePosc tilePos, boolean continuous) {
		int data = world.getBlockData(tilePos) & ~8;

		if (continuous) data |= 8;

		world.setBlockData(tilePos, data);
	}

	public Direction getFrontDirection(int metadata) {
		return Direction.fromId(getFacing(metadata));
	}

	public int getFacing(int metadata) {
		return (metadata & 3) + 2;
	}

	public int setFacing(int metadata, int facing) {
		metadata &= ~3;
		facing = facing >= 2 ? facing - 2 : 0; // only 4 valid facings
		return metadata | facing;
	}

	public boolean isBlockMechanicalOn(WorldSource source, TilePosc tilePos) {
		return (source.getBlockData(tilePos) & 4) > 0;
	}

	public void setBlockMechanicalOn(World world, TilePosc tilePos, boolean on) {
		int metadata = world.getBlockData(tilePos) & ~4;
		if (on) {
			metadata |= 4;
		}

		world.setBlockDataNotify(tilePos, metadata);
	}

	public boolean isCurrentStateValid(World world, TilePosc tilePos) {
		return isInputtingMechanicalPower(world, tilePos) == isBlockMechanicalOn(world, tilePos);
	}

	private void blow(World world, TilePosc tilePos) {
		int data = world.getBlockData(tilePos);
		Direction front = getFrontDirection(data);
		int facing = getFacing(data);
		// the two horizontal sides
		Direction side1 = Direction.fromId(facing <= 3 ? 4 : 2);
		Direction side2 = Direction.fromId(facing <= 3 ? 5 : 3);

		TilePos particlePos = new TilePos(tilePos);
		particlePos.add(front);

		emitBellowsParticles(world, particlePos, world.rand);

		TilePos target = new TilePos(tilePos);
		for (int count = 0; count < 3; count++) {
			target.add(front);

			Block<?> targetBlock = world.getBlockType(target);
			if (targetBlock == Blocks.FIRE || targetBlock == BTXBlocks.STOKED_FIRE) {
				stokeFire(world, target);
			} else if (!world.isAirBlock(target)) {
				break; // line of sight to the next fire is blocked
			}

			TilePos sidePos1 = new TilePos(target);
			sidePos1.add(side1);
			Block<?> sideBlock1 = world.getBlockType(sidePos1);

			if (sideBlock1 == Blocks.FIRE || sideBlock1 == BTXBlocks.STOKED_FIRE) {
				stokeFire(world, sidePos1);
			}

			TilePos sidePos2 = new TilePos(target);
			sidePos2.add(side2);

			Block<?> sideBlock2 = world.getBlockType(sidePos2);

			if (sideBlock2 == Blocks.FIRE || sideBlock2 == BTXBlocks.STOKED_FIRE) {
				stokeFire(world, sidePos2);
			}
		}
	}

	private void stokeFire(World world, TilePosc pos) {
		TilePos below = new TilePos(pos.x(), pos.y() - 1, pos.z());
		TilePos above = new TilePos(pos.x(), pos.y() + 1, pos.z());

		if (world.getBlockType(below) == BTXBlocks.HIBACHI) {
			if (world.getBlockType(pos) == BTXBlocks.STOKED_FIRE) {
				// reset the stoked fire's state to keep it going
				world.setBlockData(pos, 0);
			} else {
				world.setBlockTypeNotify(pos, BTXBlocks.STOKED_FIRE);
				// BTA may not call onPlacedByWorld for setBlockTypeNotify, so make sure it ticks
				world.scheduleBlockUpdate(pos, BTXBlocks.STOKED_FIRE, BTXBlocks.STOKED_FIRE.getLogic().tickDelay());
			}

			// put regular fire above the stoked
			if (world.isAirBlock(above)) {
				world.setBlockTypeNotify(above, Blocks.FIRE);
			}
		} else {
			// we don't have a hibachi below, extinguish the fire
			world.setBlockTypeNotify(pos, Blocks.AIR);
		}
	}

	private void liftCollidingEntities(World world, TilePosc tilePos) {
		AABBd area = new AABBd(
			tilePos.x(), tilePos.y() + BELLOWS_CONTRACTED_HEIGHT, tilePos.z(),
			tilePos.x() + 1, tilePos.y() + 1, tilePos.z() + 1);
		double extendedMaxY = tilePos.y() + 1;

		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, area);
		for (Entity entity : list) {
			if (!entity.removed && (entity.isPushable() || entity instanceof EntityItem)) {
				double minY = entity.bb.minY();
				if (minY < extendedMaxY) {
					entity.setPos(entity.x, entity.y + (extendedMaxY - minY), entity.z);
				}
			}
		}
	}

	private void emitBellowsParticles(World world, TilePosc pos, Random random) {
		for (int counter = 0; counter < 10; counter++) {
			double smokeX = pos.x() + random.nextFloat();
			double smokeY = pos.y() + random.nextFloat() * 0.5F;
			double smokeZ = pos.z() + random.nextFloat();
			world.spawnParticle("smoke", smokeX, smokeY, smokeZ, 0.0, 0.0, 0.0, 0, false);
		}
	}
}
