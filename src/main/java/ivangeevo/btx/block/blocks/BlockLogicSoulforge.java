package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.blocks.turntable.TurntableInteraction;
import ivangeevo.btx.gui.BTXScreens;
import ivangeevo.btx.util.utils.FacingUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

public class BlockLogicSoulforge extends BlockLogic implements TurntableInteraction {
	private static final double NARROW_MIN = 3.0D / 16.0D;
	private static final double NARROW_MAX = 13.0D / 16.0D;

	public BlockLogicSoulforge(@NotNull Block<?> block) {
		super(block, Materials.METAL);
	}

	public static BlockLogicSupplier<BlockLogicSoulforge> properties() {
		return block -> new BlockLogicSoulforge(
			block
				.withSound(BlockSounds.METAL)
				.withHardness(3.5F)
				.withTags(BlockTags.MINEABLE_BY_PICKAXE)
		);
	}

	@Override
	public boolean isSolidRender() {
		return false;
	}

	@Override
	public boolean isCubeShaped() {
		return false;
	}

	@Override
	public @NotNull AABBdc getBoundsFromState(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		int facing = getFacing(source.getBlockData(tilePos));
		if (facing == 2 || facing == 3) {
			return new AABBd(NARROW_MIN, 0.0D, 0.0D, NARROW_MAX, 1.0D, 1.0D);
		}
		return new AABBd(0.0D, 0.0D, NARROW_MIN, 1.0D, 1.0D, NARROW_MAX);
	}

	@Override
	public int getPlacedData(@Nullable Player player, @NotNull ItemStack itemStack, @NotNull World world,
							 @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		int facing = 2;
		if (player != null) {
			int placing = MathHelper.floor(player.yRot * 4.0F / 360.0F + 0.5D) & 3;
			facing = switch (placing) {
				case 0 -> 2;
				case 1 -> 5;
				case 2 -> 3;
				default -> 4;
			};
		}
		return facing;
	}

	@Override
	public boolean onInteracted(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Player player, @Nullable Side side, double xHit, double yHit) {
		if (!world.isClientSide) {
			((BTXScreens) player).btx$displaySoulforgeScreen(tilePos);
		}
		return true;
	}

	@Override
	public @NotNull ItemStack @Nullable [] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		return switch (dropCause) {
			case PICK_BLOCK, EXPLOSION, PROPER_TOOL, SILK_TOUCH -> new ItemStack[]{new ItemStack(this.block)};
			default -> null;
		};
	}

	@Override
	public boolean canRotateOnTurntable(WorldSource source, TilePosc tilePos) {
		return true;
	}

	@Override
	public boolean canTransmitRotationHorizontally(WorldSource source, TilePosc tilePos) {
		return false;
	}

	@Override
	public boolean canTransmitRotationVertically(WorldSource source, TilePosc tilePos) {
		return true;
	}

	@Override
	public int rotateOnTurntable(World world, TilePosc pos, boolean reverse, int craftingCounter) {
		int facing = getFacing(world.getBlockData(pos));
		world.setBlockDataNotify(pos, FacingUtils.rotateFacingAroundY(facing, reverse));
		return craftingCounter;
	}

	public static int getFacing(int data) {
		int facing = data & 7;
		return facing < 2 || facing > 5 ? 2 : facing;
	}
}