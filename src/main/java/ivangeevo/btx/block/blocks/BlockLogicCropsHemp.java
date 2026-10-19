package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.BTXItems;
import net.minecraft.core.block.*;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.sound.BlockSounds;
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

public class BlockLogicCropsHemp extends BlockLogicFlower {
	public static final int MAX_GROWTH_STATE = 7;
	private static final double HALF_WIDTH = 0.2;

	public BlockLogicCropsHemp(@NotNull Block<?> block) {
		super(block);
		float f = 0.2f;
		this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 1.0F, 0.5F + f);
	}

	public static BlockLogicSupplier<BlockLogicCropsHemp> properties() {
		return block -> new BlockLogicCropsHemp(
			block
				.setTicking(true)
				.withSound(BlockSounds.GRASS)
				.withHardness(0.0f)
				.setStatParent(() -> BTXItems.HEMP_SEEDS)
				.withTags(BlockTags.BROKEN_BY_FLUIDS, BlockTags.NOT_IN_CREATIVE_MENU)
		);
	}

	// Grows taller with each stage, like CE's HempCropBlock. Tops are placed fully grown, so they're always full height.
	@Override
	public @NotNull AABBdc getBoundsFromState(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		int growthLevel = Math.min(source.getBlockData(tilePos) & 7, MAX_GROWTH_STATE);
		double height = (1 + growthLevel) / 8.0;
		return new AABBd(0.5 - HALF_WIDTH, 0.0, 0.5 - HALF_WIDTH, 0.5 + HALF_WIDTH, height, 0.5 + HALF_WIDTH);
	}

	// Can be placed on a farmland, a soil planter or on top of itself (2 tall plant)
	@Override
	protected boolean mayPlaceOn(@NotNull Block<?> block) {
		return isSoil(block) || block == BTXBlocks.HEMP;
	}

	private static boolean isSoil(Block<?> block) {
		return block == Blocks.FARMLAND_DIRT || block == BTXBlocks.PLANTER_SOIL;
	}

	// A soil planter never needs water (BTW 2.94)
	private static boolean isHydratedSoil(Block<?> block, int data) {
		return block == BTXBlocks.PLANTER_SOIL || block == Blocks.FARMLAND_DIRT && BlockLogicFarmland.isWet(data);
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		super.updateTick(world, tilePos, rand, isRandomTick);
		if (isBright(world, tilePos, 0) || isBright(world, tilePos, 1) || isBright(world, tilePos, 2)) {
			TilePos tileBelow = tilePos.down(new TilePos());
			Block<?> blockBelow = world.getBlockType(tileBelow);

			int dataBelow = world.getBlockData(tileBelow);

			if (isHydratedSoil(blockBelow, dataBelow)) {
				int growthLevel = world.getBlockData(tilePos);
				if (growthLevel < MAX_GROWTH_STATE) {
					// fertilized soil grows about 1.5x faster, like BTA wheat
					int growthChance = BlockLogicFarmland.isFertilized(dataBelow) ? 13 : 20;
					if (rand.nextInt(growthChance) == 0) {
						world.setBlockDataNotify(tilePos, ++growthLevel);
					}
				} else {
					for (int targetY = tilePos.y() + 1; targetY < tilePos.y() + 2; targetY++) {
						TilePos targetPos = new TilePos(tilePos.x(), targetY, tilePos.z());
						Block<?> targetBlock = world.getBlockType(targetPos);
						if (targetBlock != this.block) {
							if (world.isAirBlock(targetPos) && rand.nextInt(60) == 0) {
								world.setBlockTypeDataNotify(targetPos, this.block, 7);
							}
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public void onDestroyedByPlayer(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side, int data, @NotNull Player player, @Nullable Item item) {
		if (world.isClientSide) return;

		// Breaking the top or bottom part de-fertilizes the farmland
		TilePos pos = tilePos.down(new TilePos());

		while (world.getBlockType(pos) == this.block) {
			pos = pos.down(new TilePos());
		}

		if (!isSoil(world.getBlockType(pos))) return;

		int farmlandData = world.getBlockData(pos);
		if (BlockLogicFarmland.isFertilized(farmlandData) && world.rand.nextInt(3) == 2) {
			world.setBlockDataNotify(pos, BlockLogicFarmland.setFertilized(farmlandData, false));
		}
	}

	@Override
	public @NotNull ItemStack @Nullable [] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		if (dropCause == EnumDropCause.PICK_BLOCK) {
			return new ItemStack[]{new ItemStack(BTXItems.HEMP_SEEDS)};
		}

		if (data == MAX_GROWTH_STATE) {
			return new ItemStack[]{new ItemStack(BTXItems.HEMP)};
		}

		return new ItemStack[0];
	}

	@Override
	public void dropWithCause(@NotNull World world, @NotNull EnumDropCause dropCause, @NotNull TilePosc tilePos, int data, @Nullable TileEntity tileEntity, @Nullable Player player) {
		super.dropWithCause(world, dropCause, tilePos, data, tileEntity, player);

		if (world.isClientSide) return;

		boolean isBottom = isSoil(world.getBlockType(tilePos.down(new TilePos())));
		if (!isBottom && world.rand.nextInt(100) < 50) {
			world.dropItem(tilePos, new ItemStack(BTXItems.HEMP_SEEDS));
		}
	}

	// Helper method to check the brightness required at a specific position
	private boolean isBright(World world, TilePosc pos, int offsetY) {
		return world.getFullBlockLightValue(pos.add(0, offsetY, 0, new TilePos())) >= 15;
	}
}
