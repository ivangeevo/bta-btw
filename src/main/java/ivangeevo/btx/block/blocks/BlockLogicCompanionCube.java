package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.blocks.turntable.TurntableInteraction;
import ivangeevo.btx.block.blocks.util.FireInteraction;
import ivangeevo.btx.sound.BTXSoundTypes;
import ivangeevo.btx.util.utils.FacingUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicRotatable;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public class BlockLogicCompanionCube extends BlockLogicRotatable implements TurntableInteraction, FireInteraction {
	public static final int SPIN_SHIFT = 3;
	public static final int MASK_SPIN = 3 << SPIN_SHIFT;

	public BlockLogicCompanionCube(@NotNull Block<?> block) {
		super(block, Materials.CLOTH);
	}

	public static BlockLogicSupplier<BlockLogicCompanionCube> properties() {
		return block -> new BlockLogicCompanionCube(
			block
				.withSound(BlockSounds.CLOTH)
				.withHardness(0.4F)
		);
	}

	@Override
	public void onPlacedByMob(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side, @NotNull Mob mob, double xHit, double yHit) {
		world.setBlockDataNotify(tilePos, getPlacedDataForMob(mob));
		spawnHearts(world, tilePos);
	}

	@Override
	public void onPlacedOnSide(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		world.setBlockDataNotify(tilePos, setFacing(0, side.direction().opposite()));
	}

	@Override
	public void onRemoved(@NotNull World world, @NotNull TilePosc tilePos, int data) {
		super.onRemoved(world, tilePos, data);
		playWhine(world, tilePos);
	}

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

	@Override
	public int rotateOnTurntable(World world, TilePosc pos, boolean reverse, int craftingCounter) {
		int data = world.getBlockData(pos);
		Direction facing = getFacing(data);

		if (facing.isHorizontal()) {
			data = setFacing(data, Direction.fromId(FacingUtils.rotateFacingAroundY(facing.id, reverse)));
		} else {
			data = setSpin(data, Direction.fromId(FacingUtils.rotateFacingAroundY(getSpin(data).id, reverse)));
		}

		world.setBlockDataNotify(pos, data);

		if (world.rand.nextInt(12) == 0) {
			playWhine(world, pos);
		}

		return craftingCounter;
	}

	@Override
	public void onBurned(World world, TilePosc pos) {
		playDeathEffect(world, pos);
	}

	public static int getPlacedDataForMob(Mob mob) {
		Direction look = getHorizontalLookDirection(mob);
		Direction facing;

		if (mob.xRot > 60.0F) {
			facing = mob.isSneaking() ? Direction.DOWN : Direction.UP;
		} else if (mob.xRot < -60.0F) {
			facing = mob.isSneaking() ? Direction.UP : Direction.DOWN;
		} else {
			facing = look.opposite();
		}

		return setSpin(setFacing(0, facing), look);
	}

	public static Direction getHorizontalLookDirection(Mob mob) {
		return Direction.horizontal[(MathHelper.floor(mob.yRot * 4.0F / 360.0F + 0.5D) + 2) & 3];
	}

	public static Direction getFacing(int data) {
		return getDirectionFromMeta(data);
	}

	public static int setFacing(int data, Direction facing) {
		return (data & ~MASK_DIRECTION) | facing.id;
	}

	public static Direction getSpin(int data) {
		return Direction.horizontal[(data & MASK_SPIN) >> SPIN_SHIFT];
	}

	public static int setSpin(int data, Direction spin) {
		int index = 0;
		for (int i = 0; i < Direction.horizontal.length; i++) {
			if (Direction.horizontal[i] == spin) {
				index = i;
			}
		}

		return (data & ~MASK_SPIN) | (index << SPIN_SHIFT);
	}

	public static void spawnHearts(World world, TilePosc pos) {
		for (int count = 0; count < 7; count++) {
			double motionX = world.rand.nextGaussian() * 0.02D;
			double motionY = world.rand.nextGaussian() * 0.02D;
			double motionZ = world.rand.nextGaussian() * 0.02D;

			world.spawnParticle("heart",
				pos.x() + world.rand.nextFloat(),
				pos.y() + 1 + world.rand.nextFloat(),
				pos.z() + world.rand.nextFloat(),
				motionX, motionY, motionZ, 0, true);
		}
	}

	public static void playWhine(World world, TilePosc pos) {
		if (!world.isClientSide) {
			SoundUtils.playAtTileCenter(world, pos, SoundCategory.ENTITY_SOUNDS, BTXSoundTypes.COMPANION_CUBE_WHINE,
				0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
		}
	}

	public static void playHurt(World world, TilePosc pos, float volume) {
		if (!world.isClientSide) {
			SoundUtils.playAtTileCenter(world, pos, SoundCategory.ENTITY_SOUNDS, BTXSoundTypes.COMPANION_CUBE_HURT,
				volume, (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
		}
	}

	public static void playDeathEffect(World world, TilePosc pos) {
		spawnHearts(world, pos);
		playWhine(world, pos);
	}

	// TODO: move into the Block Dispenser's entity consume logic once it's ported
	// if (targetEntity instanceof MobWolf wolf) {
	// 	playHurt(world, dispenserPos, 1.0F);
	// 	wolf.remove();
	// 	InventoryUtils.addItemStackToInventory(dispenser, new ItemStack(BTXBlocks.COMPANION_CUBE));
	// 	for (int count = 0; count < 2; count++) {
	// 		dispenser.spitOutItem(world, dispenserPos, new ItemStack(Items.STRING));
	// 	}
	// 	return true;
	// }
}
