package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.entity.TileEntityPulley;
import ivangeevo.btx.gui.BTXScreens;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.sound.BTXSoundTypes;
import ivangeevo.btx.util.utils.InventoryUtils;
import ivangeevo.btx.util.utils.MechPowerUtils;
import ivangeevo.btx.util.utils.MiscUtils;
import ivangeevo.btx.util.utils.WorldUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BlockLogicPulley extends BlockLogic implements MechanicalDevice {
	private static final int TICK_RATE = 10;
	private static final int BIT_MECHANICAL_ON = 1;
	private static final int BIT_REDSTONE_ON = 2;

	public BlockLogicPulley(@NotNull Block<?> block) {
		super(block, Materials.WOOD);
	}

	public static BlockLogicSupplier<BlockLogicPulley> properties() {
		return block -> new BlockLogicPulley(
			block
				.setTicking(true)
				.withEntity(TileEntityPulley::new)
				.withSound(BlockSounds.WOOD)
				.withHardness(2.0F)
				.withTags(BlockTags.MINEABLE_BY_AXE)
		);
	}

	@Override
	public int tickDelay() {
		return TICK_RATE;
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		super.onPlacedByWorld(world, tilePos);
		world.scheduleBlockUpdate(tilePos, this.block, TICK_RATE);
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> neighbor) {
		if (!WorldUtils.isUpdatePendingThisTick(world, tilePos, this.block)) {
			world.scheduleBlockUpdate(tilePos, this.block, TICK_RATE);
		}
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		boolean mechanicalOn = isInputtingMechanicalPower(world, tilePos);
		boolean redstoneOn = isReceivingRedstonePower(world, tilePos);

		if (isRandomTick) {
			if ((mechanicalOn != isBlockOn(world, tilePos) || redstoneOn != isRedstoneOn(world, tilePos))
				&& !WorldUtils.isUpdateScheduled(world, tilePos, this.block)) {
				world.scheduleBlockUpdate(tilePos, this.block, TICK_RATE);
			}

			return;
		}

		boolean changed = false;
		int data = world.getBlockData(tilePos);

		if (mechanicalOn != isBlockOn(world, tilePos)) {
			data = mechanicalOn ? data | BIT_MECHANICAL_ON : data & ~BIT_MECHANICAL_ON;
			changed = true;
		}

		if (redstoneOn != isRedstoneOn(world, tilePos)) {
			data = redstoneOn ? data | BIT_REDSTONE_ON : data & ~BIT_REDSTONE_ON;
			changed = true;
		}

		if (changed) {
			world.setBlockDataNotify(tilePos, data);

			if (world.getTileEntity(tilePos) instanceof TileEntityPulley pulley) {
				pulley.notifyPulleyEntityOfBlockStateChange();
			}
		}
	}

	@Override
	public boolean onInteracted(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Player player, @Nullable Side side, double xHit, double yHit) {
		if (!world.isClientSide && world.getTileEntity(tilePos) instanceof TileEntityPulley pulley) {
			((BTXScreens) player).btx$displayPulleyScreen(pulley);
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
	public boolean isEquivalent(@NotNull WorldSource world, @NotNull TilePosc tilePos, @NotNull TilePosc otherPos, @NotNull Block<?> otherBlock) {
		if (!(otherBlock.getLogic() instanceof BlockLogicPulley)) {
			return super.isEquivalent(world, tilePos, otherPos, otherBlock);
		}

		return getInventoryLevel(world, tilePos) == getInventoryLevel(world, otherPos);
	}

	private static int getInventoryLevel(WorldSource world, TilePosc tilePos) {
		return world.getTileEntity(tilePos) instanceof Container container
			? InventoryUtils.calcRedstoneFromInventory(container)
			: 0;
	}

	//------------- MechanicalDevice -------------//

	@Override
	public boolean canOutputMechanicalPower() {
		return false;
	}

	@Override
	public boolean canInputMechanicalPower() {
		return true;
	}

	@Override
	public boolean isInputtingMechanicalPower(World world, TilePosc tilePos) {
		if (MechPowerUtils.isBlockPoweredByAxle(world, tilePos, this)) {
			return true;
		}

		for (int facing = 2; facing <= 5; facing++) {
			if (MechPowerUtils.isBlockPoweredByHandCrankToSide(world, tilePos, facing)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean canInputAxlePowerToFacing(World world, TilePosc tilePos, Direction facing) {
		return facing.id >= 2;
	}

	@Override
	public boolean isOutputtingMechanicalPower(World world, TilePosc tilePos) {
		return false;
	}

	@Override
	public void overpower(World world, TilePosc tilePos) {
		breakPulley(world, tilePos);
	}

	public void breakPulley(World world, TilePosc tilePos) {
		MiscUtils.ejectItemsIndividually(world, tilePos, Items.STICK.id, 2, 1.0F);
		MiscUtils.ejectItemsIndividually(world, tilePos, BTXItems.GEAR.id, 1, 1.0F);
		MiscUtils.ejectItemsIndividually(world, tilePos, Items.INGOT_IRON.id, 1, 1.0F);

		world.playSoundEffect(null, SoundCategory.WORLD_SOUNDS,
			tilePos.x() + 0.5, tilePos.y() + 0.5, tilePos.z() + 0.5, BTXSoundTypes.MECHANICAL_BREAK, 0.5F, 0.6F + world.rand.nextFloat() * 0.25F);
		world.spawnParticle("explode", tilePos.x() + 0.5, tilePos.y() + 0.5, tilePos.z() + 0.5, 0.0, 0.0, 0.0, 0, false);
		world.setBlockTypeNotify(tilePos, Blocks.AIR);
	}

	//------------- Class Specific Methods -------------//

	private static boolean isReceivingRedstonePower(World world, TilePosc tilePos) {
		return world.hasNeighborSignal(tilePos) || world.hasNeighborSignal(tilePos.up(new TilePos()));
	}

	public static boolean isBlockOn(WorldSource source, TilePosc pos) {
		return (source.getBlockData(pos) & BIT_MECHANICAL_ON) != 0;
	}

	public static boolean isRedstoneOn(WorldSource source, TilePosc pos) {
		return (source.getBlockData(pos) & BIT_REDSTONE_ON) != 0;
	}
}