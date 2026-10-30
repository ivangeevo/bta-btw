package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.BTXBlocks;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSupplier;
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

public class BlockLogicAxlePainted extends BlockLogicAxle implements PaintedMechanicalDevice {
	public BlockLogicAxlePainted(@NotNull Block<?> block) {
		super(block);
	}

	public static BlockLogicSupplier<BlockLogicAxlePainted> paintedProperties() {
		return block -> new BlockLogicAxlePainted(
			block
				.setBlockItem(b -> new ItemBlockPainted<>(b, true))
				.withSound(BlockSounds.WOOD)
				.withHardness(2.0F)
				.withTags(BlockTags.MINEABLE_BY_AXE)
		);
	}

	@Override
	public int getPlacedData(@Nullable Player player, @NotNull ItemStack itemStack, @NotNull World world,
							 @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		return itemStack.getMetadata() & MASK_COLOR;
	}

	@Override
	public @NotNull ItemStack @Nullable [] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		return switch (dropCause) {
			case PICK_BLOCK, EXPLOSION, PROPER_TOOL, SILK_TOUCH -> new ItemStack[]{new ItemStack(BTXBlocks.AXLE_PAINTED, 1, data & MASK_COLOR)};
			default -> null;
		};
	}

	@Override
	public Block<?> getUnpaintedBlock() {
		return BTXBlocks.AXLE;
	}

	@Override
	public void setColor(@NotNull World world, @NotNull TilePosc tilePos, @NotNull DyeColor color) {
		PaintedMechanicalDevice.super.setColor(world, tilePos, color);
	}
}