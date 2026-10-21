package ivangeevo.btx.block.blocks.turntable;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.MechanicalDevice;
import ivangeevo.btx.block.entity.TileEntityTurntable;
import ivangeevo.btx.sound.BTXSoundTypes;
import ivangeevo.btx.util.utils.MechPowerUtils;
import ivangeevo.btx.util.utils.MiscUtils;
import ivangeevo.btx.util.utils.SoundUtils;
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
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BlockLogicTurntable extends BlockLogic implements MechanicalDevice, TurntableInteraction {
	private static final int TURNTABLE_TICK_RATE = 10;

	public BlockLogicTurntable(@NotNull Block<?> block) {
		super(block, Materials.STONE);
	}

	public static BlockLogicSupplier<BlockLogicTurntable> properties() {
		return block -> new BlockLogicTurntable(
			block
				.withEntity(TileEntityTurntable::new)
				.withSound(BlockSounds.STONE)
				.withHardness(2.0F)
				.withTags(BlockTags.MINEABLE_BY_PICKAXE)
		);
	}

	@Override
	public int tickDelay() {
		return TURNTABLE_TICK_RATE;
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		super.onPlacedByWorld(world, tilePos);
		world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		if (!WorldUtils.isUpdatePendingThisTick(world, tilePos, this.block)) {
			world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
		}
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		boolean receivingMechanicalPower = isInputtingMechanicalPower(world, tilePos);

		if (isBlockMechanicalOn(world, tilePos) != receivingMechanicalPower) {
			emitTurntableParticles(world, tilePos, rand);
			setBlockMechanicalOn(world, tilePos, receivingMechanicalPower);
		}

		boolean receivingRedstonePower = world.hasDirectSignal(tilePos);

		if (isBlockRedstoneOn(world, tilePos) != receivingRedstonePower) {
			setBlockRedstoneOn(world, tilePos, receivingRedstonePower);
		}
	}

	@Override
	public boolean onInteracted(
		@NotNull World world, @NotNull TilePosc tilePos, @NotNull Player player,
		@Nullable Side side, double xHit, double yHit
	) {
		if (player.getHeldItem() != null) {
			return false;
		}
		if (!world.isClientSide) {
			int setting = getSwitchSetting(world, tilePos) + 1;

			setSwitchSetting(world, tilePos, setting > 3 ? 0 : setting);
			SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.TURNTABLE_ADJUST, 0.3F, 0.6F);
		}

		return true;
	}

	@Override
	public void animationTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand) {
		if (isBlockMechanicalOn(world, tilePos)) {
			emitTurntableParticles(world, tilePos, rand);
		}
	}

	@Override
	public @NotNull ItemStack @Nullable [] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause,
														  int data, @Nullable TileEntity tileEntity) {
		return switch (dropCause) {
			case PICK_BLOCK, EXPLOSION, PROPER_TOOL, SILK_TOUCH -> new ItemStack[]{new ItemStack(this.block)}; // meta 0
			default -> null;
		};
	}

	// A turntable can't be turned by another turntable.
	@Override
	public boolean canRotateOnTurntable(WorldSource source, TilePosc pos) { return false; }
	@Override
	public boolean canTransmitRotationHorizontally(WorldSource source, TilePosc pos) { return false; }
	@Override
	public boolean canTransmitRotationVertically(WorldSource source, TilePosc pos) { return false; }

	//------------- MechanicalDevice -------------//

	@Override
	public boolean canOutputMechanicalPower() { return false; }

	@Override
	public boolean canInputMechanicalPower() { return true; }

	@Override
	public boolean isInputtingMechanicalPower(World world, TilePosc tilePos) {
		return MechPowerUtils.isBlockPoweredByAxle(world, tilePos, this);
	}

	@Override
	public boolean canInputAxlePowerToFacing(World world, TilePosc tilePos, Direction facing) {
		return facing == Direction.DOWN;
	}

	@Override
	public boolean isOutputtingMechanicalPower(World world, TilePosc tilePos) { return false; }

	@Override
	public void overpower(World world, TilePosc tilePos) {
		breakTurntable(world, tilePos);
	}

	private void breakTurntable(World world, TilePosc tilePos) {
		MiscUtils.ejectItemsIndividually(world, tilePos, Items.DUST_REDSTONE.id, 1, 1.0F);
		MiscUtils.ejectItemsIndividually(world, tilePos, Items.AMMO_PEBBLE.id, 16, 0.75F);
		MiscUtils.ejectItemsIndividually(world, tilePos, BTXBlocks.SIDING_OAK.id(), 2, 1.0F);

		world.playSoundEffect(null, SoundCategory.WORLD_SOUNDS,
			tilePos.x() + 0.5, tilePos.y() + 0.5, tilePos.z() + 0.5, BTXSoundTypes.MECHANICAL_BREAK, 0.5F, 0.6F + world.rand.nextFloat() * 0.25F);
		world.setBlockTypeNotify(tilePos, Blocks.AIR);
	}

	//------------- Class Specific Methods -------------//

	public boolean isBlockMechanicalOn(WorldSource source, TilePosc pos) {
		return (source.getBlockData(pos) & 1) > 0;
	}

	public void setBlockMechanicalOn(World world, TilePosc pos, boolean on) {
		world.setBlockDataNotify(pos, on ? world.getBlockData(pos) | 1 : world.getBlockData(pos) & ~1);
	}

	public boolean isBlockRedstoneOn(WorldSource source, TilePosc pos) {
		return (source.getBlockData(pos) & 2) > 0;
	}

	public void setBlockRedstoneOn(World world, TilePosc pos, boolean on) {
		world.setBlockDataNotify(pos, on ? world.getBlockData(pos) | 2 : world.getBlockData(pos) & ~2);
	}

	public int getSwitchSetting(WorldSource source, TilePosc pos) {
		return (source.getBlockData(pos) & 12) >> 2;
	}

	public void setSwitchSetting(World world, TilePosc pos, int setting) {
		if (setting < 0 || setting > 3) {
			setting = 0;
		}
		world.setBlockDataNotify(pos, (world.getBlockData(pos) & ~12) | (setting << 2));
	}

	private void emitTurntableParticles(World world, TilePosc pos, Random rand) {
		for (int i = 0; i < 5; i++) {
			double x = pos.x() + rand.nextFloat();
			double y = pos.y() + rand.nextFloat() * 0.5F + 1.0F;
			double z = pos.z() + rand.nextFloat();
			world.spawnParticle("smoke", x, y, z, 0.0, 0.0, 0.0, 0, false);
		}
	}
}
