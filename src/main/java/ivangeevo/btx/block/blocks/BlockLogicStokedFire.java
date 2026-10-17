package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.util.FireInteraction;
import ivangeevo.btx.mixin.BlockLogicFireAccessor;
import ivangeevo.btx.util.utils.WorldUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicFire;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.data.gamerule.GameRules;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BlockLogicStokedFire extends BlockLogicFire implements FireInteraction {
    private static final int TICK_RATE = 42;

    public BlockLogicStokedFire(@NotNull Block<?> block) {
        super(block);
    }

    public static BlockLogicSupplier<BlockLogicStokedFire> properties() {
        return block -> new BlockLogicStokedFire(
            block
                .withSound(BlockSounds.WOOD)
                .withHardness(0F)
            // TODO: light value 1.0 (BTW setLightValue(1F)), use whatever your other light-emitting blocks use
        );
    }

    @Override
    public void initializeBlock() {
        super.initializeBlock();
        setFlammable(this.block, 60, 0);
    }

    // BTW: tickRate
    @Override
    public int tickDelay() {
        return TICK_RATE;
    }

    // BTW: onBlockAdded. Override to make burn time more consistent (no portal check).
    @Override
    public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
        world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
    }

    @Override
    public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
        if (isRandomTick) {
            // verify we have a tick already scheduled to prevent jams on chunk load
            if (!WorldUtils.isUpdateScheduled(world, tilePos, this.block)) {
                // reset the countdown and schedule an extra long tick
                // to give any nearby bellows a chance to catch up too
                world.setBlockData(tilePos, 0);
                world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay() * 4L);
            }
            return;
        }

        if (!validateState(world, tilePos)) {
            return;
        }

        TilePos above = new TilePos(tilePos.x(), tilePos.y() + 1, tilePos.z());
        if (world.getBlockType(above) == Blocks.BRICK_CLAY) {
            world.setBlockTypeNotify(above, BTXBlocks.KILN);
        }

        int metaData = world.getBlockData(tilePos);
        if (metaData < 15) {
            metaData += 1;
            world.setBlockData(tilePos, metaData);
        }

        tryToDestroyBlockWithFire(world, tilePos.x() + 1, tilePos.y(), tilePos.z(), 300, rand, 0);
        tryToDestroyBlockWithFire(world, tilePos.x() - 1, tilePos.y(), tilePos.z(), 300, rand, 0);
        tryToDestroyBlockWithFire(world, tilePos.x(), tilePos.y() - 1, tilePos.z(), 250, rand, 0);
        tryToDestroyBlockWithFire(world, tilePos.x(), tilePos.y() + 1, tilePos.z(), 250, rand, 0);
        tryToDestroyBlockWithFire(world, tilePos.x(), tilePos.y(), tilePos.z() - 1, 300, rand, 0);
        tryToDestroyBlockWithFire(world, tilePos.x(), tilePos.y(), tilePos.z() + 1, 300, rand, 0);

        checkForFireSpreadFromLocation(world, tilePos, rand, 0);

        if (metaData >= 3) {
            // revert to regular fire if we've exceeded our life-span
            world.setBlockTypeDataNotify(tilePos, Blocks.FIRE, 0);
        } else {
            world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay() + world.rand.nextInt(10));
        }

        // TODO: AchievementEventDispatcher.triggerEventForNearbyPlayers(StokeFireEvent, ...) (no BTA equivalent seen)
    }

    @Override
    public boolean doesInfiniteBurnToFacing(World world, TilePosc tilePos, Direction side) {
        return side == Direction.UP;
    }

	@Override
	protected Block<?> getSpreadFireBlock() {
		return Blocks.FIRE;
	}

	//------------- Class Specific Methods ------------//

    /** Return true if the Stoked fire block still remains. */
    public boolean validateState(World world, TilePosc tilePos) {
        TilePos below = new TilePos(tilePos.x(), tilePos.y() - 1, tilePos.z());

        if (!canPlaceAt(world, tilePos)) {
            world.setBlockTypeNotify(tilePos, Blocks.AIR);
            return false;
        } else if (world.getBlockType(below) == BTXBlocks.HIBACHI) {
            if (!((BlockLogicHibachi) BTXBlocks.HIBACHI.getLogic()).isLit(world, below)) {
                // we are above an unlit BBQ, extinguish
                world.setBlockTypeNotify(tilePos, Blocks.AIR);
                return false;
            }
        } else {
            // stoked fire can only exist over a Hibachi. Revert to regular fire if we're not over one
            world.setBlockTypeDataNotify(tilePos, Blocks.FIRE, 0);
            return false;
        }

        return true;
    }

    // ---- BTW BlockFire helpers that BTA's BlockLogicFire doesn't have ----

    public void tryToDestroyBlockWithFire(World world, int x, int y, int z, int chanceToDestroy, Random random, int sourceMetadata) {
        TilePos pos = new TilePos(x, y, z);
        int abilityToCatchFire = BlockLogicFireAccessor.btx$getBurnChance()[world.getBlockType(pos).id()];

        if (random.nextInt(chanceToDestroy) < abilityToCatchFire) {
            if (world.getGameRuleValue(GameRules.DO_FIRE_BURN)) {
                onBlockDestroyedByFire(world, pos, sourceMetadata, random);
            }
        }
    }

    /** BTW's default Block.onDestroyedByFire behaviour (from the pre-FCMOD tryToCatchBlockOnFire). */
    protected void onBlockDestroyedByFire(World world, TilePosc pos, int fireAge, Random random) {
        Block<?> target = world.getBlockType(pos);
        boolean isTNT = target == Blocks.TNT;

        if (random.nextInt(fireAge + 10) < 5 && !world.isBlockBeingRainedOn(pos)
            && this.getBurnResult(world, pos) == Blocks.AIR) {
            world.setBlockTypeDataNotify(pos, this.getSpreadFireBlock(), Math.min(fireAge + random.nextInt(5) / 4, 15));
        } else {
            this.setBurnResult(world, pos);
        }
        this.onBlockBurnt(world, pos, random);

        if (isTNT) {
            Blocks.TNT.getLogic().ignite(world, pos, true);
        }
    }

    public void checkForFireSpreadFromLocation(World world, TilePosc pos, Random rand, int sourceFireAge) {
        if (!world.getGameRuleValue(GameRules.DO_FIRE_BURN)) return;

        for (int i = pos.x() - 1; i <= pos.x() + 1; ++i) {
            for (int k = pos.z() - 1; k <= pos.z() + 1; ++k) {
                for (int j = pos.y() - 1; j <= pos.y() + 4; ++j) {
                    if (i != pos.x() || j != pos.y() || k != pos.z()) {
                        int spreadTopBound = 100;
                        if (j > pos.y() + 1) {
                            spreadTopBound += (j - (pos.y() + 1)) * 100;
                        }
                        checkForFireSpreadToOneBlockLocation(world, new TilePos(i, j, k), rand, sourceFireAge, spreadTopBound);
                    }
                }
            }
        }
    }

    private void checkForFireSpreadToOneBlockLocation(World world, TilePos pos, Random rand, int sourceFireAge, int spreadTopBound) {
        int neighborChance = getChanceOfNeighborsEncouragingFireCustom(world, pos);
        if (neighborChance <= 0) return;

        int spreadChance = (neighborChance + 61) / (sourceFireAge + 30); // BTW formula
        // TODO: halve when high humidity (BTW isBlockHighHumidity), if BTA has an equivalent

        TilePos q = new TilePos();
        if (spreadChance > 0 && rand.nextInt(spreadTopBound) <= spreadChance
            && !world.isBlockBeingRainedOn(pos)
            && !world.isBlockBeingRainedOn(pos.west(q))
            && !world.isBlockBeingRainedOn(pos.east(q))
            && !world.isBlockBeingRainedOn(pos.north(q))
            && !world.isBlockBeingRainedOn(pos.south(q))) {
            int startMetadata = Math.min(sourceFireAge + rand.nextInt(5) / 4, 15);
            world.setBlockTypeDataNotify(pos, this.getSpreadFireBlock(), startMetadata);
        }
    }

    protected int getChanceOfNeighborsEncouragingFireCustom(World world, TilePosc pos) {
        // BTW also has a setOnFireDirectly branch for non-replaceable blocks, not ported
        if (!world.isAirBlock(pos)) return 0;

        int chance = 0;
        TilePos q = new TilePos();
        chance = getFlammability(world, pos.east(q), chance);
        chance = getFlammability(world, pos.west(q), chance);
        chance = getFlammability(world, pos.down(q), chance);
        chance = getFlammability(world, pos.up(q), chance);
        chance = getFlammability(world, pos.north(q), chance);
        chance = getFlammability(world, pos.south(q), chance);
        return chance;
    }
}
