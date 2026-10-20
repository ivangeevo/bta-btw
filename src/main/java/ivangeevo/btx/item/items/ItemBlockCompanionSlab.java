package ivangeevo.btx.item.items;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicCompanionCube;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.block.ItemBlockSlab;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBlockCompanionSlab<T extends BlockLogic> extends ItemBlockSlab<T> {
	public ItemBlockCompanionSlab(@NotNull Block<T> block) {
		super(block);
	}

	@Override
	public @NotNull BlockDataResult getAccumulationResult(@NotNull ItemStack selfStack, @NotNull World world, @Nullable Player player,
														  @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit,
														  @NotNull BlockDataResult result) {
		result.block = BTXBlocks.COMPANION_CUBE;
		result.data = player != null
			? BlockLogicCompanionCube.getPlacedDataForMob(player)
			: BlockLogicCompanionCube.setFacing(0, Direction.NORTH);
		result.bounds.setMin(0.0, 0.0, 0.0).setMax(1.0, 1.0, 1.0);
		return result;
	}

	@Override
	public void postAccumulate(@NotNull ItemStack selfStack, @NotNull World world, @Nullable Player player,
							   @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit,
							   @NotNull BlockDataResult result) {
		BlockLogicCompanionCube.spawnHearts(world, tilePos);
	}
}
