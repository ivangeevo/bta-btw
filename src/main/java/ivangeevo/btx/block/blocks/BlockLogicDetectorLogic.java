package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.entity.entities.lift.BlockLiftedByPlatformEntity;
import ivangeevo.btx.util.utils.WorldUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicAir;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBd;

import java.util.Random;

public class BlockLogicDetectorLogic extends BlockLogicAir {
	public static final int MASK_ENTITY_COLLIDING = 1;
	public static final int MASK_DETECTOR_LOGIC = 2;
	public static final int MASK_INTERSECTION_POINT = 4;
	public static final int MASK_LIT = 8;
	private static final int TICK_RATE = 5;

	private static boolean isBeamBeingRemoved = false;

	public BlockLogicDetectorLogic(@NotNull Block<?> block) {
		super(block);
	}

	public static BlockLogicSupplier<BlockLogicDetectorLogic> properties() {
		return block -> new BlockLogicDetectorLogic(logicProperties(block));
	}

	protected static Block<?> logicProperties(Block<?> block) {
		return block
			.setTicking(true)
			.withTags(BlockTags.PLACE_OVERWRITES, BlockTags.NOT_IN_CREATIVE_MENU);
	}

	@Override
	public int tickDelay() {
		return TICK_RATE;
	}

	//------------- Events -------------//

	@Override
	public void onRemoved(@NotNull World world, @NotNull TilePosc tilePos, int data) {
		if (isBeamBeingRemoved || world.isClientSide) {
			return;
		}

		if ((data & MASK_DETECTOR_LOGIC) == 0 || (data & MASK_INTERSECTION_POINT) != 0) {
			for (Direction dir : Direction.all) {
				int rangeToSource = getRangeToValidLensSource(world, tilePos, dir);

				if (rangeToSource > 0) {
					int remaining = BlockLogicLens.MAX_RANGE - rangeToSource;

					if (remaining > 0) {
						removeLensBeamFromBlock(world, tilePos, dir.opposite(), remaining);
					}
				}
			}
		}
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		if (world.isClientSide) {
			return;
		}

		if (isDetectorLogicFlagOn(world, tilePos)) {
			if (!checkForNeighboringDetector(world, tilePos)) {
				if (isIntersectionPointFlagOn(world, tilePos)) {
					setFlag(world, tilePos, MASK_DETECTOR_LOGIC, false);

					if (!hasMultipleValidLensSources(world, tilePos, null)) {
						setFlag(world, tilePos, MASK_INTERSECTION_POINT, false);
					}
				} else {
					removeSelf(world, tilePos);
					return;
				}
			} else {
				notifyNeighboringDetectorBlocksOfChange(world, tilePos);
			}
		}

		if (!isDetectorLogicFlagOn(world, tilePos) || isIntersectionPointFlagOn(world, tilePos)) {
			scheduleCheck(world, tilePos);
		}
	}

	@Override
	public void onEntityCollision(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Entity entity) {
		if (world.isClientSide || isEntityCollidingFlagOn(world, tilePos)) {
			return;
		}

		if (isEntityWithinBounds(world, tilePos)) {
			changeStateToRegisterEntityCollision(world, tilePos);
			world.scheduleBlockUpdate(tilePos, world.getBlockType(tilePos), TICK_RATE);
		}
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		if (world.isClientSide) {
			return;
		}

		boolean isOn = isEntityCollidingFlagOn(world, tilePos);

		if (isEntityWithinBounds(world, tilePos)) {
			if (!isOn) {
				changeStateToRegisterEntityCollision(world, tilePos);
			}

			world.scheduleBlockUpdate(tilePos, world.getBlockType(tilePos), TICK_RATE);
		} else if (isOn) {
			changeStateToClearEntityCollision(world, tilePos);
		}

		if (isLogicBlock(world.getBlockType(tilePos))) {
			fullyValidateBlock(world, tilePos);
		}
	}

	private static void scheduleCheck(World world, TilePosc tilePos) {
		Block<?> block = world.getBlockType(tilePos);

		if (!WorldUtils.isUpdatePendingThisTick(world, tilePos, block)) {
			world.scheduleBlockUpdate(tilePos, block, TICK_RATE);
		}
	}

	private static void removeSelf(World world, TilePosc tilePos) {
		boolean glowing = isBlockGlowing(world, tilePos);
		isBeamBeingRemoved = true;

		if (glowing) {
			world.setBlockTypeDataNotify(tilePos, Blocks.AIR, 0);
		} else {
			world.setBlockTypeData(tilePos, Blocks.AIR, 0);
		}

		isBeamBeingRemoved = false;
	}

	//------------- Flags -------------//

	public static boolean isLogicBlock(Block<?> block) {
		return block == BTXBlocks.DETECTOR_LOGIC || block == BTXBlocks.DETECTOR_LOGIC_GLOWING;
	}

	public static boolean isLogicBlock(WorldSource source, TilePosc pos) {
		return isLogicBlock(source.getBlockType(pos));
	}

	public static boolean isEntityCollidingFlagOn(WorldSource source, TilePosc pos) {
		return (source.getBlockData(pos) & MASK_ENTITY_COLLIDING) != 0;
	}

	public static boolean isDetectorLogicFlagOn(WorldSource source, TilePosc pos) {
		return (source.getBlockData(pos) & MASK_DETECTOR_LOGIC) != 0;
	}

	public static boolean isIntersectionPointFlagOn(WorldSource source, TilePosc pos) {
		return (source.getBlockData(pos) & MASK_INTERSECTION_POINT) != 0;
	}

	public static boolean isLitFlagOn(WorldSource source, TilePosc pos) {
		return (source.getBlockData(pos) & MASK_LIT) != 0;
	}

	public static void setFlag(World world, TilePosc pos, int mask, boolean on) {
		int data = world.getBlockData(pos);
		world.setBlockData(pos, on ? data | mask : data & ~mask);
	}

	private static void setEntityCollidingFlag(World world, TilePosc pos, boolean on) {
		setFlag(world, pos, MASK_ENTITY_COLLIDING, on);
		world.notifyBlocksOfNeighborChange(pos, world.getBlockType(pos));
	}

	public static boolean isBlockGlowing(WorldSource source, TilePosc pos) {
		return source.getBlockType(pos) == BTXBlocks.DETECTOR_LOGIC_GLOWING;
	}

	private static void setBlockGlowing(World world, TilePosc pos, boolean glowing) {
		int data = world.getBlockData(pos);
		isBeamBeingRemoved = true;
		world.setBlockTypeDataNotify(pos, glowing ? BTXBlocks.DETECTOR_LOGIC_GLOWING : BTXBlocks.DETECTOR_LOGIC, data);
		isBeamBeingRemoved = false;

		if (isEntityCollidingFlagOn(world, pos)) {
			world.scheduleBlockUpdate(pos, world.getBlockType(pos), TICK_RATE);
		}
	}

	//------------- Neighbors -------------//

	private static boolean isEntityWithinBounds(World world, TilePosc pos) {
		AABBd box = new AABBd(pos.x(), pos.y(), pos.z(), pos.x() + 1, pos.y() + 1, pos.z() + 1);

		for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, box)) {
			if (!entity.removed && !(entity instanceof BlockLiftedByPlatformEntity)) {
				return true;
			}
		}

		return false;
	}

	public static boolean checkForNeighboringDetector(WorldSource source, TilePosc pos) {
		TilePos tempPos = new TilePos();

		for (Direction dir : Direction.all) {
			pos.add(dir, tempPos);

			if (source.getBlockType(tempPos) == BTXBlocks.DETECTOR_BLOCK
				&& BlockLogicDetector.getDirection(source, tempPos) == dir.opposite()) {
				return true;
			}
		}

		return false;
	}

	public static void notifyNeighboringDetectorBlocksOfChange(World world, TilePosc pos) {
		TilePos tempPos = new TilePos();

		for (Direction dir : Direction.all) {
			pos.add(dir, tempPos);

			if (world.getBlockType(tempPos) == BTXBlocks.DETECTOR_BLOCK) {
				BTXBlocks.DETECTOR_BLOCK.getLogic().onNeighborChanged(world, tempPos, world.getBlockType(pos));
			}
		}
	}

	//------------- Lens sources -------------//

	public static boolean hasValidLensSource(World world, TilePosc pos, Direction ignore) {
		for (Direction dir : Direction.all) {
			if (dir != ignore && getRangeToValidLensSource(world, pos, dir) > 0) {
				return true;
			}
		}

		return false;
	}

	public static boolean hasMultipleValidLensSources(World world, TilePosc pos, Direction ignore) {
		int count = 0;

		for (Direction dir : Direction.all) {
			if (dir != ignore && getRangeToValidLensSource(world, pos, dir) > 0 && ++count > 1) {
				return true;
			}
		}

		return false;
	}

	public static int getRangeToValidLensSource(World world, TilePosc pos, Direction dir) {
		TilePos tempPos = new TilePos(pos);

		for (int distance = 1; distance <= BlockLogicLens.MAX_RANGE; distance++) {
			step(tempPos, dir);

			if (!world.isBlockLoaded(tempPos)) {
				return 0;
			}

			Block<?> block = world.getBlockType(tempPos);

			if (block == BTXBlocks.LENS) {
				return BlockLogicLens.getDirection(world, tempPos) == dir.opposite() ? distance : 0;
			} else if (!isLogicBlock(block)) {
				return 0;
			}
		}

		return 0;
	}

	public static boolean verifyLitByLens(WorldSource source, TilePosc pos, Direction ignore) {
		for (Direction dir : Direction.all) {
			if (dir != ignore && getRangeToValidLitLensSource(source, pos, dir) > 0) {
				return true;
			}
		}

		return false;
	}

	public static boolean hasValidLitLensSource(WorldSource source, TilePosc pos, Direction dir) {
		return getRangeToValidLitLensSource(source, pos, dir) > 0;
	}

	public static int getRangeToValidLitLensSource(WorldSource source, TilePosc pos, Direction dir) {
		TilePos tempPos = new TilePos(pos);

		for (int distance = 1; distance <= BlockLogicLens.MAX_RANGE; distance++) {
			step(tempPos, dir);

			if (source instanceof World world && !world.isBlockLoaded(tempPos)) {
				return 0;
			}

			Block<?> block = source.getBlockType(tempPos);

			if (block == BTXBlocks.LENS) {
				if (BlockLogicLens.getDirection(source, tempPos) == dir.opposite() && BlockLogicLens.isLit(source.getBlockData(tempPos))) {
					return distance;
				}
				return 0;
			} else if (isLogicBlock(block)) {
				if (!isLitFlagOn(source, tempPos) || isEntityCollidingFlagOn(source, tempPos)) {
					return 0;
				}
			} else {
				return 0;
			}
		}

		return 0;
	}

	//------------- Beams -------------//

	public static void createLensBeamFromBlock(World world, TilePosc pos, Direction dir, int maxRange) {
		TilePos tempPos = new TilePos(pos);

		for (int distance = 1; distance <= maxRange; distance++) {
			step(tempPos, dir);

			if (!world.isBlockLoaded(tempPos)) {
				break;
			}

			Block<?> block = world.getBlockType(tempPos);

			if (block == Blocks.AIR) {
				if (!world.setBlockTypeData(tempPos, BTXBlocks.DETECTOR_LOGIC, 0)) {
					break;
				}
			} else if (isLogicBlock(block)) {
				setFlag(world, tempPos, MASK_INTERSECTION_POINT, true);
			} else {
				break;
			}
		}
	}

	public static void removeLensBeamFromBlock(World world, TilePosc pos, Direction dir, int maxRange) {
		isBeamBeingRemoved = true;
		TilePos tempPos = new TilePos(pos);
		Direction opposite = dir.opposite();

		for (int distance = 1; distance <= maxRange; distance++) {
			step(tempPos, dir);

			if (!world.isBlockLoaded(tempPos) || !isLogicBlock(world, tempPos)) {
				break;
			}

			if (isIntersectionPointFlagOn(world, tempPos)) {
				if (isDetectorLogicFlagOn(world, tempPos)) {
					if (!hasValidLensSource(world, tempPos, opposite)) {
						setFlag(world, tempPos, MASK_INTERSECTION_POINT, false);
					}
				} else if (!hasMultipleValidLensSources(world, tempPos, opposite)) {
					setFlag(world, tempPos, MASK_INTERSECTION_POINT, false);
				}

				if (isLitFlagOn(world, tempPos)) {
					if (!verifyLitByLens(world, tempPos, opposite)) {
						unlightBlock(world, tempPos);
					} else if (isBlockGlowing(world, tempPos) && !shouldBeGlowing(world, tempPos)) {
						setBlockGlowing(world, tempPos, false);
					}
				}
			} else if (isBlockGlowing(world, tempPos)) {
				world.setBlockTypeDataNotify(tempPos, Blocks.AIR, 0);
			} else {
				world.setBlockTypeData(tempPos, Blocks.AIR, 0);
			}
		}

		isBeamBeingRemoved = false;
	}

	public static void lightBlock(World world, TilePosc pos) {
		setFlag(world, pos, MASK_LIT, true);

		if (isDetectorLogicFlagOn(world, pos)) {
			notifyNeighboringDetectorBlocksOfChange(world, pos);
		}
	}

	public static void unlightBlock(World world, TilePosc pos) {
		setFlag(world, pos, MASK_LIT, false);

		if (isBlockGlowing(world, pos)) {
			setBlockGlowing(world, pos, false);
		}

		if (isDetectorLogicFlagOn(world, pos)) {
			notifyNeighboringDetectorBlocksOfChange(world, pos);
		}
	}

	private static void changeStateToRegisterEntityCollision(World world, TilePosc pos) {
		setEntityCollidingFlag(world, pos, true);

		if (isLitFlagOn(world, pos)) {
			if (!isBlockGlowing(world, pos)) {
				setBlockGlowing(world, pos, true);
			} else {
				world.markBlockNeedsUpdate(pos);
			}

			for (Direction dir : Direction.all) {
				int rangeToSource = getRangeToValidLitLensSource(world, pos, dir);

				if (rangeToSource > 0) {
					int remaining = BlockLogicLens.MAX_RANGE - rangeToSource;

					if (remaining > 0) {
						turnBeamOffFromBlock(world, pos, dir.opposite(), remaining);
					}
				}
			}
		}
	}

	private static void changeStateToClearEntityCollision(World world, TilePosc pos) {
		setEntityCollidingFlag(world, pos, false);

		for (Direction dir : Direction.all) {
			int rangeToSource = getRangeToValidLitLensSource(world, pos, dir);

			if (rangeToSource > 0) {
				int remaining = BlockLogicLens.MAX_RANGE - rangeToSource;

				if (remaining > 0) {
					turnBeamOnFromBlock(world, pos, dir.opposite(), remaining);
				}
			}
		}

		if (isLitFlagOn(world, pos) && isBlockGlowing(world, pos)) {
			if (!shouldBeGlowing(world, pos)) {
				setBlockGlowing(world, pos, false);
			} else {
				world.markBlockNeedsUpdate(pos);
			}
		}
	}

	public static void turnBeamOnFromBlock(World world, TilePosc pos, Direction dir, int maxRange) {
		TilePos tempPos = new TilePos(pos);

		for (int distance = 1; distance <= maxRange; distance++) {
			step(tempPos, dir);

			if (!world.isBlockLoaded(tempPos)) {
				break;
			}

			if (isLogicBlock(world, tempPos)) {
				lightBlock(world, tempPos);

				if (isEntityCollidingFlagOn(world, tempPos)) {
					if (!isBlockGlowing(world, tempPos)) {
						setBlockGlowing(world, tempPos, true);
					}
					break;
				}
			} else {
				if (!world.isAirBlock(tempPos)) {
					TilePos previousPos = tempPos.add(dir.opposite(), new TilePos());

					if (isLogicBlock(world, previousPos) && !isBlockGlowing(world, previousPos)) {
						setBlockGlowing(world, previousPos, true);
					}
				}
				break;
			}
		}
	}

	public static void turnBeamOffFromBlock(World world, TilePosc pos, Direction dir, int maxRange) {
		TilePos tempPos = new TilePos(pos);
		Direction opposite = dir.opposite();

		for (int distance = 1; distance <= maxRange; distance++) {
			step(tempPos, dir);

			if (!world.isBlockLoaded(tempPos) || !isLogicBlock(world, tempPos)) {
				break;
			}

			if (isIntersectionPointFlagOn(world, tempPos)) {
				if (!verifyLitByLens(world, tempPos, opposite)) {
					unlightBlock(world, tempPos);
				} else if (isBlockGlowing(world, tempPos) && !shouldBeGlowing(world, tempPos)) {
					setBlockGlowing(world, tempPos, false);
				}
			} else {
				unlightBlock(world, tempPos);
			}

			if (isEntityCollidingFlagOn(world, tempPos)) {
				break;
			}
		}
	}

	public static boolean shouldBeProjectingToFacing(WorldSource source, TilePosc pos, Direction dir) {
		if (isEntityCollidingFlagOn(source, pos)) {
			return false;
		}

		return source.isBlockNormalCube(pos.add(dir, new TilePos())) && hasValidLitLensSource(source, pos, dir.opposite());
	}

	private static boolean shouldBeGlowingToFacing(World world, TilePosc pos, Direction dir) {
		return !world.isAirBlock(pos.add(dir, new TilePos())) && hasValidLitLensSource(world, pos, dir.opposite());
	}

	private static boolean shouldBeGlowing(World world, TilePosc pos) {
		if (isEntityCollidingFlagOn(world, pos)) {
			return true;
		}

		for (Direction dir : Direction.all) {
			if (shouldBeGlowingToFacing(world, pos, dir)) {
				return true;
			}
		}

		return false;
	}

	public static void propagateBeamsThroughBlock(World world, TilePosc pos) {
		boolean lit = false;
		int sourceCount = 0;

		if (!world.setBlockTypeData(pos, BTXBlocks.DETECTOR_LOGIC, 0)) {
			return;
		}

		for (Direction dir : Direction.all) {
			int rangeToSource = getRangeToValidLensSource(world, pos, dir);

			if (rangeToSource > 0) {
				sourceCount++;
				int remaining = BlockLogicLens.MAX_RANGE - rangeToSource;

				if (remaining > 0) {
					createLensBeamFromBlock(world, pos, dir.opposite(), remaining);

					if (hasValidLitLensSource(world, pos, dir)) {
						turnBeamOnFromBlock(world, pos, dir.opposite(), remaining);
						lit = true;
					}
				} else if (hasValidLitLensSource(world, pos, dir)) {
					lit = true;
				}
			}
		}

		if (lit) {
			lightBlock(world, pos);

			if (shouldBeGlowing(world, pos)) {
				setBlockGlowing(world, pos, true);
			}
		}

		if (sourceCount > 1) {
			setFlag(world, pos, MASK_INTERSECTION_POINT, true);
		}
	}

	public static void fullyValidateBlock(World world, TilePosc pos) {
		boolean hasDetector = checkForNeighboringDetector(world, pos);

		if (hasDetector != isDetectorLogicFlagOn(world, pos)) {
			setFlag(world, pos, MASK_DETECTOR_LOGIC, hasDetector);
		}

		boolean shouldBeLit = false;
		int lensSources = 0;

		for (Direction dir : Direction.all) {
			int rangeToSource = getRangeToValidLensSource(world, pos, dir);

			if (rangeToSource > 0) {
				lensSources++;

				if (!shouldBeLit && hasValidLitLensSource(world, pos, dir)) {
					shouldBeLit = true;
				}

				if (BlockLogicLens.MAX_RANGE - rangeToSource > 0) {
					TilePos targetPos = pos.add(dir.opposite(), new TilePos());

					if (world.isBlockLoaded(targetPos) && world.getBlockType(targetPos) == Blocks.AIR) {
						propagateBeamsThroughBlock(world, targetPos);
					}
				}
			}
		}

		if (lensSources == 0 && !isDetectorLogicFlagOn(world, pos)) {
			removeSelf(world, pos);
			return;
		}

		boolean shouldBeIntersection = lensSources > 1 || (lensSources == 1 && isDetectorLogicFlagOn(world, pos));

		if (shouldBeIntersection != isIntersectionPointFlagOn(world, pos)) {
			setFlag(world, pos, MASK_INTERSECTION_POINT, shouldBeIntersection);
		}

		if (shouldBeLit != isLitFlagOn(world, pos)) {
			if (shouldBeLit) {
				lightBlock(world, pos);
			} else {
				unlightBlock(world, pos);
			}
		}

		if (isLitFlagOn(world, pos)) {
			boolean shouldGlow = shouldBeGlowing(world, pos);

			if (shouldGlow != isBlockGlowing(world, pos)) {
				setBlockGlowing(world, pos, shouldGlow);
			}
		}
	}

	private static void step(TilePos pos, Direction dir) {
		pos.x += dir.offsetX();
		pos.y += dir.offsetY();
		pos.z += dir.offsetZ();
	}
}