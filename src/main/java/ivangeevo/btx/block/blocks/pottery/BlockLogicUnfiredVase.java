package ivangeevo.btx.block.blocks.pottery;

import ivangeevo.btx.util.utils.SupportUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.support.ISupport;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

public class BlockLogicUnfiredVase extends BlockLogicUnfiredPottery {
	public BlockLogicUnfiredVase(@NotNull Block<?> block) { super(block); }

	public static BlockLogicSupplier<BlockLogicUnfiredVase> vaseProperties() {
		return block -> new BlockLogicUnfiredVase(applyCommon(block));
	}

	@Override
	public @NotNull AABBdc getBoundsFromState(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return new AABBd(0.1875, 0, 0.1875, 0.8125, 1, 0.8125);
	}

	@Override
	public @NotNull ISupport getSupport(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side) {
		return SupportUtils.fromBounds(this.getBoundsFromState(world, tilePos), side);
	}
}
