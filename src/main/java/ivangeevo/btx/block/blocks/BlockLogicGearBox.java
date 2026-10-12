package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.networking.ClientDataChangeListener;
import ivangeevo.btx.util.FacingUtils;
import ivangeevo.btx.util.MechPowerUtils;
import ivangeevo.btx.util.MiscUtils;
import ivangeevo.btx.util.SoundUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicRotatable;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.Mob;
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
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BlockLogicGearBox extends BlockLogicRotatable implements MechanicalDevice, ClientDataChangeListener {
	public static final int TICK_RATE = 10;

	private static final int TURN_ON_TICK_RATE = 10;
	private static final int TURN_OFF_TICK_RATE = 9;

	/** BTW: BTWMod.disableGearBoxPowerDrain */
	public static boolean disableGearBoxPowerDrain = false;

	public BlockLogicGearBox(@NotNull Block<?> block) {
		super(block, Materials.WOOD);
	}

	public static BlockLogicSupplier<BlockLogicGearBox> properties() {
		return block -> new BlockLogicGearBox(
			block
				.setTicking(true)
				.withSound(BlockSounds.WOOD)
				.withHardness(2.0F)
				.withTags(BlockTags.MINEABLE_BY_AXE)
		);
	}

	public int getTickRate() {
		return TICK_RATE;
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		super.onPlacedByWorld(world, tilePos);
		world.scheduleBlockUpdate(tilePos, this.block, getTickRate());
	}

	// Place in the direction facing away from a player if they are sneaking
	@Override
	public void onPlacedByMob(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side,
							  @NotNull Mob mob, double xHit, double yHit) {
		boolean sneaking = mob instanceof Player player && player.isSneaking();
		Direction direction = sneaking
			? mob.getPlacementDirection(side)
			: mob.getPlacementDirection(side.opposite());

		// input face points away from the player
		world.setBlockDataNotify(tilePos, setFacingInData(0, FacingUtils.facingFromDirection(direction.opposite())));
	}

	@Override
	public boolean onInteracted(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Player player, @Nullable Side side, double xHit, double yHit) {
		if (player.getHeldItem() == null && !MechPowerUtils.isBlockPoweredByAxle(world, tilePos, this)) {
			if (!world.isClientSide) {
				toggleFacing(world, tilePos, false);
				SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.ENTITY_SOUNDS, this.block.getSound().getBreakSoundName(), 1.0F, 0.8f);
			}

			return true;
		}

		return false;
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		if (isRandomTick) {
			if (!isCurrentStateValid(world, tilePos)) {
				world.scheduleBlockUpdate(tilePos, this.block, getTickRate());
			}
			return;
		}
		updateMechPoweredState(world, tilePos, isInputtingMechanicalPower(world, tilePos));
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> neighbor) {
		if (!isCurrentStateValid(world, tilePos)) {
			int delay = (!disableGearBoxPowerDrain && isGearBoxOn(world, tilePos))
				? TURN_OFF_TICK_RATE : TURN_ON_TICK_RATE;
			world.scheduleBlockUpdate(tilePos, this.block, delay);
		}
	}

	@Override
	public void animationTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand) {
		if (isGearBoxOn(world, tilePos)) {
			emitGearBoxParticles(world, tilePos, rand);
		}
	}

	@Override
	public @NotNull ItemStack @Nullable [] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		switch (dropCause) {
			case PICK_BLOCK, EXPLOSION, PROPER_TOOL, SILK_TOUCH -> {
				return new ItemStack[]{new ItemStack(BTXBlocks.GEARBOX)};
			}
			default -> {
				return null;
			}
		}
	}

	@Override
	public int getPistonPushReaction(@NotNull World world, @NotNull TilePosc tilePos) {
		return 1;
	}

	public boolean dropComponentItemsOnBadBreak(World world, TilePosc tilePos, int data, float chanceOfDrop) {
		dropItemsIndividually(world, tilePos, Blocks.PLANKS_OAK.asItem().id, 4, 0, chanceOfDrop);
		dropItemsIndividually(world, tilePos, BTXItems.GEAR.id, 3, 0, chanceOfDrop);
		dropItemsIndividually(world, tilePos, Items.DUST_REDSTONE.id, 1, 0, chanceOfDrop);

		return true;
	}

	private void dropItemsIndividually(World world, TilePosc tilePos, int itemId, int count, int damage, float chanceOfDrop) {
		for (int i = 0; i < count; i++) {
			if (world.rand.nextFloat() <= chanceOfDrop) {
				MiscUtils.ejectSingleItemWithRandomOffset(world, tilePos, itemId, damage);
			}
		}
	}

	public int getFacing(WorldSource source, TilePosc tilePos) {
		return source.getBlockData(tilePos) & 7;
	}

	public static int setFacingInData(int data, int facing) {
		return (data & 8) | (facing & 7);
	}

	public void setFacing(World world, TilePosc tilePos, int facing) {
		world.setBlockDataNotify(tilePos, setFacingInData(world.getBlockData(tilePos), facing));
	}

	public boolean toggleFacing(World world, TilePosc tilePos, boolean reverse) {
		if (isGearBoxOn(world, tilePos)) {
			setGearBoxOn(world, tilePos, false);
		}

		int facing = getFacing(world, tilePos);

		facing = cycleFacing(facing, reverse);

		setFacing(world, tilePos, facing);

		world.markBlocksDirty(tilePos, tilePos);
		world.scheduleBlockUpdate(tilePos, this.block, getTickRate());
		world.notifyBlockChange(tilePos, this.block);

		return true;
	}

	//------------- MechanicalDevice -------------//

	@Override
	public boolean canOutputMechanicalPower() {
		return true;
	}

	@Override
	public boolean canInputMechanicalPower() {
		return true;
	}

	@Override
	public boolean isInputtingMechanicalPower(World world, TilePosc tilePos) {
		return MechPowerUtils.isBlockPoweredByAxleToSide(world, tilePos, getFacing(world, tilePos));
	}

	@Override
	public boolean canInputAxlePowerToFacing(World world, TilePosc tilePos, Direction facing) {
		return FacingUtils.facingFromDirection(facing) == getFacing(world, tilePos);
	}

	@Override
	public boolean isOutputtingMechanicalPower(World world, TilePosc tilePos) {
		return isGearBoxOn(world, tilePos);
	}

	@Override
	public int getMechanicalPowerLevelProvidedToAxleAtFacing(World world, TilePosc tilePos, Direction facing) {
		// "facing" = the gearbox face that touches the axle (axle passes the opposite of its own facing)
		if (isGearBoxOn(world, tilePos) && getFacing(world, tilePos) != FacingUtils.facingFromDirection(facing)) {
			return 4;
		}
		return 0;
	}

	@Override
	public void overpower(World world, TilePosc tilePos) {
		if (isGearBoxOn(world, tilePos)) {
			// an overpowered gearbox that doesn't have its gears disengaged, is destroyed
			breakGearBox(world, tilePos);
		}
	}

	//------------- Class Specific Methods ------------//

	protected void updateMechPoweredState(World world, TilePosc tilePos, boolean shouldBePowered) {
		if (isGearBoxOn(world, tilePos) != shouldBePowered) {
			setGearBoxOn(world, tilePos, shouldBePowered);

			if (shouldBePowered) {
				// BTW played this client side in clientNotificationOfMetadataChange
				world.playSoundEffectAtTile(null, SoundCategory.ENTITY_SOUNDS, tilePos,
					"random.explode", 0.05F, 1.0F);
			}
		}
	}

	protected boolean isCurrentStateValid(World world, TilePosc tilePos) {
		return isGearBoxOn(world, tilePos) == isInputtingMechanicalPower(world, tilePos);
	}

	public boolean isGearBoxOn(WorldSource worldSource, TilePosc tilePos) {
		return isGearBoxOn(worldSource.getBlockData(tilePos));
	}

	public boolean isGearBoxOn(int data) {
		return (data & 8) > 0;
	}

	public int setGearBoxOn(int data, boolean on) {
		data &= 7; // filter out any old on state

		if (on) {
			data |= 8;
		}

		return data;
	}

	public void setGearBoxOn(World world, TilePosc tilePos, boolean on) {
		int data = setGearBoxOn(world.getBlockData(tilePos), on);
		world.setBlockDataNotify(tilePos, data);
		markRangeForRenderUpdate(world, tilePos, 3);
	}

	public void breakGearBox(World world, TilePosc tilePos) {
		dropComponentItemsOnBadBreak(world, tilePos, world.getBlockData(tilePos), 1F);
		world.playSoundEffect(null, SoundCategory.ENTITY_SOUNDS,
			tilePos.x() + 0.5, tilePos.y() + 0.5, tilePos.z() + 0.5, "random.explode", 0.2F, 1.25F);
		world.setBlockTypeNotify(tilePos, Blocks.AIR);
		// client-side ±3 refresh is needed here too, see the caveat below
	}

	private void emitGearBoxParticles(World world, TilePosc tilePos, Random rand) {
		for (int tempCount = 0; tempCount < 5; tempCount++) {
			float smokeX = tilePos.x() + rand.nextFloat();
			float smokeY = tilePos.y() + rand.nextFloat() * 0.5F + 1.0F;
			float smokeZ = tilePos.z() + rand.nextFloat();

			world.spawnParticle("smoke", smokeX, smokeY, smokeZ, 0.0, 0.0, 0.0, 0, false);
		}
	}

	//------------- BTW facing helpers (Block.rotateFacingAroundY / cycleFacing) -------------//

	private static final int[] HORIZONTAL_CLOCKWISE = {2, 5, 3, 4}; // N, E, S, W

	public static int rotateFacingAroundY(int facing, boolean reverse) {
		for (int i = 0; i < 4; i++) {
			if (HORIZONTAL_CLOCKWISE[i] == facing) {
				return HORIZONTAL_CLOCKWISE[(i + (reverse ? 3 : 1)) % 4];
			}
		}

		return facing; // up / down do not rotate around Y
	}

	public static int cycleFacing(int facing, boolean reverse) {
		return (facing + (reverse ? 5 : 1)) % 6;
	}

	public static void markRangeForRenderUpdate(World world, TilePosc pos, int radius) {
		world.markBlocksDirty(
			new TilePos(pos.x() - radius, pos.y() - radius, pos.z() - radius),
			new TilePos(pos.x() + radius, pos.y() + radius, pos.z() + radius)
		);
	}

	@Override
	public void clientNotificationOfDataChange(World world, TilePosc pos, int oldData, int newData) {
		if (!isGearBoxOn(oldData) && isGearBoxOn(newData)) {
			//world.playSoundEffectAtTile(null, SoundCategory.ENTITY_SOUNDS, pos,
				//"random.chestopen", 0.25F, world.rand.nextFloat() * 0.25F + 0.25F);
			emitGearBoxParticles(world, pos, world.rand);
		}
		markRangeForRenderUpdate(world, pos, 3);
	}

	@Override public void clientBlockAdded(World world, TilePosc tilePos) {
		markRangeForRenderUpdate(world, tilePos, 3);
	}

	@Override public void clientBlockRemoved(World world, TilePosc tilePos, int oldData) {
		markRangeForRenderUpdate(world, tilePos, 3);
	}
}
