package ivangeevo.btx.block.blocks;

import ivangeevo.btx.BTXMod;
import ivangeevo.btx.achievement.BTXAchievementTriggers;
import ivangeevo.btx.achievement.BTXAchievements;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.entity.TileEntityCement;
import ivangeevo.btx.sound.BTXSoundTypes;
import ivangeevo.btx.util.utils.SoundUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicDoor;
import net.minecraft.core.block.BlockLogicLadder;
import net.minecraft.core.block.BlockLogicSign;
import net.minecraft.core.block.BlockLogicSugarcane;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.MaterialColor;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemBucket;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.collection.NamespaceID;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

import java.util.Random;

public class BlockLogicCement extends BlockLogic {
	public static final Material CEMENT_MATERIAL = new Material(MaterialColor.stone).replaceable().notPushable();
	public static final int MAX_SPREAD_DIST = 16;
	public static final int TICKS_TO_DRY = 12;
	public static final int TICKS_TO_PARTIALLY_DRY = 8;
	public static final int MASK_POWERED = 1;
	private static final int TICK_RATE = 20;
	private static final int PISTON_IMMOVABLE = 2;
	private static final int[][] SIDE_OFFSETS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

	public BlockLogicCement(@NotNull Block<?> block) {
		super(block, CEMENT_MATERIAL);
		block.withEntity(TileEntityCement::new);
	}

	public static NamespaceID registerBucketState() {
		return ItemBucket.registerState(NamespaceID.fromPool(BTXMod.MOD_ID, "cement"),
			new ItemBucket.BucketState("cement", BTXBlocks.CEMENT, null, null, BlockLogicCement::onPouredFromBucket));
	}

	private static void onPouredFromBucket(World world, TilePosc tilePos) {
		if (world.isClientSide) {
			return;
		}

		float pitch = 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F;
		SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.CEMENT_POUR, 0.5F, pitch);

		BTXAchievementTriggers.awardNearby(world, tilePos, BTXAchievements.CEMENT);
	}

	public static BlockLogicSupplier<BlockLogicCement> properties() {
		return block -> new BlockLogicCement(
			block
				.setTicking(true)
				.withSound(BlockSounds.SAND)
				.withHardness(100.0F)
				.withLightBlock(15)
				.withTags(BlockTags.PLACE_OVERWRITES, BlockTags.NOT_IN_CREATIVE_MENU)
		);
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
	public boolean isCubeShaped() {
		return false;
	}

	@Override
	public boolean canCollideCheck(int data, boolean shouldCollideWithFluids) {
		return shouldCollideWithFluids && (data & MASK_POWERED) == 0;
	}

	@Override
	public @Nullable AABBdc getCollisionAABB(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		double height = source.getBlockType(tilePos.up(new TilePos())) == this.block ? 1.0D : 0.5D;
		return new AABBd(tilePos.x(), tilePos.y(), tilePos.z(), tilePos.x() + 1, tilePos.y() + height, tilePos.z() + 1);
	}

	@Override
	public int getPistonPushReaction(@NotNull World world, @NotNull TilePosc tilePos) {
		return PISTON_IMMOVABLE;
	}

	@Override
	public ItemStack @Nullable [] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		return null;
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		super.onPlacedByWorld(world, tilePos);

		if (world.getBlockType(tilePos) == this.block) {
			world.scheduleBlockUpdate(tilePos, this.block, TICK_RATE);

			if (world.hasNeighborSignal(tilePos)) {
				setPowered(world, tilePos, true);
			}
		}
	}

	@Override
	public void animationTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand) {
		if (!isPartiallyDry(world, tilePos) && rand.nextInt(250) == 0) {
			float pitch = 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F;
			SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.CEMENT_MOAN, 0.5F, pitch);
		}
	}

	//------------- Flowing and drying -------------//

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		if (world.isClientSide || !(world.getTileEntity(tilePos) instanceof TileEntityCement)) {
			return;
		}

		int x = tilePos.x(), y = tilePos.y(), z = tilePos.z();
		int cementDist = getSpreadDist(world, tilePos);
		boolean powered = world.hasNeighborSignal(tilePos);

		if (isPowered(world, tilePos) != powered) {
			setPowered(world, tilePos, powered);
		}

		if (cementDist > 0) {
			int newCementDist = -100;
			newCementDist = checkForLesserSpreadDist(world, new TilePos(x - 1, y, z), newCementDist);
			newCementDist = checkForLesserSpreadDist(world, new TilePos(x + 1, y, z), newCementDist);
			newCementDist = checkForLesserSpreadDist(world, new TilePos(x, y, z - 1), newCementDist);
			newCementDist = checkForLesserSpreadDist(world, new TilePos(x, y, z + 1), newCementDist);
			newCementDist = checkForLesserSpreadDist(world, new TilePos(x, y + 1, z), newCementDist);

			if (newCementDist < 0) {
				newCementDist = -1;
			} else {
				newCementDist++;
			}

			int cementDistUp = getSpreadDist(world, new TilePos(x, y + 1, z));

			if (cementDistUp >= 0 && cementDistUp < newCementDist) {
				newCementDist = cementDistUp + 1;
			}

			if (newCementDist > 0 && newCementDist < cementDist) {
				cementDist = newCementDist;
				setSpreadDist(world, tilePos, cementDist);
				setDryTime(world, tilePos, 0);
			}
		} else if (cementDist == 0 && powered) {
			setDryTime(world, tilePos, 0);
		}

		int dryTime = getDryTime(world, tilePos) + 1;
		int minDryTime = checkNeighboursCloserToSourceForMinDryTime(world, tilePos);

		if (minDryTime <= dryTime) {
			dryTime = minDryTime <= 0 ? 0 : minDryTime - 1;
		}

		if (dryTime > TICKS_TO_DRY) {
			world.setBlockTypeNotify(tilePos, Blocks.STONE);
			return;
		}

		setDryTime(world, tilePos, dryTime);
		world.scheduleBlockUpdate(tilePos, this.block, TICK_RATE);

		TilePos below = new TilePos(x, y - 1, z);

		if (isBlockOpenToSpread(world, below)) {
			int targetCementDist = cementDist + 1;

			if (targetCementDist <= MAX_SPREAD_DIST) {
				spreadTo(world, below, targetCementDist);
			}
		} else if (cementDist >= 0 && (cementDist == 0 || blocksFlow(world, below))) {
			int spreadDist = cementDist + 1;

			if (spreadDist <= MAX_SPREAD_DIST) {
				for (int[] offset : SIDE_OFFSETS) {
					TilePos sidePos = new TilePos(x + offset[0], y, z + offset[1]);

					if (!blocksFlow(world, sidePos) && !isSourceBlock(world, sidePos) && isBlockOpenToSpread(world, sidePos)) {
						spreadTo(world, sidePos, spreadDist);
					}
				}
			}
		}
	}

	private void spreadTo(World world, TilePosc pos, int spreadDist) {
		Block<?> existing = world.getBlockType(pos);

		if (!world.isAirBlock(pos)) {
			existing.dropWithCause(world, EnumDropCause.WORLD, pos, world.getBlockData(pos), world.getTileEntity(pos), null);
		}

		world.setBlockTypeNotify(pos, this.block);
		setSpreadDist(world, pos, spreadDist);
	}

	private int checkForLesserSpreadDist(World world, TilePosc pos, int sourceSpreadDist) {
		int targetSpreadDist = getSpreadDist(world, pos);

		if (targetSpreadDist < 0) {
			return sourceSpreadDist;
		}

		return sourceSpreadDist >= 0 && targetSpreadDist >= sourceSpreadDist ? sourceSpreadDist : targetSpreadDist;
	}

	private int checkNeighboursCloserToSourceForMinDryTime(World world, TilePosc pos) {
		int minDryTime = 1000;
		int distToSource = getSpreadDist(world, pos);
		int x = pos.x(), y = pos.y(), z = pos.z();

		minDryTime = getLesserDryTimeIfCloserToSource(world, new TilePos(x, y + 1, z), distToSource, minDryTime);
		minDryTime = getLesserDryTimeIfCloserToSource(world, new TilePos(x + 1, y, z), distToSource, minDryTime);
		minDryTime = getLesserDryTimeIfCloserToSource(world, new TilePos(x - 1, y, z), distToSource, minDryTime);
		minDryTime = getLesserDryTimeIfCloserToSource(world, new TilePos(x, y, z + 1), distToSource, minDryTime);
		return getLesserDryTimeIfCloserToSource(world, new TilePos(x, y, z - 1), distToSource, minDryTime);
	}

	private int getLesserDryTimeIfCloserToSource(World world, TilePosc pos, int distToSource, int dryTime) {
		if (world.getBlockMaterial(pos) == CEMENT_MATERIAL && getSpreadDist(world, pos) < distToSource) {
			int targetDryTime = getDryTime(world, pos);

			if (targetDryTime < dryTime) {
				return targetDryTime;
			}
		}

		return dryTime;
	}

	private boolean blocksFlow(World world, TilePosc pos) {
		Block<?> block = world.getBlockType(pos);
		BlockLogic logic = block.getLogic();

		if (logic instanceof BlockLogicDoor || logic instanceof BlockLogicSign || logic instanceof BlockLogicLadder || logic instanceof BlockLogicSugarcane) {
			return true;
		}

		if (world.isAirBlock(pos)) {
			return false;
		}

		Material material = block.getMaterial();
		return material != CEMENT_MATERIAL && material.isSolid();
	}

	private boolean isBlockOpenToSpread(World world, TilePosc pos) {
		return world.getBlockMaterial(pos) != CEMENT_MATERIAL && !blocksFlow(world, pos);
	}

	//------------- State -------------//

	public static boolean isPowered(WorldSource source, TilePosc pos) {
		return (source.getBlockData(pos) & MASK_POWERED) != 0;
	}

	private void setPowered(World world, TilePosc pos, boolean powered) {
		int data = world.getBlockData(pos);

		if (powered == ((data & MASK_POWERED) != 0)) {
			return;
		}

		if (powered) {
			float pitch = world.rand.nextFloat() * 0.4F + 0.8F;
			SoundUtils.playAtTileCenter(world, pos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.CEMENT_SCREAM, 1.0F, pitch);
		}

		world.setBlockData(pos, powered ? data | MASK_POWERED : data & ~MASK_POWERED);
	}

	public static int getSpreadDist(WorldSource source, TilePosc pos) {
		if (source.getBlockType(pos) != BTXBlocks.CEMENT) {
			return -1;
		}

		return source.getTileEntity(pos) instanceof TileEntityCement cement ? cement.getSpreadDist() : MAX_SPREAD_DIST;
	}

	private static void setSpreadDist(World world, TilePosc pos, int spreadDist) {
		if (world.getTileEntity(pos) instanceof TileEntityCement cement) {
			cement.setSpreadDist(spreadDist);
			world.notifyBlocksOfNeighborChange(pos, BTXBlocks.CEMENT);
		}
	}

	public static boolean isSourceBlock(WorldSource source, TilePosc pos) {
		return getSpreadDist(source, pos) == 0;
	}

	public static int getDryTime(WorldSource source, TilePosc pos) {
		if (source.getBlockType(pos) == BTXBlocks.CEMENT && source.getTileEntity(pos) instanceof TileEntityCement cement) {
			return cement.getDryTime();
		}

		return 0;
	}

	private static void setDryTime(World world, TilePosc pos, int dryTime) {
		if (world.getTileEntity(pos) instanceof TileEntityCement cement && cement.getDryTime() != dryTime) {
			cement.setDryTime(dryTime);
			world.notifyBlocksOfNeighborChange(pos, BTXBlocks.CEMENT);
		}
	}

	public static boolean isPartiallyDry(WorldSource source, TilePosc pos) {
		return getDryTime(source, pos) >= TICKS_TO_PARTIALLY_DRY;
	}

	public static float getRenderHeight(WorldSource source, TilePosc pos) {
		if (source.getBlockType(pos) != BTXBlocks.CEMENT) {
			return 1.0F;
		}

		float height = (getSpreadDist(source, pos) + 1) / (float) (MAX_SPREAD_DIST + 2);
		return height * (isPartiallyDry(source, pos) ? 0.1F : 0.5F);
	}
}
