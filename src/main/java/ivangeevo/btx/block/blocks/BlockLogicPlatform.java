package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.blocks.turntable.TurntableInteraction;
import ivangeevo.btx.entity.entities.lift.BlockLiftedByPlatformEntity;
import ivangeevo.btx.entity.entities.lift.MovingAnchorEntity;
import ivangeevo.btx.entity.entities.lift.MovingPlatformEntity;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public class BlockLogicPlatform extends BlockLogic implements TurntableInteraction {
	private static final int RANGE = 2;
	private static final int SIZE = RANGE * 2 + 1;

	public BlockLogicPlatform(@NotNull Block<?> block) {
		super(block, Materials.WOOD);
	}

	public static BlockLogicSupplier<BlockLogicPlatform> properties() {
		return block -> new BlockLogicPlatform(
			block
				.withSound(BlockSounds.WOOD)
				.withHardness(2.0F)
				.withTags(BlockTags.MINEABLE_BY_AXE)
		);
	}

	@Override
	public boolean isSolidRender() {
		return false;
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

	//------------- Lift conversion -------------//

	public void convertConnectedPlatformsToEntities(World world, TilePosc source, MovingAnchorEntity anchor) {
		propagateConvertToEntity(world, new TilePos(source), anchor, source, new boolean[SIZE][SIZE][SIZE]);
	}

	private void propagateConvertToEntity(World world, TilePos pos, MovingAnchorEntity anchor, TilePosc source, boolean[][][] considered) {
		int dx = pos.x() - source.x();
		int dy = pos.y() - source.y();
		int dz = pos.z() - source.z();

		if (considered[dx + RANGE][dy + RANGE][dz + RANGE]) {
			return;
		}

		considered[dx + RANGE][dy + RANGE][dz + RANGE] = true;

		int distToSource = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
		int closestAnchorDist = getDistToClosestConnectedAnchorPoint(world, pos);

		if (closestAnchorDist != -1 && distToSource > closestAnchorDist) {
			return;
		}

		convertToEntity(world, pos, anchor);

		for (int facing = 0; facing < 6; facing++) {
			TilePos neighbor = new TilePos(pos);
			neighbor.add(Direction.fromId(facing));

			if (world.getBlockType(neighbor) == this.block && isWithinRange(neighbor, source)) {
				propagateConvertToEntity(world, neighbor, anchor, source, considered);
			}
		}
	}

	private void convertToEntity(World world, TilePosc pos, MovingAnchorEntity anchor) {
		world.entityJoinedWorld(new MovingPlatformEntity(world, pos.x() + 0.5D, pos.y() + 0.5D, pos.z() + 0.5D, anchor));

		TilePos above = pos.up(new TilePos());

		if (BlockLiftedByPlatformEntity.canBlockBeConvertedToEntity(world, above)) {
			world.entityJoinedWorld(new BlockLiftedByPlatformEntity(world, above));
			world.setBlockTypeNotify(above, Blocks.AIR);
		}

		world.setBlockTypeNotify(pos, Blocks.AIR);
	}

	private int getDistToClosestConnectedAnchorPoint(World world, TilePosc pos) {
		int closest = -1;

		for (int x = pos.x() - RANGE; x <= pos.x() + RANGE; x++) {
			for (int y = pos.y() - RANGE; y <= pos.y() + RANGE; y++) {
				for (int z = pos.z() - RANGE; z <= pos.z() + RANGE; z++) {
					TilePos candidate = new TilePos(x, y, z);

					if (world.getBlockType(candidate) != this.block || !BlockLogicAnchor.isLiftAnchor(world, candidate.up(new TilePos()))) {
						continue;
					}

					if (isPlatformConnectedToAnchorPoint(world, pos, candidate)) {
						int dist = Math.abs(x - pos.x()) + Math.abs(y - pos.y()) + Math.abs(z - pos.z());

						if (closest == -1 || dist < closest) {
							closest = dist;
						}
					}
				}
			}
		}

		return closest;
	}

	private boolean isPlatformConnectedToAnchorPoint(World world, TilePosc platform, TilePosc anchorPoint) {
		if (platform.x() == anchorPoint.x() && platform.y() == anchorPoint.y() && platform.z() == anchorPoint.z()) {
			return true;
		}

		return propagateTestForConnected(world, new TilePos(anchorPoint), anchorPoint, platform, new boolean[SIZE][SIZE][SIZE]);
	}

	private boolean propagateTestForConnected(World world, TilePos pos, TilePosc source, TilePosc target, boolean[][][] considered) {
		int dx = pos.x() - source.x();
		int dy = pos.y() - source.y();
		int dz = pos.z() - source.z();

		if (considered[dx + RANGE][dy + RANGE][dz + RANGE]) {
			return false;
		}

		considered[dx + RANGE][dy + RANGE][dz + RANGE] = true;

		for (int facing = 0; facing < 6; facing++) {
			TilePos neighbor = new TilePos(pos);
			neighbor.add(Direction.fromId(facing));

			if (neighbor.x() == target.x() && neighbor.y() == target.y() && neighbor.z() == target.z()) {
				return true;
			}

			if (world.getBlockType(neighbor) == this.block && isWithinRange(neighbor, source)
				&& propagateTestForConnected(world, neighbor, source, target, considered)) {
				return true;
			}
		}

		return false;
	}

	private static boolean isWithinRange(TilePosc pos, TilePosc source) {
		return Math.abs(pos.x() - source.x()) <= RANGE && Math.abs(pos.y() - source.y()) <= RANGE && Math.abs(pos.z() - source.z()) <= RANGE;
	}
}