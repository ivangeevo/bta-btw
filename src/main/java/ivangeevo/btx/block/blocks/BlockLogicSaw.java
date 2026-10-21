package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.turntable.TurntableInteraction;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.saw.RecipeEntrySaw;
import ivangeevo.btx.recipe.saw.RecipeEntrySawBase;
import ivangeevo.btx.recipe.saw.RecipeEntrySawBreaks;
import ivangeevo.btx.sound.BTXSoundTypes;
import ivangeevo.btx.util.BTXDamageSources;
import ivangeevo.btx.util.utils.FacingUtils;
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
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.MathHelper;
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

public class BlockLogicSaw extends BlockLogic implements MechanicalDevice, TurntableInteraction {
	public static final double BASE_HEIGHT = 12.0D / 16.0D;
	private static final double BLADE_HALF_LENGTH = (10.0D / 16.0D) * 0.5D;
	private static final double BLADE_HALF_WIDTH = (0.25D / 16.0D) * 0.5D;
	private static final double BLADE_HEIGHT = 1.0D - BASE_HEIGHT;

	private static final int POWER_CHANGE_TICK_RATE = 10;
	private static final int SAW_TIME_BASE_TICK_RATE = 20;
	private static final int SAW_TIME_TICK_RATE_VARIANCE = 4;
	private static final int BLADE_DAMAGE = 4;
	private static final double BLADE_HIT_MARGIN = 0.01D;

	private static final int MASK_FACING = 7;
	private static final int BIT_ON = 8;

	public BlockLogicSaw(@NotNull Block<?> block) {
		super(block, Materials.WOOD);
	}

	public static BlockLogicSupplier<BlockLogicSaw> properties() {
		return block -> new BlockLogicSaw(
			block
				.setTicking(true)
				.withSound(BlockSounds.WOOD)
				.withHardness(2.0F)
				.withTags(BlockTags.MINEABLE_BY_AXE)
		);
	}

	@Override
	public int tickDelay() {
		return POWER_CHANGE_TICK_RATE;
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
		return FacingUtils.tiltToFacingAlongY(new AABBd(0, 0, 0, 1, BASE_HEIGHT, 1), getFacing(source.getBlockData(tilePos)));
	}

	@Override
	public @NotNull ItemStack @Nullable [] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		return switch (dropCause) {
			case PICK_BLOCK, EXPLOSION, PROPER_TOOL, SILK_TOUCH -> new ItemStack[]{new ItemStack(BTXBlocks.SAW)};
			default -> null;
		};
	}

	//------------- Placement -------------//

	@Override
	public int getPlacedData(@Nullable Player player, @NotNull ItemStack itemStack, @NotNull World world,
							 @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		return setFacing(0, FacingUtils.facingFromDirection(side.opposite().direction()));
	}

	@Override
	public void onPlacedByMob(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side,
							  @NotNull Mob mob, double xHit, double yHit) {
		world.setBlockDataNotify(tilePos, setFacing(world.getBlockData(tilePos), getPlacedFacing(mob)));
	}

	private static int getPlacedFacing(Mob mob) {
		boolean flipped = mob instanceof Player player && player.isSneaking();

		if (mob.xRot > 60.0F) {
			return flipped ? 0 : 1;
		}

		if (mob.xRot < -60.0F) {
			return flipped ? 1 : 0;
		}

		int facing = switch (MathHelper.floor(mob.yRot * 4.0F / 360.0F + 0.5D) & 3) {
			case 0 -> 2;
			case 1 -> 5;
			case 2 -> 3;
			default -> 4;
		};

		return flipped ? facing ^ 1 : facing;
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		super.onPlacedByWorld(world, tilePos);
		world.scheduleBlockUpdate(tilePos, this.block, POWER_CHANGE_TICK_RATE);
	}

	//------------- Ticking -------------//

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> neighbor) {
		if (!WorldUtils.isUpdatePendingThisTick(world, tilePos, this.block)) {
			scheduleUpdateIfRequired(world, tilePos);
		}
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		if (isRandomTick) {
			// verify a tick is already scheduled, so a saw can't jam after chunk load
			if (!WorldUtils.isUpdateScheduled(world, tilePos, this.block)) {
				scheduleUpdateIfRequired(world, tilePos);
			}

			return;
		}

		boolean receivingPower = isInputtingMechanicalPower(world, tilePos);
		boolean on = isBlockOn(world, tilePos);

		if (on != receivingPower) {
			emitSawParticles(world, tilePos, rand);
			setBlockOn(world, tilePos, receivingPower);

			if (receivingPower) {
				SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.SAW_POWER_ON,
					1.0F + rand.nextFloat() * 0.1F, 1.5F + rand.nextFloat() * 0.1F);

				// the saw doesn't cut on the tick it turns on, so check whether a cut needs scheduling
				scheduleUpdateIfRequired(world, tilePos);
			} else {
				SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.SAW_POWER_OFF,
					1.0F + rand.nextFloat() * 0.1F, 0.75F + rand.nextFloat() * 0.1F);
			}
		} else if (on) {
			sawBlockToFront(world, tilePos, rand);
		}
	}

	@Override
	public void animationTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand) {
		if (isBlockOn(world, tilePos)) {
			emitSawParticles(world, tilePos, rand);
		}
	}

	protected void scheduleUpdateIfRequired(World world, TilePosc tilePos) {
		if (!isCurrentPowerStateValid(world, tilePos)) {
			world.scheduleBlockUpdate(tilePos, this.block, POWER_CHANGE_TICK_RATE);
		} else if (isBlockOn(world, tilePos) && findRecipeToFront(world, tilePos) != null) {
			SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.SAW_CUT,
				1.5F + world.rand.nextFloat() * 0.1F, 1.9F + world.rand.nextFloat() * 0.1F);
			world.scheduleBlockUpdate(tilePos, this.block, SAW_TIME_BASE_TICK_RATE + world.rand.nextInt(SAW_TIME_TICK_RATE_VARIANCE));
		}
	}

	protected boolean isCurrentPowerStateValid(World world, TilePosc tilePos) {
		return isBlockOn(world, tilePos) == isInputtingMechanicalPower(world, tilePos);
	}

	//------------- Sawing -------------//

	private TilePos getTargetPos(WorldSource source, TilePosc tilePos) {
		TilePos target = new TilePos(tilePos);
		target.add(getFacingDirection(source, tilePos));
		return target;
	}

	private @Nullable RecipeEntrySawBase<?, ?> findRecipeToFront(WorldSource source, TilePosc tilePos) {
		return BTXRecipes.SAW == null
			? null
			: BTXRecipes.SAW.findRecipe(source, getTargetPos(source, tilePos), getFacingDirection(source, tilePos));
	}

	protected void sawBlockToFront(World world, TilePosc tilePos, Random rand) {
		RecipeEntrySawBase<?, ?> recipe = findRecipeToFront(world, tilePos);
		if (recipe instanceof RecipeEntrySawBreaks) {
			breakSaw(world, tilePos);
		} else if (recipe instanceof RecipeEntrySaw cut) {
			TilePos target = getTargetPos(world, tilePos);
			cut.apply(world, target, rand);
			emitSawParticles(world, target, rand);
		}
	}

	public void breakSaw(World world, TilePosc tilePos) {
		ejectItems(world, tilePos, BTXItems.GEAR.id, 2);
		ejectItems(world, tilePos, Blocks.PLANKS_OAK.asItem().id, 2);
		ejectItems(world, tilePos, Items.INGOT_IRON.id, 2);
		ejectItems(world, tilePos, BTXItems.BELT.id, 1);

		SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.MECHANICAL_BREAK, 0.5F, 0.6F + world.rand.nextFloat() * 0.25F);
		world.spawnParticle("explode", tilePos.x() + 0.5, tilePos.y() + 0.5, tilePos.z() + 0.5, 0.0, 0.0, 0.0, 0, false);
		world.setBlockTypeNotify(tilePos, Blocks.AIR);
	}

	private static void ejectItems(World world, TilePosc tilePos, int itemId, int count) {
		for (int i = 0; i < count; i++) {
			MiscUtils.ejectSingleItemWithRandomOffset(world, tilePos, itemId, 0);
		}
	}

	//------------- Blade -------------//

	@Override
	public void onEntityCollision(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Entity entity) {
		if (world.isClientSide || !(entity instanceof Mob) || !isBlockOn(world, tilePos)) {
			return;
		}

		// grown slightly: an entity pressed against the base only touches the blade box at its edge
		AABBd blade = getBladeBounds(getFacing(world.getBlockData(tilePos)));
		blade.setMin(blade.minX - BLADE_HIT_MARGIN, blade.minY - BLADE_HIT_MARGIN, blade.minZ - BLADE_HIT_MARGIN);
		blade.setMax(blade.maxX + BLADE_HIT_MARGIN, blade.maxY + BLADE_HIT_MARGIN, blade.maxZ + BLADE_HIT_MARGIN);
		blade.translate(tilePos.x(), tilePos.y(), tilePos.z());

		if (blade.intersectsAABB(entity.bb) && BTXDamageSources.hurt(entity, BLADE_DAMAGE, DamageType.COMBAT, BTXDamageSources.SAW)) {
			Random rand = world.rand;

			SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.SAW_HURT,
				1.0F + rand.nextFloat() * 0.1F, 2.0F + rand.nextFloat() * 0.1F);

			TilePos target = getTargetPos(world, tilePos);

			for (int i = 0; i < 10; i++) {
				world.spawnParticle("reddust", target.x() + rand.nextFloat(), target.y() + rand.nextFloat(), target.z() + rand.nextFloat(),
					0.0, 0.0, 0.0, 0, false);
			}
		}
	}

	private static AABBd getBladeBounds(int facing) {
		double l = BLADE_HALF_LENGTH;
		double w = BLADE_HALF_WIDTH;
		double h = BLADE_HEIGHT;

		return switch (facing) {
			case 0 -> new AABBd(0.5 - l, 0.0, 0.5 - w, 0.5 + l, h, 0.5 + w);
			case 1 -> new AABBd(0.5 - l, 1.0 - h, 0.5 - w, 0.5 + l, 1.0, 0.5 + w);
			case 2 -> new AABBd(0.5 - l, 0.5 - w, 0.0, 0.5 + l, 0.5 + w, h);
			case 3 -> new AABBd(0.5 - l, 0.5 - w, 1.0 - h, 0.5 + l, 0.5 + w, 1.0);
			case 4 -> new AABBd(0.0, 0.5 - w, 0.5 - l, h, 0.5 + w, 0.5 + l);
			default -> new AABBd(1.0 - h, 0.5 - w, 0.5 - l, 1.0, 0.5 + w, 0.5 + l);
		};
	}

	private void emitSawParticles(World world, TilePosc tilePos, Random rand) {
		int facing = getFacing(world.getBlockData(tilePos));
		double x = tilePos.x();
		double y = tilePos.y();
		double z = tilePos.z();
		double extentX = 0.0;
		double extentZ = 0.0;

		switch (facing) {
			case 0 -> { x += 0.5; z += 0.5; extentX = 1.0; }
			case 1 -> { x += 0.5; z += 0.5; y += 1.0; extentX = 1.0; }
			case 2 -> { x += 0.5; y += 0.5; extentX = 1.0; }
			case 3 -> { x += 0.5; y += 0.5; z += 1.0; extentX = 1.0; }
			case 4 -> { y += 0.5; z += 0.5; extentZ = 1.0; }
			default -> { y += 0.5; z += 0.5; x += 1.0; extentZ = 1.0; }
		}

		for (int i = 0; i < 5; i++) {
			world.spawnParticle("smoke",
				x + (rand.nextFloat() - 0.5) * extentX,
				y + rand.nextFloat() * 0.1,
				z + (rand.nextFloat() - 0.5) * extentZ,
				0.0, 0.0, 0.0, 0, false);
		}
	}

	//------------- State -------------//

	public int getFacing(int data) {
		return data & MASK_FACING;
	}

	public static int setFacing(int data, int facing) {
		return (data & ~MASK_FACING) | (facing & MASK_FACING);
	}

	public Direction getFacingDirection(WorldSource source, TilePosc tilePos) {
		return FacingUtils.directionFromFacing(getFacing(source.getBlockData(tilePos)));
	}

	public boolean isBlockOn(WorldSource source, TilePosc tilePos) {
		return isBlockOn(source.getBlockData(tilePos));
	}

	public boolean isBlockOn(int data) {
		return (data & BIT_ON) != 0;
	}

	public void setBlockOn(World world, TilePosc tilePos, boolean on) {
		int data = world.getBlockData(tilePos) & MASK_FACING;
		world.setBlockDataNotify(tilePos, on ? data | BIT_ON : data);
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
		return MechPowerUtils.isBlockPoweredByAxle(world, tilePos, this);
	}

	@Override
	public boolean canInputAxlePowerToFacing(World world, TilePosc tilePos, Direction facing) {
		return FacingUtils.facingFromDirection(facing) != getFacing(world.getBlockData(tilePos));
	}

	@Override
	public boolean isOutputtingMechanicalPower(World world, TilePosc tilePos) {
		return false;
	}

	@Override
	public void overpower(World world, TilePosc tilePos) {
		breakSaw(world, tilePos);
	}

	//------------- TurntableInteraction -------------//

	@Override
	public boolean canRotateOnTurntable(WorldSource source, TilePosc tilePos) {
		return getFacing(source.getBlockData(tilePos)) != 0;
	}

	@Override
	public boolean canTransmitRotationHorizontally(WorldSource source, TilePosc tilePos) {
		return false;
	}

	@Override
	public boolean canTransmitRotationVertically(WorldSource source, TilePosc tilePos) {
		int facing = getFacing(source.getBlockData(tilePos));
		return facing != 0 && facing != 1;
	}

	@Override
	public int rotateOnTurntable(World world, TilePosc pos, boolean reverse, int craftingCounter) {
		int data = world.getBlockData(pos);
		int facing = getFacing(data);
		int rotated = FacingUtils.rotateFacingAroundY(facing, reverse);

		if (rotated != facing) {
			world.setBlockDataNotify(pos, setFacing(data, rotated));
			world.scheduleBlockUpdate(pos, this.block, POWER_CHANGE_TICK_RATE);
			MechPowerUtils.destroyHorizontallyAttachedAxles(world, pos);
		}

		return craftingCounter;
	}
}
