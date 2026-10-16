package ivangeevo.btx.block.blocks.mini_blocks;

import ivangeevo.btx.util.IExactHitPlacement;
import ivangeevo.btx.util.rendering.PlacementBounds;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

import java.util.List;
import java.util.function.Supplier;

/**
 * Geometry and connection logic shared by the plain and painted mouldings.
 * Data layout: bits 0-3 = alignment (0-11), bits 4-7 = color (painted only).
 */
public abstract class BlockLogicMouldingBase extends BlockLogicWoodPiece
	implements IExactHitPlacement, BlockLogic.MatcherDataEquivalency, PlacementBounds
{
	public static final int MASK_ALIGNMENT = 15;
	public static final int COLOR_SHIFT = 4;

	private static final double[][] ALIGNMENT_BOUNDS = {
		{0,0,0, 1,.5,.5}, {.5,0,0, 1,.5,1}, {0,0,.5, 1,.5,1}, {0,0,0, .5,.5,1}, // 0-3 (bottom edges)
		{0,0,0, .5,1,.5}, {.5,0,0, 1,1,.5}, {.5,0,.5, 1,1,1}, {0,0,.5, .5,1,1}, // 4-7 (vertical edges)
		{0,.5,0, 1,1,.5}, {.5,.5,0, 1,1,1}, {0,.5,.5, 1,1,1}, {0,.5,0, .5,1,1}  // 8-11 (top edges)
	};

	private static final int[][] facingOfConnections = // indexed by axis and alignment
		{
			{ -1, 4, -1, 5, 5, 4, 4, 5, -1, 4, -1, 5 }, // i Axis
			{ 1, 1, 1, 1, -1, -1, -1, -1, 0, 0, 0, 0 }, // j Axis
			{ 3, -1, 2, -1, 3, 3, 2, 2, 3, -1, 2, -1 }  // k Axis
		};

	private static final int[][] alignmentOffsetAlongAxis = // indexed by axis and alignment
		{
			{ 0, 1, 0, -1, -1, 1, 1, -1, 0, 1, 0, -1 }, // i Axis
			{ -1, -1, -1, -1, 0, 0, 0, 0, 1, 1, 1, 1 }, // j Axis
			{ -1, 0, 1, 0, -1, -1, 1, 1, -1, 0, 1, 0 }  // k Axis
		};

	protected final Supplier<Block<?>> matchingCorner;

	protected BlockLogicMouldingBase(@NotNull Block<?> block, Supplier<Block<?>> matchingCorner) {
		super(block);
		this.matchingCorner = matchingCorner;
	}

	@Override
	public float getAmbientOcclusionStrength(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return super.getAmbientOcclusionStrength(source, tilePos);
	}

	//------------- Bounds -------------//

	@Override
	public @NotNull AABBdc getBoundsFromState(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		if (source.getBlockType(tilePos) != this.block) {
			return getBoundsForAlignment(0); // placement check on a position holding another block
		}
		return getBoundsForAlignment(getMouldingAlignment(source, tilePos));
	}

	@Override
	public AABBdc placementBounds(int data) {
		return getBoundsForAlignment(getMouldingAlignmentFromBlockData(data));
	}

	@Override
	public void getCollisionAABBs(@NotNull World world, @NotNull TilePosc tilePos, @NotNull AABBdc aabb, @NotNull List<@NotNull AABBdc> aabbList) {
		int alignment = getMouldingAlignment(world, tilePos);
		addIntersectingBoundingBox(
			aabb,
			this.getBoundsForAlignment(alignment).translate(tilePos.x(), tilePos.y(), tilePos.z()),
			aabbList
		);
		for (int axis = 0; axis < 3; axis++) {
			AABBd ext = getConnectionBoundsAlongAxis(world, tilePos, axis);
			if (ext != null) {
				addIntersectingBoundingBox(aabb, ext.translate(tilePos.x(), tilePos.y(), tilePos.z()), aabbList);
			}
		}
	}

	@Override
	public @NotNull AABBdc getSelectionAABB(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		// BTW getSelectedBoundingBoxFromPool: base box expanded to include every connection box
		AABBd box = getBoundsForAlignment(getMouldingAlignment(source, tilePos));

		for (int axis = 0; axis < 3; axis++) {
			AABBd ext = getConnectionBoundsAlongAxis(source, tilePos, axis);
			if (ext != null) {
				box.minX = Math.min(box.minX, ext.minX);
				box.minY = Math.min(box.minY, ext.minY);
				box.minZ = Math.min(box.minZ, ext.minZ);
				box.maxX = Math.max(box.maxX, ext.maxX);
				box.maxY = Math.max(box.maxY, ext.maxY);
				box.maxZ = Math.max(box.maxZ, ext.maxZ);
			}
		}

		return box.translate(tilePos.x(), tilePos.y(), tilePos.z());
	}

	@Nullable
	@Override
	public HitResult collisionRayTrace(@NotNull World world, @NotNull TilePosc tilePos,
									   @NotNull Vector3dc start, @NotNull Vector3dc end, boolean useSelectionAABB) {
		HitResult best = traceRayAgainstLocalAABB(tilePos, start, end,
			getBoundsForAlignment(getMouldingAlignment(world, tilePos)));

		for (int axis = 0; axis < 3; axis++) {
			AABBd ext = getConnectionBoundsAlongAxis(world, tilePos, axis);
			if (ext == null) continue;

			HitResult hit = traceRayAgainstLocalAABB(tilePos, start, end, ext);
			if (hit != null && (best == null
				|| start.distanceSquared(hit.location) < start.distanceSquared(best.location))) {
				best = hit;
			}
		}
		return best;
	}

	public AABBd getBoundsForAlignment(int alignment) {
		if (alignment < 0 || alignment >= ALIGNMENT_BOUNDS.length) {
			alignment = 0;
		}
		double[] b = ALIGNMENT_BOUNDS[alignment];
		return new AABBd(b[0], b[1], b[2], b[3], b[4], b[5]);
	}

	//------------- Placement -------------//

	protected int getPlacedAlignment(@NotNull Side side, double xHit, double yHit) {
		double[] c = IExactHitPlacement.decode(side, xHit, yHit);
		return alignmentFromClick(side.id, c[0], c[1], c[2]);
	}

	/** Port of BTW MouldingBlock.onBlockPlaced. cx/cy/cz are block-local click coordinates (0-1). */
	protected static int alignmentFromClick(int facing, double cx, double cy, double cz) {
		double dx = Math.abs(cx - 0.5), dy = Math.abs(cy - 0.5), dz = Math.abs(cz - 0.5);
		return switch (facing) {
			case 0 -> dx > dz ? (cx > 0.5 ? 9 : 11) : (cz > 0.5 ? 10 : 8);
			case 1 -> dx > dz ? (cx > 0.5 ? 1 : 3) : (cz > 0.5 ? 2 : 0);
			case 2 -> dx > dy ? (cx > 0.5 ? 6 : 7) : (cy > 0.5 ? 10 : 2);
			case 3 -> dx > dy ? (cx > 0.5 ? 5 : 4) : (cy > 0.5 ? 8 : 0);
			case 4 -> dz > dy ? (cz > 0.5 ? 6 : 5) : (cy > 0.5 ? 9 : 1);
			default -> dz > dy ? (cz > 0.5 ? 7 : 4) : (cy > 0.5 ? 11 : 3);
		};
	}

	//------------- Connections -------------//

	/**
	 * Local (0-1) bounds of the connecting piece along the given axis, or null if nothing connects.
	 * Public so the model can render the same boxes the collision uses.
	 */
	@Nullable
	public AABBd getConnectionBoundsAlongAxis(WorldSource source, TilePosc tilePos, int axis) {
		int alignment = getMouldingAlignment(source, tilePos);
		int connectionToFacing = facingOfConnections[axis][alignment];

		if (connectionToFacing < 0) {
			return null;
		}

		TilePos connectingPos = new TilePos(tilePos);
		connectingPos.add(Direction.fromId(connectionToFacing));

		int connectingAlignment = getAlignmentOfConnectingMouldingAtLocation(source, connectingPos, alignment, axis);
		if (connectingAlignment >= 0) {
			return getBoundsForConnectingMoulding(connectionToFacing, connectingAlignment);
		}

		int cornerData = getConnectingCornerDataAtLocation(source, connectingPos, alignment, axis);
		if (cornerData >= 0) {
			return getBoundsForConnectingCorner(axis, alignmentOffsetAlongAxis[axis][alignment], cornerData);
		}

		return null;
	}

	private AABBd getBoundsForConnectingMoulding(int toFacing, int toAlignment) {
		AABBd box = getBoundsForAlignment(toAlignment);

		// clip the connecting moulding's box to the direction of connection
		switch (toFacing) {
			case 0 -> box.maxY = 0.5D;
			case 1 -> box.minY = 0.5D;
			case 2 -> box.maxZ = 0.5D;
			case 3 -> box.minZ = 0.5D;
			case 4 -> box.maxX = 0.5D;
			default -> box.minX = 0.5D;
		}

		return box;
	}

	private AABBd getBoundsForConnectingCorner(int connectingAxis, int offsetAlongAxis, int cornerData) {
		Vector3d min = new Vector3d(0, 0, 0);
		Vector3d max = new Vector3d(0.5, 0.5, 0.5);

		for (int axis = 0; axis <= 2; axis++) {
			if (axis == connectingAxis) {
				offsetAlongAxis(axis, -offsetAlongAxis, min, max);
			} else {
				offsetAlongAxis(axis, BlockLogicCorner.getCornerAlignmentOffsetAlongAxis(cornerData, axis), min, max);
			}
		}

		return new AABBd(min.x, min.y, min.z, max.x, max.y, max.z);
	}

	private static void offsetAlongAxis(int axis, int offset, Vector3d min, Vector3d max) {
		if (offset > 0) {
			if (axis == 0) {
				min.x += 0.5D;
				max.x += 0.5D;
			} else if (axis == 1) {
				min.y += 0.5D;
				max.y += 0.5D;
			} else {
				min.z += 0.5D;
				max.z += 0.5D;
			}
		}
	}

	/**
	 * Returns the corner's offset flags if a flush, matching corner is at the position, else -1.
	 */
	private int getConnectingCornerDataAtLocation(
		WorldSource source, TilePosc tilePos, int alignmentToConnectTo, int axisToConnectAlong
	) {
		if (source.getBlockType(tilePos) != matchingCorner.get()) {
			return -1;
		}

		int cornerData = source.getBlockData(tilePos) & 7; // offset flags only

		// make sure the corner is flush with the side the moulding is on
		if (alignmentOffsetAlongAxis[axisToConnectAlong][alignmentToConnectTo]
			!= BlockLogicCorner.getCornerAlignmentOffsetAlongAxis(cornerData, axisToConnectAlong)) {
			return -1;
		}

		int result = -1;
		for (int axis = 0; axis <= 2; axis++) {
			if (axis == axisToConnectAlong) continue;

			int mouldingOffset = alignmentOffsetAlongAxis[axis][alignmentToConnectTo];
			if (mouldingOffset == 0) {
				result = cornerData;
			} else if (mouldingOffset != BlockLogicCorner.getCornerAlignmentOffsetAlongAxis(cornerData, axis)) {
				return -1; // the corner does not match up
			}
		}

		return result;
	}

	/**
	 * Returns -1 if no connecting moulding is present.
	 */
	private int getAlignmentOfConnectingMouldingAtLocation(
		WorldSource source, TilePosc tilePos, int alignmentToConnectTo, int axisToConnectAlong
	) {
		if (isMouldingOfSameType(source, tilePos) && isAlignedAlongAxis(source, tilePos, axisToConnectAlong)) {
			int targetAlignment = getMouldingAlignment(source, tilePos);

			for (int axis = 0; axis <= 2; axis++) {
				if (axis != axisToConnectAlong
					&& facingOfConnections[axis][targetAlignment] == facingOfConnections[axis][alignmentToConnectTo]) {
					return targetAlignment;
				}
			}
		}

		return -1;
	}

	private boolean isAlignedAlongAxis(WorldSource source, TilePosc tilePos, int axis) {
		int alignment = getMouldingAlignment(source, tilePos);

		// if the target moulding runs along the axis of connection, it could not itself connect along it
		return facingOfConnections[axis][alignment] < 0;
	}

	protected boolean isMouldingOfSameType(WorldSource source, TilePosc tilePos) {
		return source.getBlockType(tilePos) == this.block;
	}

	//------------- Data -------------//

	public int getMouldingAlignment(WorldSource source, TilePosc tilePos) {
		return getMouldingAlignmentFromBlockData(source.getBlockData(tilePos));
	}

	public int getMouldingAlignmentFromBlockData(int data) {
		int a = data & MASK_ALIGNMENT;
		return a < ALIGNMENT_BOUNDS.length ? a : 0;
	}

	public void setMouldingAlignment(World world, TilePosc tilePos, int alignment) {
		int data = world.getBlockData(tilePos);
		world.setBlockDataNotify(tilePos, setMouldingAlignmentInMetadata(data, alignment));
	}

	public int setMouldingAlignmentInMetadata(int data, int alignment) {
		return (data & ~MASK_ALIGNMENT) | (alignment & MASK_ALIGNMENT);
	}

	public static int colorIndex(int data) {
		return (data >> COLOR_SHIFT) & 15;
	}
}
