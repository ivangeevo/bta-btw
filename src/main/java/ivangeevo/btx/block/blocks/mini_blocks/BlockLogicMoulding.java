package ivangeevo.btx.block.blocks.mini_blocks;

import ivangeevo.btx.block.BTXBlocks;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.IPaintable;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BlockLogicMoulding extends BlockLogicMouldingBase implements IPaintable {

	public BlockLogicMoulding(@NotNull Block<?> block, Supplier<Block<?>> matchingCorner) {
		super(block, matchingCorner);
	}

	public static BlockLogicSupplier<BlockLogicMoulding> properties() {
		return block -> new BlockLogicMoulding(
			block
				.withSound(BlockSounds.WOOD)
				.withHardness(2.0F)
				.withBlastResistance(5.0F)
				.withTags(BlockTags.MINEABLE_BY_AXE),
			() -> BTXBlocks.CORNER_OAK
		);
	}

	@Override
	public int getPlacedData(@Nullable Player player, @NotNull ItemStack itemStack, @NotNull World world,
							 @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		return setMouldingAlignmentInMetadata(0, getPlacedAlignment(side, xHit, yHit));
	}

	@Override
	public void setColor(@NotNull World world, @NotNull TilePosc tilePos, @NotNull DyeColor color) {
		int alignment = getMouldingAlignmentFromBlockData(world.getBlockData(tilePos));
		world.setBlockTypeDataRaw(tilePos, BTXBlocks.MOULDING_OAK_PAINTED, alignment);
		world.notifyBlockChange(tilePos, BTXBlocks.MOULDING_OAK_PAINTED);
		BTXBlocks.MOULDING_OAK_PAINTED.getLogic().setColor(world, tilePos, color);
	}
}
