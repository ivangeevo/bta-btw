package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.util.FireInteraction;
import ivangeevo.btx.sound.BTXSoundTypes;
import ivangeevo.btx.util.utils.FireUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import ivangeevo.btx.util.utils.WorldUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BlockLogicHibachi extends BlockLogic implements FireInteraction {
	private final static int TICK_RATE = 4;

	public BlockLogicHibachi(@NotNull Block<?> block) {
		super(block, Materials.STONE);
	}

	public static BlockLogicSupplier<BlockLogicHibachi> properties() {
		return block -> new BlockLogicHibachi(
			block
				.setTicking(true)
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
	public void onChunkLoad(@NotNull World world, @NotNull TilePosc tilePos) {
		if (!isCurrentStateValid(world, tilePos)) {
			// Reset the data to prevent stuck states if re-logging
			// BTW: CE handles this with the uncommented isUpdateScheduled check in updateTick(),
			// But since BTA doesn't have tick persistence(saving scheduled ticks), it takes around 30-40 seconds for it to kick in
			//world.setBlockData(tilePos, 0);

			world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
		}
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		if (isRandomTick) {
			if (!isCurrentStateValid(world, tilePos)) {
				// verify we have a tick already scheduled to prevent jams on chunk load
				// Disabled because of the setBlockData change in the onChunkLoad method
				if (!WorldUtils.isUpdateScheduled(world, tilePos, this.block)) {
					world.scheduleBlockUpdate(tilePos, this.block, tickDelay());
				}
			}
		}

		boolean powered = isGettingPowered(world, tilePos);

		if (powered) {
			if (!isLit(world, tilePos)) {
				ignite(world, tilePos);
			} else {
				// make sure the fire hasn't gone out from other sources, or that the block above hasn't become vacated,
				// if we're supposed to be lit
				TilePos posAbove = tilePos.up(new TilePos());
				Block<?> blockAbove = world.getBlockType(posAbove);

				if (blockAbove != Blocks.FIRE && blockAbove != BTXBlocks.STOKED_FIRE) {
					if (shouldIgniteAbove(world, tilePos)) {
						float pitch = world.rand.nextFloat() * 0.4F + 0.8F;
						SoundUtils.playAtTileCenter(
							world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.HIBACHI_IGNITE, 1F, pitch
						);

						world.setBlockTypeNotify(posAbove, Blocks.FIRE);
					}
				}
			}
		} else {
			if (isLit(world, tilePos)) {
				extinguish(world, tilePos);
			} else {
				TilePos posAbove = tilePos.up(new TilePos());
				Block<?> blockAbove = world.getBlockType(posAbove);

				if (blockAbove == Blocks.FIRE || blockAbove == BTXBlocks.STOKED_FIRE) {
					// we've got a fire burning above an unlit, unpowered BBQ.
					// It has probably been lit by the player, or spread from elsewhere, so
					// put it out.
					world.setBlockTypeNotify(posAbove, Blocks.AIR);
				}
			}
		}
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		if (!isCurrentStateValid(world, tilePos) &&
			!WorldUtils.isUpdatePendingThisTick(world, tilePos, block))
		{
			world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
		}
	}

	@Override
	public boolean doesExtinguishFireAbove(World world, TilePosc pos) {
		return !isLit(world, pos);
	}

	@Override
	public boolean doesInfiniteBurnToFacing(World world, TilePosc pos, Direction side) {
		return side == Direction.UP && isLit(world, pos);
	}

	public boolean isLit(WorldSource source, TilePosc tilePos) {
		int data = source.getBlockData(tilePos);
		return (data & 4) > 0;
	}

	private void setLitFlag(World world, TilePosc tilePos) {
		int data = world.getBlockData(tilePos);
		world.setBlockDataNotify(tilePos, (data | 4));
	}

	private void clearLitFlag(World world, TilePosc tilePos) {
		int data = world.getBlockData(tilePos);
		world.setBlockDataNotify(tilePos, (data & (~4)));
	}

	private boolean isGettingPowered(World world, TilePosc tilePos) {
		TilePos queryPos = new TilePos();
		return world.hasDirectSignal(tilePos) || world.hasDirectSignal(tilePos.up(queryPos));
	}

	private boolean canIncinerateBlock(World world, TilePosc tilePos) {
		Block<?> target = world.getBlockType(tilePos);
		return target == Blocks.AIR || FireUtils.canBlockBeIncinerated(world, tilePos);
	}

	private boolean shouldIgniteAbove(World world, TilePosc pos) {
		TilePos above = new TilePos(pos.x(), pos.y() + 1, pos.z());
		return world.isAirBlock(above) || canIncinerateBlock(world, above);
	}

	private void ignite(World world, TilePosc tilePos) {
		setLitFlag(world, tilePos);

		float pitch = world.rand.nextFloat() * 0.4F + 1F;
		SoundUtils.playAtTileCenter(
			world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.HIBACHI_IGNITE, 1F, pitch
		);

		if (shouldIgniteAbove(world, tilePos)) {
			world.setBlockTypeNotify(tilePos.up(new TilePos()), Blocks.FIRE);
		}
	}

	private void extinguish(World world, TilePosc tilePos) {
		clearLitFlag(world, tilePos);

		float pitch = 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F;
		SoundUtils.playAtTileCenter(
			world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.HIBACHI_EXTINGUISH, 0.5F, pitch
		);

		// check if there is an actual fire block above the BBQ

		TilePos queryPos = new TilePos();
		Block<?> blockAbove = world.getBlockType(tilePos.up(queryPos));

		if (blockAbove == Blocks.FIRE || blockAbove == BTXBlocks.STOKED_FIRE) {
			// notify the fire to extinguish
			world.setBlockTypeNotify(tilePos.up(queryPos), Blocks.AIR);
		}
	}

	public boolean isCurrentStateValid(World world, TilePosc tilePos) {
		boolean bPowered = isGettingPowered(world, tilePos);

		if (isLit(world, tilePos) != bPowered) {
			return false;
		} else if (isLit(world, tilePos)) {
			Block<?> blockAbove = world.getBlockType(tilePos.up(new TilePos()));

			if (blockAbove != Blocks.FIRE && blockAbove != BTXBlocks.STOKED_FIRE) {
				if (shouldIgniteAbove(world, tilePos)) {
					// the bbq is lit, there is no fire above, and the block above is flammable (or air)
					// this requires a state update

					return false;
				}
			}
		}

		return true;
	}

}
