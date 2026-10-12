package ivangeevo.btx.block.blocks;

import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.util.MiscUtils;
import ivangeevo.btx.util.SoundUtils;
import ivangeevo.btx.util.WorldUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.support.ISupport;
import net.minecraft.core.block.support.ISupportable;
import net.minecraft.core.block.support.PartialSupport;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.Items;
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
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

import java.util.Random;

public class BlockLogicHandCrank extends BlockLogic implements MechanicalDevice, ISupportable {
	private static final int TICK_RATE = 3;
	private static final int DELAY_BEFORE_RESET = 15;
	public static final float BASE_HEIGHT = 0.25F;

	public BlockLogicHandCrank(@NotNull Block<?> block) {
		super(block, Materials.DECORATION);
	}

	public static BlockLogicSupplier<BlockLogicHandCrank> properties() {
		return block -> new BlockLogicHandCrank(
			block
				.setTicking(true)
				.withSound(BlockSounds.WOOD)
				.withHardness(0.5f)
				.withTags(BlockTags.MINEABLE_BY_PICKAXE)
		);
	}

	@Override
	public int tickDelay() {
		return TICK_RATE;
	}

	@Override
	public boolean canPlaceAt(@NotNull World world, @NotNull TilePosc tilePos) {
		return this.isSupported(world, tilePos, Side.BOTTOM);
	}

	@Override
	public @NotNull ISupport getSupportConstraint(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side) {
		PartialSupport support;
		if (side == Side.BOTTOM) {
			support = PartialSupport.INSTANCE.center();
		} else {
			support = PartialSupport.INSTANCE;
		}

		return support;
	}

	@Override
	public @Nullable AABBdc getCollisionAABB(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return new AABBd(tilePos.x(), tilePos.y(), tilePos.z(), tilePos.x() + 1.0F, tilePos.y() + BASE_HEIGHT, tilePos.z() + 1.0F);
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
	public boolean onInteracted(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Player player, @Nullable Side side, double xHit, double yHit) {
		if (world.isClientSide) {
			return true;
		}

		int data = world.getBlockData(tilePos);
		if (data == 0) {
			if (!this.checkForOverpower(world, tilePos)) {
				world.setBlockData(tilePos, 1);
				world.markBlocksDirty(tilePos, tilePos);
				this.playCrankingSound(world, tilePos, false);
				world.notifyBlockChange(tilePos, this.block);
				world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
			} else {
				this.breakCrankWithDrop(world, tilePos);
			}
		}

		return true;
	}

	@Override
	public void onChunkLoad(@NotNull World world, @NotNull TilePosc tilePos) {
		int data = world.getBlockData(tilePos);
		if (data > 0) {
			world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
		}
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		int data = world.getBlockData(tilePos);

		if (isRandomTick) {
			if (data > 0) {
				// verify we have a tick already scheduled to prevent jams on chunk load
				if (!WorldUtils.isUpdateScheduled(world, tilePos, this.block)) {
					world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay());
				}
			}

			return;
		}

		if (data > 0) {
			if (data < 7) {
				this.playCrankingSound(world, tilePos, false);

				if (data <= 5) {
					world.scheduleBlockUpdate(tilePos, this.block, this.tickDelay() + data);
				} else {
					world.scheduleBlockUpdate(tilePos, this.block, DELAY_BEFORE_RESET);
				}

				world.setBlockData(tilePos, data + 1);
			} else {
				world.setBlockData(tilePos, 0);
				world.markBlocksDirty(tilePos, tilePos);
				this.playCrankingSound(world, tilePos, true);
				world.notifyBlockChange(tilePos, this.block);
			}
		}
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		TilePos below = new TilePos();
		tilePos.down(below);
		if (!world.isBlockNormalCube(below)) {
			this.dropWithCause(world, EnumDropCause.WORLD, tilePos, world.getBlockData(tilePos), world.getTileEntity(tilePos), null);
			world.setBlockType(tilePos, Blocks.AIR);
		}
	}

	@Override
	public boolean canOutputMechanicalPower() {
		return true;
	}

	@Override
	public boolean canInputMechanicalPower() {
		return false;
	}

	@Override
	public boolean isInputtingMechanicalPower(World world, TilePosc tilePos) {
		return false;
	}

	@Override
	public boolean isOutputtingMechanicalPower(World world, TilePosc tilePos) {
		return world.getBlockData(tilePos) > 0;
	}

	public boolean checkForOverpower(World world, TilePosc tilePos) {
		int count = 0;
		TilePos target = new TilePos();
		for (Direction dir : Direction.horizontal) {
			tilePos.add(dir, target);
			Block<?> b = world.getBlockType(target);
			if (b.getLogic() instanceof MechanicalDevice d && d.canInputMechanicalPower()) {
				count++;
			}
		}
		return count > 1;
	}

	public void breakCrankWithDrop(World world, TilePosc tilePos) {
		MiscUtils.ejectSingleItemWithRandomOffset(world, tilePos, Items.STICK.id, 0);
		MiscUtils.ejectSingleItemWithRandomOffset(world, tilePos, Blocks.COBBLE_STONE.asItem().id, 0);
		MiscUtils.ejectSingleItemWithRandomOffset(world, tilePos, Blocks.COBBLE_STONE.asItem().id, 0);
		MiscUtils.ejectSingleItemWithRandomOffset(world, tilePos, BTXItems.GEAR.id, 0);
		SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.ENTITY_SOUNDS, "random.explode", 0.2F, 1.25F);
		world.setBlockType(tilePos, Blocks.AIR);
	}

	private void playCrankingSound(World world, TilePosc tilePos, boolean quiet) {
		float volume = 1.0F;
		float pitch = 2.0F;

		if (quiet) {
			volume = 0.3F; pitch = 0.7F;
		}

		SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.ENTITY_SOUNDS, "random.click", volume, pitch);
	}

}
