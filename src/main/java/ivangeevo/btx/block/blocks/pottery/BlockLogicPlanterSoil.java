package ivangeevo.btx.block.blocks.pottery;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicFarmland;
import net.minecraft.core.block.BlockLogicSupplier;
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
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockLogicPlanterSoil extends BlockLogicPlanter implements IBonemealable {
	protected BlockLogicPlanterSoil(@NotNull Block<?> block) {
		super(block);
	}

	public static BlockLogicSupplier<BlockLogicPlanterSoil> soilProperties() {
		return block -> new BlockLogicPlanterSoil(
			block
				.withSound(BlockSounds.GLASS)
				.withHardness(0.6F)
				.withTags(BlockTags.MINEABLE_BY_PICKAXE)
		);
	}

	public static boolean isFertilized(int data) {
		return BlockLogicFarmland.isFertilized(data);
	}

	public static int setFertilized(int data, boolean fertilized) {
		return BlockLogicFarmland.setFertilized(data, fertilized);
	}

	@Override
	public boolean onBonemealUsed(
		@NotNull ItemStack itemStack, @Nullable Player player, @NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit
	) {
		int data = world.getBlockData(tilePos);
		if (!isFertilized(data) && itemStack.consumeItem(player)) {
			world.setBlockDataNotify(tilePos, setFertilized(data, true));
			return true;
		}

		return false;
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
