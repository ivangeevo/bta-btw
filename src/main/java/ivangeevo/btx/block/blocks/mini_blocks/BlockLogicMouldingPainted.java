package ivangeevo.btx.block.blocks.mini_blocks;

import ivangeevo.btx.block.BTXBlocks;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.IPainted;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.block.ItemBlockPainted;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BlockLogicMouldingPainted extends BlockLogicMouldingBase implements IPainted {

	public BlockLogicMouldingPainted(@NotNull Block<?> block, Supplier<Block<?>> matchingCorner) {
		super(block, matchingCorner);
	}

	public static BlockLogicSupplier<BlockLogicMouldingPainted> properties() {
		return block -> new BlockLogicMouldingPainted(
			block
				.setBlockItem(b -> new ItemBlockPainted<>(b, true))
				.withSound(BlockSounds.WOOD)
				.withHardness(2.0F)
				.withBlastResistance(5.0F)
				.withTags(BlockTags.MINEABLE_BY_AXE),
			() -> BTXBlocks.CORNER_OAK_PAINTED
		);
	}

	@Override
	public int getPlacedData(@Nullable Player player, @NotNull ItemStack itemStack, @NotNull World world,
							 @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		int color = colorIndex(itemStack.getMetadata()); // item meta uses the same layout as toMetadata
		return setMouldingAlignmentInMetadata(color << COLOR_SHIFT, getPlacedAlignment(side, xHit, yHit));
	}

	@Override
	public ItemStack[] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		return new ItemStack[]{new ItemStack(this, 1, data & ~MASK_ALIGNMENT)}; // color bits only, no alignment
	}

	@NotNull
	@Override
	public DyeColor fromMetadata(int meta) {
		return DyeColor.colorFromBlockMeta(colorIndex(meta));
	}

	@Override
	public int toMetadata(@NotNull DyeColor color) {
		return color.blockMeta << COLOR_SHIFT;
	}

	@Override
	public int stripColorFromMetadata(int meta) {
		return meta & MASK_ALIGNMENT; // keep alignment, drop color
	}

	@Override
	public void removeDye(@NotNull World world, @NotNull TilePosc tilePos) {
		int alignment = getMouldingAlignmentFromBlockData(world.getBlockData(tilePos));
		world.setBlockTypeDataRaw(tilePos, BTXBlocks.MOULDING_OAK, alignment);
		world.notifyBlockChange(tilePos, BTXBlocks.MOULDING_OAK);
	}

	@Override
	public void setColor(@NotNull World world, @NotNull TilePosc tilePos, @NotNull DyeColor color) {
		IPainted.super.setColor(world, tilePos, color);
	}
}
