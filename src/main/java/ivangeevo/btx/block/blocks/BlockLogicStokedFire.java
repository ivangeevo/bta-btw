package ivangeevo.btx.block.blocks;

import ivangeevo.btx.BTXGlobals;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.util.FireInteraction;
import ivangeevo.btx.mixin.BlockLogicFireAccessor;
import ivangeevo.btx.util.utils.WorldUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicFire;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.data.gamerule.GameRules;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

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
                .withSetUnbreakable()
                .withLightEmission(1F)
                .withTags(BlockTags.BROKEN_BY_FLUIDS, BlockTags.PLACE_OVERWRITES, BlockTags.NOT_IN_CREATIVE_MENU)
        );
    }

    @Override
    public void initializeBlock() {
        super.initializeBlock();
        setFlammable(this.block, 60, 0);
    }

    @Override
    public HitResult collisionRayTrace(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Vector3dc start, @NotNull Vector3dc end, boolean useSelectionBounds) {
        if (!BTXGlobals.localPlayerInstantBreaks.getAsBoolean()) return null;

        return super.collisionRayTrace(world, tilePos, start, end, useSelectionBounds);
    }

    @Override
    public void onDestroyedByPlayer(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side, int data, @NotNull Player player, @Nullable Item item) {
        super.onDestroyedByPlayer(world, tilePos, side, data, player, item);

        TilePos above = tilePos.up(new TilePos());
        if (world.getBlockType(above) == Blocks.FIRE) {
            world.setBlockTypeNotify(above, Blocks.AIR);
        }
    }

    @Override
    public int tickDelay() {
        return TICK_RATE;
    }

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
    }

    @Override
    public boolean doesInfiniteBurnToFacing(World world, TilePosc tilePos, Direction side) {
        return side == Direction.UP;
    }

	@Override
	protected Block<?> getSpreadFireBlock() {
		return Blocks.FIRE;
	}

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

    public void tryToDestroyBlockWithFire(World world, int x, int y, int z, int chanceToDestroy, Random random, int sourceMetadata) {
        TilePos pos = new TilePos(x, y, z);
        int abilityToCatchFire = BlockLogicFireAccessor.btx$getBurnChance()[world.getBlockType(pos).id()];

        if (random.nextInt(chanceToDestroy) < abilityToCatchFire) {
            if (world.getGameRuleValue(GameRules.DO_FIRE_BURN)) {
                onBlockDestroyedByFire(world, pos, sourceMetadata, random);
            }
        }
    }

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

    private static boolean isHighHumidity(World world, TilePosc pos) {
        Biome biome = world.getBlockBiome(pos);
        return biome.defaultHumidity > 0.85F;
    }

    public void checkForFireSpreadFromLocation(World world, TilePosc pos, Random rand, int sourceFireAge) {
        if (!world.getGameRuleValue(GameRules.DO_FIRE_BURN)) return;

        boolean highHumidity = isHighHumidity(world, pos);

        for (int i = pos.x() - 1; i <= pos.x() + 1; ++i) {
            for (int k = pos.z() - 1; k <= pos.z() + 1; ++k) {
                for (int j = pos.y() - 1; j <= pos.y() + 4; ++j) {
                    if (i != pos.x() || j != pos.y() || k != pos.z()) {
                        int spreadTopBound = 100;
                        if (j > pos.y() + 1) {
                            spreadTopBound += (j - (pos.y() + 1)) * 100;
                        }
                        checkForFireSpreadToOneBlockLocation(world, new TilePos(i, j, k), rand, sourceFireAge, highHumidity, spreadTopBound);
                    }
                }
            }
        }
    }

    private void checkForFireSpreadToOneBlockLocation(World world, TilePos pos, Random rand, int sourceFireAge, boolean highHumidity, int spreadTopBound) {
        int neighborChance = getChanceOfNeighborsEncouragingFireCustom(world, pos);
        if (neighborChance <= 0) return;

        int spreadChance = (neighborChance + 61) / (sourceFireAge + 30);

        if (highHumidity) {
            spreadChance /= 2;
        }

        TilePos queryPos = new TilePos();
        if (spreadChance > 0 && rand.nextInt(spreadTopBound) <= spreadChance
            && !world.isBlockBeingRainedOn(pos)
            && !world.isBlockBeingRainedOn(pos.west(queryPos))
            && !world.isBlockBeingRainedOn(pos.east(queryPos))
            && !world.isBlockBeingRainedOn(pos.north(queryPos))
            && !world.isBlockBeingRainedOn(pos.south(queryPos))) {
            int startMetadata = Math.min(sourceFireAge + rand.nextInt(5) / 4, 15);
            world.setBlockTypeDataNotify(pos, this.getSpreadFireBlock(), startMetadata);
        }
    }

    protected int getChanceOfNeighborsEncouragingFireCustom(World world, TilePosc pos) {
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
