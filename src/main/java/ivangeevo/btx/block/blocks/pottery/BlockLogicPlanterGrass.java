package ivangeevo.btx.block.blocks.pottery;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.support.FullSupport;
import net.minecraft.core.block.support.ISupport;
import net.minecraft.core.block.support.PartialSupport;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.IBonemealable;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.biome.Biomes;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BlockLogicPlanterGrass extends BlockLogicPlanter implements IBonemealable {
	public static final int MASK_GROWTH = 3;
	public static final int MAX_GROWTH_STATE = 3;

	/** The grass block this planter holds, and spreads to nearby dirt */
	@NotNull
	public final Block<?> grass;

	protected BlockLogicPlanterGrass(@NotNull Block<?> block, @NotNull Block<?> grass) {
		super(block);
		this.grass = grass;
	}

	public static BlockLogicSupplier<BlockLogicPlanterGrass> grassProperties(@NotNull Block<?> grass) {
		return block -> new BlockLogicPlanterGrass(
			block
				.setTicking(true)
				.withSound(BlockSounds.GLASS)
				.withHardness(0.6F)
				.withTags(BlockTags.MINEABLE_BY_PICKAXE, BlockTags.GROWS_FLOWERS, BlockTags.GROWS_SUGAR_CANE),
			grass
		);
	}

	public static int getGrowthState(int data) {
		return data & MASK_GROWTH;
	}

	public static int setGrowthState(int data, int growthState) {
		return (data & ~MASK_GROWTH) | (growthState & MASK_GROWTH);
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		if (world.isClientSide) return;

		int oldGrowthState = getGrowthState(world.getBlockData(tilePos));
		int newGrowthState = 0;

		TilePos above = tilePos.up(new TilePos());
		if (world.isAirBlock(above) && world.getBlockLightValue(above) >= 8) {
			newGrowthState = oldGrowthState + 1;

			if (newGrowthState > MAX_GROWTH_STATE) {
				newGrowthState = 0;
				spawnPlant(world, above, rand);
			}
		}

		spreadGrass(world, tilePos, rand);

		if (newGrowthState != oldGrowthState) {
			// growth state isn't visible, so no need to notify
			world.setBlockData(tilePos, setGrowthState(world.getBlockData(tilePos), newGrowthState));
		}
	}

	// Same plant roll as BlockLogicGrass.updateTick()
	private static void spawnPlant(@NotNull World world, @NotNull TilePosc pos, @NotNull Random rand) {
		Block<?> plant = null;
		int r = rand.nextInt(400);
		if (r < 26) {
			plant = Blocks.FLOWER_RED;
		} else if (r < 41) {
			plant = Blocks.FLOWER_YELLOW;
		} else if (r < 60) {
			Biome biome = world.getBlockBiome(pos);
			if (biome == Biomes.OVERWORLD_BIRCH_FOREST || biome == Biomes.OVERWORLD_SEASONAL_FOREST) {
				plant = Blocks.FLOWER_PINK;
			} else if (biome == Biomes.OVERWORLD_MEADOW || biome == Biomes.OVERWORLD_BOREAL_FOREST || biome == Biomes.OVERWORLD_SHRUBLAND) {
				plant = Blocks.FLOWER_PURPLE;
			} else if (biome == Biomes.OVERWORLD_FOREST
				|| biome == Biomes.OVERWORLD_SWAMPLAND
				|| biome == Biomes.OVERWORLD_RAINFOREST
				|| biome == Biomes.OVERWORLD_CAATINGA) {
				plant = Blocks.FLOWER_LIGHT_BLUE;
			}
		} else if (rand.nextInt(8) == 0) {
			plant = Blocks.TALLGRASS_FERN;
		} else {
			plant = Blocks.TALLGRASS;
		}

		if (plant != null) {
			world.setBlockTypeNotify(pos, plant);
		}
	}

	// Same conditions as BlockLogicGrass.updateTick
	private void spreadGrass(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand) {
		TilePos queryPos = new TilePos();
		if (world.getBlockLightValue(tilePos.up(queryPos)) < 9) return;

		for (int i = 0; i < 4; i++) {
			int checkX = tilePos.x() + rand.nextInt(3) - 1;
			int checkY = tilePos.y() + rand.nextInt(5) - 3;
			int checkZ = tilePos.z() + rand.nextInt(3) - 1;
			if (world.getBlockType(queryPos.set(checkX, checkY, checkZ)) == Blocks.DIRT
				&& world.getBlockLightValue(queryPos.set(checkX, checkY + 1, checkZ)) >= 4
				&& world.getBlockType(queryPos.set(checkX, checkY + 1, checkZ)).lightBlock() <= 2) {
				world.setBlockTypeNotify(queryPos.set(checkX, checkY, checkZ), this.grass);
			}
		}
	}

	@Override
	public boolean onBonemealUsed(
		@NotNull ItemStack itemStack, @Nullable Player player, @NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit
	) {
		TilePos above = tilePos.up(new TilePos());
		if (!world.isAirBlock(above)) return false;

		if (!world.isClientSide && itemStack.consumeItem(player)) {
			spawnPlant(world, above, world.rand);
			world.setBlockData(tilePos, setGrowthState(world.getBlockData(tilePos), 0));
		}

		return true;
	}

	@Override
	public @NotNull ISupport getSupport(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side) {
		return side == Side.TOP ? PartialSupport.INSTANCE : FullSupport.INSTANCE;
	}

	@Override
	public @NotNull ItemStack @Nullable [] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		return new ItemStack[]{new ItemStack(this.block)};
	}
}
