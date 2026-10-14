package ivangeevo.btx.util;

import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.joml.Vector3d;

public final class WaterFlowHelper {
	private static final int[] NEIGHBOR_X = {-1, 0, 1, 0};
	private static final int[] NEIGHBOR_Z = {0, -1, 0, 1};

	private WaterFlowHelper() {}

	public static Vector3d getFlowVector(World world, TilePosc tilePos) {
		Vector3d result = new Vector3d();
		TilePos queryPos = new TilePos();
		int depthLocal = getEffectiveFlowDecay(world, tilePos);

		for (int i = 0; i < 4; i++) {
			int checkX = tilePos.x() + NEIGHBOR_X[i];
			int checkY = tilePos.y();
			int checkZ = tilePos.z() + NEIGHBOR_Z[i];

			int depthCheck = getEffectiveFlowDecay(world, queryPos.set(checkX, checkY, checkZ));

			if (depthCheck < 0) {
				if (!world.getBlockMaterial(queryPos.set(checkX, checkY, checkZ)).blocksMotion()) {
					depthCheck = getEffectiveFlowDecay(world, queryPos.set(checkX, checkY - 1, checkZ));

					if (depthCheck >= 0) {
						int mult = depthCheck - (depthLocal - 8);
						result.add(NEIGHBOR_X[i] * mult, 0.0, NEIGHBOR_Z[i] * mult);
					}
				}
			} else {
				int mult = depthCheck - depthLocal;
				result.add(NEIGHBOR_X[i] * mult, 0.0, NEIGHBOR_Z[i] * mult);
			}
		}

		// falling water pressed against a solid block gets pulled downward
		if ((world.getBlockData(tilePos) & 15) >= 8 && isPressedAgainstSolid(world, tilePos, queryPos)) {
			safeNormalize(result);
			result.add(0.0, -6.0, 0.0);
		}

		safeNormalize(result);
		return result;
	}

	private static boolean isPressedAgainstSolid(World world, TilePosc tilePos, TilePos queryPos) {
		for (int yOffset = 0; yOffset <= 1; yOffset++) {
			for (int i = 0; i < 4; i++) {
				queryPos.set(tilePos.x() + NEIGHBOR_X[i], tilePos.y() + yOffset, tilePos.z() + NEIGHBOR_Z[i]);

				if (isBlockSolid(world, queryPos)) {
					return true;
				}
			}
		}

		return false;
	}

	private static boolean isBlockSolid(World world, TilePosc tilePos) {
		Material material = world.getBlockMaterial(tilePos);

		if (material == Materials.WATER) {
			return false;
		}

		if (material == Materials.ICE) {
			return false;
		}

		return material.isSolid();
	}

	private static int getEffectiveFlowDecay(World world, TilePosc tilePos) {
		if (world.getBlockMaterial(tilePos) != Materials.WATER) {
			return -1;
		}

		int data = world.getBlockData(tilePos) & 15;
		return data >= 8 ? 0 : data;
	}

	// BTW's Vec3 returned a zero vector for near-zero length; JOML would produce NaN
	private static void safeNormalize(Vector3d vec) {
		if (vec.length() < 1.0E-4) {
			vec.set(0.0, 0.0, 0.0);
		} else {
			vec.normalize();
		}
	}

}
