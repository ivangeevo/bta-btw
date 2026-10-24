package ivangeevo.btx.block.blocks;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicRailDetector;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.entity.vehicle.EntityMinecart;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBd;

import java.util.Random;

public class BlockLogicRailDetectorTiered extends BlockLogicRailDetector {
	public static final int MASK_POWERED = 8;
	private static final float INSET = 0.125F;

	public enum Tier {
		STONE,
		SOULFORGED_STEEL
	}

	private final Tier tier;

	public BlockLogicRailDetectorTiered(@NotNull Block<?> block, Tier tier) {
		super(block);
		this.tier = tier;
	}

	public static BlockLogicSupplier<BlockLogicRailDetectorTiered> properties(Tier tier) {
		return block -> new BlockLogicRailDetectorTiered(
			block
				.withSound(BlockSounds.METAL)
				.withHardness(0.7F)
				.withTags(BlockTags.BROKEN_BY_FLUIDS, BlockTags.MINEABLE_BY_PICKAXE, BlockTags.PREVENT_MOB_SPAWNS),
			tier
		);
	}

	@Override
	public void onEntityCollision(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Entity entity) {
		if (!world.isClientSide) {
			int meta = world.getBlockData(tilePos);

			if ((meta & MASK_POWERED) == 0) {
				checkForMinecart(world, tilePos, meta);
			}
		}
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		if (!world.isClientSide && !isRandomTick) {
			int meta = world.getBlockData(tilePos);

			if ((meta & MASK_POWERED) != 0) {
				checkForMinecart(world, tilePos, meta);
			}
		}
	}

	private void checkForMinecart(World world, TilePosc tilePos, int meta) {
		boolean isPowered = (meta & MASK_POWERED) != 0;
		boolean triggered = false;
		AABBd box = new AABBd(
			tilePos.x() + INSET, tilePos.y() + INSET, tilePos.z() + INSET,
			tilePos.x() + 1 - INSET, tilePos.y() + 1 - INSET, tilePos.z() + 1 - INSET
		);

		for (EntityMinecart cart : world.getEntitiesWithinAABB(EntityMinecart.class, box)) {
			if (doesCartTrigger(cart)) {
				triggered = true;
				break;
			}
		}

		if (triggered != isPowered) {
			world.setBlockData(tilePos, triggered ? meta | MASK_POWERED : meta & ~MASK_POWERED);
			world.markBlockNeedsUpdate(tilePos);
			world.notifyBlocksInCapsuleOfNeighborChange(Direction.DOWN, tilePos, this.block);
			world.markBlockDirty(tilePos);
		}

		if (triggered) {
			world.scheduleBlockUpdate(tilePos, this.block, tickDelay());
		}
	}

	private boolean doesCartTrigger(EntityMinecart cart) {
		Entity passenger = cart.getPassenger();

		return switch (this.tier) {
			case STONE -> cart.getType() != EntityMinecart.PASSENGER_CART || passenger != null;
			case SOULFORGED_STEEL -> passenger instanceof Player;
		};
	}
}