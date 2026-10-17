package ivangeevo.btx.block.blocks.kiln;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.kiln.RecipeEntryKiln;
import ivangeevo.btx.util.utils.MiscUtils;
import ivangeevo.btx.util.utils.WorldUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BlockLogicKiln extends BlockLogic {
	private static final int COOK_STATE_COUNT_TO_COOK = 130;
	private static final int TICKS_PER_STATE_UPDATE = 20;
	private static final int PRIMARY_FIRE_FACTOR = 5;
	private static final int SECONDARY_FIRE_FACTOR = 1;
	private static final int COOK_STEPS = 16; // counter 0..15, the 16th tick cooks

	public BlockLogicKiln(@NotNull Block<?> block) {
        super(block, Materials.STONE);
    }

    public static BlockLogicSupplier<BlockLogicKiln> properties() {
        return block -> new BlockLogicKiln(
            block
                .setTicking(true)
                .withSound(BlockSounds.STONE)
                .withHardness(2.0F)
                .withBlastResistance(10.0F)
                .withTags(BlockTags.MINEABLE_BY_PICKAXE)
        );
    }

    @Override
    public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
        super.onPlacedByWorld(world, tilePos);
        if (canBlockBeCooked(world, above(tilePos))) {
            scheduleUpdateBasedOnCookState(world, tilePos);
        }
    }

    @Override
    public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
        if (isRandomTick) {
            // verify we have a tick already scheduled to prevent jams on chunk load
            if (!WorldUtils.isUpdateScheduled(world, tilePos, this.block)
                && canBlockBeCooked(world, above(tilePos))) {
                scheduleUpdateBasedOnCookState(world, tilePos);
            }
            return;
        }

        int oldCookCounter = getCookCounter(world, tilePos);
        int newCookCounter = 0;

        TilePos abovePos = above(tilePos);
        if (canBlockBeCooked(world, abovePos)) {
            if (checkKilnIntegrity(world, tilePos)) {
                if (oldCookCounter >= 15) {
                    cookBlock(world, abovePos);
                } else {
                    newCookCounter = oldCookCounter + 1;
                    scheduleUpdateBasedOnCookState(world, tilePos);
                }
            } else {
                // if we have a valid cook block above, we have to reschedule another tick
                // regardless of other factors, because the shape of the kiln can change without
                // an immediate neighbor changing, causing the cook process to restart
                scheduleUpdateBasedOnCookState(world, tilePos);
            }
        }

        if (oldCookCounter != newCookCounter) {
            setCookCounter(world, tilePos, newCookCounter);
        }
    }

    @Override
    public @NotNull ItemStack @Nullable [] getBreakResult(
		@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity
	) {
		return dropCause != EnumDropCause.IMPROPER_TOOL ? new ItemStack[]{new ItemStack(Blocks.BRICK_CLAY)} : null;
    }

    @Override
    public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
        TilePos below = new TilePos(tilePos.x(), tilePos.y() - 1, tilePos.z());

        if (world.getBlockType(below) != BTXBlocks.STOKED_FIRE) {
            // we don't have a stoked fire beneath us, so revert to regular brick
            world.setBlockTypeNotify(tilePos, Blocks.BRICK_CLAY);
        } else if (canBlockBeCooked(world, above(tilePos))) {
            if (!WorldUtils.isUpdateScheduled(world, tilePos, this.block)
                && !WorldUtils.isUpdatePendingThisTick(world, tilePos, this.block)) {
                scheduleUpdateBasedOnCookState(world, tilePos);
            }
        } else if (getCookCounter(world, tilePos) > 0) {
            // reset the cook counter so it doesn't get passed to another block on piston push
            setCookCounterNoNotify(world, tilePos, 0);
        }
    }

    // TODO: BTW hasMortar(blockAccess, i, j, k) returns true. Port it with whatever your
    // siding/moulding/corner mortar logic uses.

    //------------- Class Specific Methods ------------//

    public int getCookCounter(int data) {
        return data;
    }

    public int getCookCounter(WorldSource source, TilePosc tilePos) {
        return getCookCounter(source.getBlockData(tilePos));
    }

    public int setCookCounter(int data, int counter) {
        return counter;
    }

    public void setCookCounter(World world, TilePosc tilePos, int iCounter) {
        int iMetadata = setCookCounter(world.getBlockData(tilePos), iCounter);
        world.setBlockDataNotify(tilePos, iMetadata);
    }

    public void setCookCounterNoNotify(World world, TilePosc tilPos, int counter) {
        int iMetadata = setCookCounter(world.getBlockData(tilPos), counter);
        world.setBlockData(tilPos, iMetadata);
    }

    protected void scheduleUpdateBasedOnCookState(World world, TilePosc tilePos) {
        int iTickRate = computeTickRateBasedOnFireFactor(world, tilePos);

		TilePos abovePos = tilePos.up(new TilePos());
		RecipeEntryKiln recipe = BTXRecipes.KILN.findRecipe(world.getBlockType(abovePos));
		if (recipe != null) {
			iTickRate *= recipe.getCookTimeMultiplier();
		}

        world.scheduleBlockUpdate(tilePos, this.block, iTickRate);
    }

    private boolean canBlockBeCooked(WorldSource source, TilePosc tilePos) {
		return BTXRecipes.KILN.canCook(source.getBlockType(tilePos));
	}

	private void cookBlock(World world, TilePosc pos) {
		RecipeEntryKiln recipe = BTXRecipes.KILN.findRecipe(world, pos);
		if (recipe == null) return;

		world.setBlockTypeNotify(pos, Blocks.AIR);
		for (ItemStack stack : recipe.getOutputs()) {
			MiscUtils.ejectStackWithRandomOffset(world, pos, stack.copy());
		}
	}

    private boolean checkKilnIntegrity(WorldSource source, TilePosc tilePos) {
        int brickCount = 0;

        for (int tempFacing = 1; tempFacing <= 5; tempFacing++) {
            TilePos tempPos = new TilePos(tilePos.x(), tilePos.y() + 1, tilePos.z());
            tempPos.add(Direction.fromId(tempFacing));

            Block<?> tempBlock = source.getBlockType(tempPos);
            if (tempBlock == Blocks.BRICK_CLAY || tempBlock == BTXBlocks.KILN) {
                brickCount++;
                if (brickCount >= 3) {
                    return true;
                }
            }
        }
        return false;
    }

	private int computeTickRateBasedOnFireFactor(WorldSource source, TilePosc tilePos) {
		// the kiln only exists above a stoked fire (onNeighborChanged reverts it otherwise), so the primary fire is always there
		int fireFactor = PRIMARY_FIRE_FACTOR;

		for (int xOffset = -1; xOffset <= 1; xOffset++) {
			for (int zOffset = -1; zOffset <= 1; zOffset++) {
				if (xOffset != 0 || zOffset != 0) {
					TilePos queryPos = new TilePos(tilePos.x() + xOffset, tilePos.y() - 1, tilePos.z() + zOffset);
					if (source.getBlockType(queryPos) == BTXBlocks.STOKED_FIRE) {
						fireFactor += SECONDARY_FIRE_FACTOR;
					}
				}
			}
		}

		// total time = 130 / fireFactor seconds, split over COOK_STEPS ticks
		return Math.max(1, Math.round(
			(float) (COOK_STATE_COUNT_TO_COOK * TICKS_PER_STATE_UPDATE) / (COOK_STEPS * fireFactor)));
	}

    private static TilePos above(TilePosc tilePos) {
        return new TilePos(tilePos.x(), tilePos.y() + 1, tilePos.z());
    }

    //----------- Client Side Functionality -----------//

    @Override
    public void animationTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand) {
        TilePos abovePos = above(tilePos);
        Block<?> blockAbove = world.getBlockType(abovePos);

        if (BTXRecipes.KILN.canCook(blockAbove) && checkKilnIntegrity(world, tilePos)) {
            if (!blockAbove.getLogic().isSolidRender()) {
                for (int tempCount = 0; tempCount < 2; tempCount++) {
                    double xPos = tilePos.x() + rand.nextDouble();
                    double yPos = tilePos.y() + 1D + (rand.nextDouble() * 0.75D);
                    double zPos = tilePos.z() + rand.nextDouble();
                    world.spawnParticle("fcwhitesmoke", xPos, yPos, zPos, 0.0, 0.0, 0.0, 0, false);
                }
            } else {
                for (int tempFacing = 2; tempFacing < 6; tempFacing++) {
                    double xPos = tilePos.x() + 0.5D;
                    double yPos = tilePos.y() + 1D + (rand.nextDouble() * 0.75D);
                    double zPos = tilePos.z() + 0.5D;

                    double facingOffset = 0.75D;
                    double horizontalOffset = -0.75D + (rand.nextDouble() * 1.5D);

                    if (tempFacing == 2) { // negative z
                        xPos += horizontalOffset;
                        zPos -= facingOffset;
                    } else if (tempFacing == 3) { // positive z
                        xPos += horizontalOffset;
                        zPos += facingOffset;
                    } else if (tempFacing == 4) { // negative x
                        xPos -= facingOffset;
                        zPos += horizontalOffset;
                    } else { // positive x
                        xPos += facingOffset;
                        zPos += horizontalOffset;
                    }

                    world.spawnParticle("fcwhitesmoke", xPos, yPos, zPos, 0.0, 0.0, 0.0, 0, false);
                }
            }
        }
    }

	public int getCookStage(WorldSource source, TilePosc tilePos) {
		if (!canBlockBeCooked(source, above(tilePos))) return -1;
		int stage = getCookCounter(source, tilePos) / 2 - 1;
		return stage >= 0 && stage <= 6 ? stage : -1;
	}
}
