package ivangeevo.btx.util;

import net.minecraft.core.util.helper.Direction;

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
}
