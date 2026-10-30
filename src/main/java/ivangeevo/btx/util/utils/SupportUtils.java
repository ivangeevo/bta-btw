package ivangeevo.btx.util.utils;

import net.minecraft.core.block.support.FullSupport;
import net.minecraft.core.block.support.ISupport;
import net.minecraft.core.block.support.PartialSupport;
import net.minecraft.core.util.helper.Side;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBdc;

public final class SupportUtils {
	private static final double EPSILON = 1.0E-4D;
	private static final double CENTER_MIN = 0.375D;
	private static final double CENTER_MAX = 0.625D;

	public static @NotNull ISupport fromBounds(@NotNull AABBdc bounds, @NotNull Side side) {
		boolean touchesFace = switch (side) {
			case BOTTOM -> bounds.minY() <= EPSILON;
			case TOP -> bounds.maxY() >= 1.0D - EPSILON;
			case NORTH -> bounds.minZ() <= EPSILON;
			case SOUTH -> bounds.maxZ() >= 1.0D - EPSILON;
			case WEST -> bounds.minX() <= EPSILON;
			case EAST -> bounds.maxX() >= 1.0D - EPSILON;
			default -> false;
		};

		if (!touchesFace) {
			return PartialSupport.INSTANCE;
		}

		double minA, maxA, minB, maxB;
		if (side == Side.TOP || side == Side.BOTTOM) {
			minA = bounds.minX(); maxA = bounds.maxX(); minB = bounds.minZ(); maxB = bounds.maxZ();
		} else if (side == Side.NORTH || side == Side.SOUTH) {
			minA = bounds.minX(); maxA = bounds.maxX(); minB = bounds.minY(); maxB = bounds.maxY();
		} else {
			minA = bounds.minZ(); maxA = bounds.maxZ(); minB = bounds.minY(); maxB = bounds.maxY();
		}

		if (minA <= EPSILON && maxA >= 1.0D - EPSILON && minB <= EPSILON && maxB >= 1.0D - EPSILON) {
			return FullSupport.INSTANCE;
		}

		if (minA <= CENTER_MIN && maxA >= CENTER_MAX && minB <= CENTER_MIN && maxB >= CENTER_MAX) {
			return PartialSupport.INSTANCE.center();
		}

		return PartialSupport.INSTANCE;
	}
}