package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.turntable.TurntableInteraction;
import ivangeevo.btx.util.utils.WorldUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BlockLogicLightBulb extends BlockLogic implements TurntableInteraction {
	private static final int TICK_RATE = 2;

	private final boolean lit;

	public BlockLogicLightBulb(@NotNull Block<?> block, boolean lit) {
		super(block, Materials.GLASS);
		this.lit = lit;
	}

	public static BlockLogicSupplier<BlockLogicLightBulb> properties(boolean lit) {
		return block -> {
			block
				.setTicking(true)
				.withSound(BlockSounds.GLASS)
				.withHardness(0.4F);

			if (lit) {
				block
					.withLightEmission(1.0F)
					.withTags(BlockTags.MINEABLE_BY_PICKAXE, BlockTags.NOT_IN_CREATIVE_MENU);
			} else {
				block.withTags(BlockTags.MINEABLE_BY_PICKAXE);
			}

			return new BlockLogicLightBulb(block, lit);
		};
	}

	@Override
	public int tickDelay() {
		return TICK_RATE;
	}

	@Override
	public boolean isSolidRender() {
		return false;
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		world.scheduleBlockUpdate(tilePos, this.block, tickDelay());
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		if (!isCurrentStateValid(world, tilePos) && !WorldUtils.isUpdatePendingThisTick(world, tilePos, this.block)) {
			world.scheduleBlockUpdate(tilePos, this.block, tickDelay());
		}
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		if (isRandomTick) {
			if (!isCurrentStateValid(world, tilePos) && !WorldUtils.isUpdateScheduled(world, tilePos, this.block)) {
				world.scheduleBlockUpdate(tilePos, this.block, tickDelay());
			}
			return;
		}

		boolean powered = world.hasNeighborSignal(tilePos);

		if (powered != this.lit) {
			world.setBlockTypeNotify(tilePos, powered ? BTXBlocks.LIGHT_BULB_ON : BTXBlocks.LIGHT_BULB);
		}
	}

	private boolean isCurrentStateValid(World world, TilePosc tilePos) {
		return world.hasNeighborSignal(tilePos) == this.lit;
	}

	@Override
	public ItemStack @Nullable [] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		return new ItemStack[]{new ItemStack(BTXBlocks.LIGHT_BULB)};
	}

	//------------- Turntable -------------//

	@Override
	public boolean canRotateOnTurntable(WorldSource source, TilePosc tilePos) {
		return true;
	}

	@Override
	public boolean canTransmitRotationHorizontally(WorldSource source, TilePosc tilePos) {
		return true;
	}

	@Override
	public boolean canTransmitRotationVertically(WorldSource source, TilePosc tilePos) {
		return true;
	}
}