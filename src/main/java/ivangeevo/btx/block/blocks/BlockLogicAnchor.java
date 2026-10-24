package ivangeevo.btx.block.blocks;

import ivangeevo.btx.achievement.BTXAchievementTriggers;
import ivangeevo.btx.achievement.BTXAchievements;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.entity.TileEntityPulley;
import ivangeevo.btx.entity.entities.lift.MovingAnchorEntity;
import ivangeevo.btx.block.blocks.turntable.TurntableInteraction;
import ivangeevo.btx.util.utils.FacingUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumBlockSoundEffectType;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

public class BlockLogicAnchor extends BlockLogic implements TurntableInteraction {
	public static final double BASE_HEIGHT = 6.0D / 16.0D;
	public static final int MASK_ANCHORED_ROPE = 64;
	private static final int FACING_UP = 1;

	public BlockLogicAnchor(@NotNull Block<?> block) {
		super(block, Materials.STONE);
	}

	public static BlockLogicSupplier<BlockLogicAnchor> properties() {
		return block -> new BlockLogicAnchor(
			block
				.withSound(BlockSounds.STONE)
				.withHardness(2.0F)
				.withTags(BlockTags.MINEABLE_BY_PICKAXE, BlockTags.CAN_HANG_OFF)
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
		return FacingUtils.tiltToFacingAlongY(new AABBd(0, 0, 0, 1, BASE_HEIGHT, 1), getFacing(source.getBlockData(tilePos)));
	}

	//------------- Placement -------------//

	@Override
	public int getPlacedData(
		@Nullable Player player, @NotNull ItemStack itemStack, @NotNull World world,
		@NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit
	) {
		return side.id;
	}

	//------------- Interaction -------------//

	@Override
	public boolean onInteracted(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Player player, @Nullable Side side, double xHit, double yHit) {
		if (!isHangingAnchor(world, tilePos)) {
			return false;
		}

		ItemStack held = player.inventory.getCurrentItem();

		if (held == null) {
			return retractAnchoredRope(world, tilePos, player);
		}

		if (held.getItem() == Items.ROPE) {
			return extendAnchoredRope(world, tilePos, player, held);
		}

		return false;
	}

	public static boolean extendAnchoredRope(World world, TilePosc from, @Nullable Player player, ItemStack ropeStack) {
		TilePos target = from.down(new TilePos());

		while (isAnchoredRope(world, target)) {
			target = target.down(new TilePos());
		}

		if (target.y() < 0 || !world.canPlaceInsideBlock(target)) {
			return false;
		}

		if (!world.isClientSide) {
			world.setBlockTypeDataNotify(target, Blocks.ROPE, MASK_ANCHORED_ROPE);
			world.playBlockSoundEffect(player, target.x() + 0.5, target.y() + 0.5, target.z() + 0.5, Blocks.ROPE, EnumBlockSoundEffectType.PLACE);

			if (player != null) {
				ropeStack.consumeItem(player);
			} else {
				ropeStack.stackSize--;
			}
		}

		return true;
	}

	public static boolean retractAnchoredRope(World world, TilePosc anchorPos, Player player) {
		TilePos bottom = anchorPos.down(new TilePos());

		if (!isAnchoredRope(world, bottom)) {
			return false;
		}

		TilePos next = bottom.down(new TilePos());
		while (isAnchoredRope(world, next)) {
			bottom = next;
			next = next.down(new TilePos());
		}

		if (!world.isClientSide) {
			world.setBlockTypeNotify(bottom, Blocks.AIR);
			world.playBlockSoundEffect(player, bottom.x() + 0.5, bottom.y() + 0.5, bottom.z() + 0.5, Blocks.ROPE, EnumBlockSoundEffectType.PLACE);

			if (player.getGamemode().hasBlockConsumption()) {
				ItemStack rope = new ItemStack(Items.ROPE);
				player.inventory.insertItem(rope, true);

				if (rope.stackSize > 0) {
					player.dropPlayerItem(rope);
				}
			}
		}

		return true;
	}

	//------------- Rope support -------------//

	public static boolean isAnchoredRope(WorldSource source, TilePosc pos) {
		return source.getBlockType(pos) == Blocks.ROPE && (source.getBlockData(pos) & MASK_ANCHORED_ROPE) != 0;
	}

	public static boolean canSupportAnchoredRope(WorldSource source, TilePosc pos) {
		return isAnchoredRope(source, pos) || isHangingAnchor(source, pos) || source.getBlockType(pos) == BTXBlocks.PULLEY;
	}

	public static boolean isHangingAnchor(WorldSource source, TilePosc pos) {
		return source.getBlockType(pos) == BTXBlocks.ANCHOR && getFacing(source.getBlockData(pos)) != FACING_UP;
	}

	public static boolean isLiftAnchor(WorldSource source, TilePosc pos) {
		return source.getBlockType(pos) == BTXBlocks.ANCHOR && getFacing(source.getBlockData(pos)) == FACING_UP;
	}

	public static int getFacing(int data) {
		return data & 7;
	}

	//------------- Lift -------------//

	public static boolean notifyAnchorBlockOfAttachedPulleyStateChange(World world, TilePosc anchorPos, TileEntityPulley pulley) {
		int direction = 0;

		if (pulley.isRaising()) {
			if (world.getBlockType(anchorPos.up(new TilePos())) == Blocks.ROPE) {
				direction = 1;
			}
		} else if (pulley.isLowering()) {
			TilePos below = anchorPos.down(new TilePos());

			if (world.isAirBlock(below) || world.getBlockType(below) == BTXBlocks.PLATFORM) {
				direction = -1;
			}
		}

		if (direction == 0) {
			return false;
		}

		convertAnchorToEntity(world, anchorPos, pulley, direction);
		return true;
	}

	private static void convertAnchorToEntity(World world, TilePosc anchorPos, TileEntityPulley pulley, int direction) {
		MovingAnchorEntity anchor = new MovingAnchorEntity(world, anchorPos.x() + 0.5D, anchorPos.y() + 0.5D, anchorPos.z() + 0.5D, pulley.tilePos, direction);
		world.entityJoinedWorld(anchor);
		BTXAchievementTriggers.awardNearby(world, anchorPos, BTXAchievements.LIFT);

		TilePos below = anchorPos.down(new TilePos());

		if (world.getBlockType(below) == BTXBlocks.PLATFORM) {
			BTXBlocks.PLATFORM.getLogic().convertConnectedPlatformsToEntities(world, below, anchor);
		}

		world.setBlockTypeNotify(anchorPos, Blocks.AIR);
	}

	//------------- Turntable -------------//

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
		return false;
	}

	@Override
	public int rotateOnTurntable(World world, TilePosc pos, boolean reverse, int craftingCounter) {
		int facing = getFacing(world.getBlockData(pos));
		int rotated = FacingUtils.rotateFacingAroundY(facing, reverse);

		if (rotated != facing) {
			world.setBlockDataNotify(pos, rotated);
		}

		return craftingCounter;
	}
}
