package ivangeevo.btx.util;

import net.minecraft.core.util.helper.Side;
import net.minecraft.core.util.phys.HitResult;

/**
 * Marker for block logics that need the full 3D hit point when placed.
 * The vanilla click only carries two tangent coordinates and drops the third,
 * so the client mixin re-packs the lost one into the slot that normally holds
 * the face plane (always 0 or 1, which is already implied by the side).
 */
public interface IExactHitPlacement {

	/** Returns {xPlaced, yPlaced} carrying both tangent coordinates of the hit. */
	static double[] encodeFromHit(HitResult.Tile hit) {
		Side side = hit.side;
		double fx = hit.location.x() - hit.tilePos.x();
		double fy = hit.location.y() - hit.tilePos.y();
		double fz = hit.location.z() - hit.tilePos.z();

		if (side.offsetY() != 0) return new double[]{fx, fz};  // top/bottom
		if (side.offsetZ() != 0) return new double[]{fx, fy};  // north/south
		return new double[]{fz, fy};                           // east/west
	}

	/** Returns {fx, fy, fz}. The axis of the clicked face stays 0.5 (use the side for it). */
	static double[] decode(Side side, double xHit, double yHit) {
		double fx = 0.5D, fy = 0.5D, fz = 0.5D;
		if (side.offsetY() != 0) {
			fx = xHit; fz = yHit;
		} else if (side.offsetZ() != 0) {
			fx = xHit; fy = yHit;
		} else if (side.offsetX() != 0) {
			fz = xHit; fy = yHit;
		}
		return new double[]{fx, fy, fz};
	}
}
