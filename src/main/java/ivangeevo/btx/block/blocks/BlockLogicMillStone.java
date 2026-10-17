package ivangeevo.btx.block.blocks;

import com.mojang.logging.LogUtils;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.entity.TileEntityMillStone;
import ivangeevo.btx.gui.BTXScreens;
import ivangeevo.btx.world.TickPersistence;
import ivangeevo.btx.util.utils.MechPowerUtils;
import net.minecraft.core.block.*;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Random;

public class BlockLogicMillStone extends BlockLogic implements MechanicalDevice {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int TICK_RATE = 10;

	public BlockLogicMillStone(@NotNull Block<?> block) {
		super(block, Materials.STONE);
	}

	public static BlockLogicSupplier<BlockLogicMillStone> properties() {
		return block -> new BlockLogicMillStone(
			block
				.setTicking(true)
				.withEntity(TileEntityMillStone::new)
				.withSound(BlockSounds.STONE)
				.withHardness(3.5f)
				.withTags(BlockTags.MINEABLE_BY_PICKAXE)
		);
	}

	public int getTickRate() {
		return TICK_RATE;
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		super.onPlacedByWorld(world, tilePos);
		world.scheduleBlockUpdate(tilePos, this.block, this.getTickRate());
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		boolean receivingPower = this.isInputtingMechanicalPower(world, tilePos);
		if (this.isActive(world, tilePos) != receivingPower) {
			world.scheduleBlockUpdate(tilePos, this.block, this.getTickRate());
		}
	}

	@Override
	public @NotNull ItemStack @Nullable [] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		ItemStack[] drops;
		switch (dropCause) {
			case PICK_BLOCK, EXPLOSION, PROPER_TOOL, SILK_TOUCH -> {
				drops = new ItemStack[]{new ItemStack(BTXBlocks.MILLSTONE)};
			}
			default -> drops = null;
		}

		return drops;
	}

	@Override
	public boolean onInteracted(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Player player, @Nullable Side side, double xHit, double yHit) {
		if (!world.isClientSide) {
			TileEntityMillStone tileEntityMillStone = (TileEntityMillStone) world.getTileEntity(tilePos);
			((BTXScreens)player).btx$displayMillStoneScreen(tileEntityMillStone);
		}
		return true;
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		int data = world.getBlockData(tilePos);

		if (isRandomTick) {
			if (!isCurrentStateValid(world, tilePos)) {
				if (data > 0 && !((TickPersistence) world).btx$isUpdateScheduled(tilePos, this.block)) {
					world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
				}
			}
		}

		boolean receivingPower = this.isInputtingMechanicalPower(world, tilePos);
		boolean isActive = this.isActive(world, tilePos);
		if (isActive != receivingPower) {
			if (isActive) {
				this.setActive(world, tilePos, false);
			} else {
				TileEntity tileEntity = world.getTileEntity(tilePos);
				if (tileEntity instanceof TileEntityMillStone tileEntityMillStone) {
					//if (tileEntityMillStone.isWholeCompanionCubeNextToBeProcessed()) {
					//world.playSoundEffectAtTile(null, SoundCategory.ENTITY_SOUNDS, tilePos.x() + 0.5, tilePos.y() + 0.5, tilePos.z() + 0.5, "mob.wolf.hurt", 5.0F, (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
					//}

					world.playSoundEffect(null, SoundCategory.WORLD_SOUNDS, tilePos.x() + 0.5, tilePos.y() + 0.5, tilePos.z() + 0.5, "random.explode", 0.2F, 1.25F);
					this.emitMillingParticles(world, tilePos, rand);
					this.setActive(world, tilePos, true);
				}
			}
		}
	}

	@Override
	public void animationTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand) {
		if (this.isActive(world, tilePos)) {
			this.emitMillingParticles(world, tilePos, rand);
		}
	}

	public boolean isActive(World world, TilePosc tilePos) {
		return world.getBlockData(tilePos) > 0;
	}

	public void setActive(World world, TilePosc tilePos, boolean active) {
		if (active) {
			world.setBlockData(tilePos, 1);
		} else {
			world.setBlockData(tilePos, 0);
		}
	}

	private void emitMillingParticles(World world, TilePosc tilePos, Random rand) {
		for (int counter = 0; counter < 5; counter++) {
			float smokeX = tilePos.x() + rand.nextFloat();
			float smokeY = tilePos.y() + rand.nextFloat() * 0.5F + 1.0F;
			float smokeZ = tilePos.z() + rand.nextFloat();
			world.spawnParticle("smoke", smokeX, smokeY, smokeZ, 0.0, 0.0, 0.0, 0, false);
		}
	}

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
		return MechPowerUtils.isBlockPoweredByAxle(world, tilePos, this) ||
			   MechPowerUtils.isBlockPoweredByHandCrank(world, tilePos);
	}

	@Override
	public boolean canInputAxlePowerToFacing(World world, TilePosc tilePos, Direction facing) {
		return facing == Direction.DOWN || facing == Direction.UP;
	}

	@Override
	public boolean isOutputtingMechanicalPower(World world, TilePosc tilePos) {
		return false;
	}

	public boolean isCurrentStateValid(World world, TilePosc tilePos) {
		boolean receivingPower = isInputtingMechanicalPower(world, tilePos);
		boolean isActive = isActive(world, tilePos);

		return isActive == receivingPower;
	}
}
