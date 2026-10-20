package ivangeevo.btx.util.utils;

import net.minecraft.core.util.helper.Direction;
import org.joml.primitives.AABBd;

public final class FacingUtils {
	// BTW facing order: 0=down 1=up 2=north 3=south 4=west 5=east
	public static int facingFromDirection(Direction direction) {
		return switch (direction) {
			case DOWN -> 0;
			case UP -> 1;
			case NORTH -> 2;
			case SOUTH -> 3;
			case WEST -> 4;
			default -> 5;
		};
	}

	public static Direction directionFromFacing(int facing) {
		return switch (facing) {
			case 0 -> Direction.DOWN;
			case 1 -> Direction.UP;
			case 2 -> Direction.NORTH;
			case 3 -> Direction.SOUTH;
			case 4 -> Direction.WEST;
			default -> Direction.EAST;
		};
	}

	/**
	 * "Tilts" the box towards the desired facing. Takes the up vector and either
	 * yaws or rolls it towards the specified axis.
	 */
	public static AABBd tiltToFacingAlongY(AABBd box, int facing)
	{
		// Snapshot first, since every case reads the old values while writing new ones
		double minX = box.minX, minY = box.minY, minZ = box.minZ;
		double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;

		switch (facing)
		{
			case 0: // x - 1: rotate around Z to point downwards
				box.setMin(1D - maxX, 1D - maxY, minZ);
				box.setMax(1D - minX, 1D - minY, maxZ);
				break;
			case 2: // y - 1
				box.setMin(minX, minZ, 1D - maxY);
				box.setMax(maxX, maxZ, 1D - minY);
				break;
			case 3: // z + 1
				box.setMin(minX, 1D - maxZ, minY);
				box.setMax(maxX, 1D - minZ, maxY);
				break;
			case 4: // x - 1
				box.setMin(1D - maxY, minX, minZ);
				box.setMax(1D - minY, maxX, maxZ);
				break;
			case 5: // x + 1
				box.setMin(minY, 1D - maxX, minZ);
				box.setMax(maxY, 1D - minX, maxZ);
				break;
			default: // 1: up, the default orientation, no change
				break;
		}
		return box;
	}

	// BTW: Block.rotatedFacingsAroundJClockwise / rotatedFacingsAroundJCounterclockwise, indexed by facing
	private static final int[] ROTATED_AROUND_Y = {0, 1, 4, 5, 3, 2};
	private static final int[] ROTATED_AROUND_Y_REVERSE = {0, 1, 5, 4, 2, 3};

	/** BTW: Block.rotateFacingAroundY. Up and down (0, 1) don't rotate. */
	public static int rotateFacingAroundY(int facing, boolean reverse) {
		return reverse ? ROTATED_AROUND_Y_REVERSE[facing] : ROTATED_AROUND_Y[facing];
	}
}
