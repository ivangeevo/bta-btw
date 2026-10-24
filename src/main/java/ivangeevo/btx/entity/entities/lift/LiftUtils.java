package ivangeevo.btx.entity.entities.lift;

import ivangeevo.btx.entity.LiftTeleportable;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.entity.Entity;
import ivangeevo.btx.block.BTXBlocks;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.vehicle.EntityMinecart;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.world.World;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.joml.primitives.AABBd;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class LiftUtils {
	private static final int BLOCK_BREAK_EVENT = 2001;
	private static final double MINECART_RAIL_RENDER_HEIGHT = 0.5D;
	private static final Map<Entity, double[]> PUSHES = new WeakHashMap<>();
	private static final Map<Entity, MovingLiftEntity> CLIENT_RIDERS = new WeakHashMap<>();

	private LiftUtils() {}

	public static void recordPush(Entity entity, long tick, double offsetY) {
		double[] push = PUSHES.get(entity);

		if (push != null && push[0] == tick) {
			push[1] += offsetY;
		} else {
			PUSHES.put(entity, new double[]{tick, offsetY});
		}
	}

	public static void restoreInterpolation(Entity entity, long tick) {
		double[] push = PUSHES.remove(entity);

		if (push != null && push[0] == tick) {
			entity.yo -= push[1];
		}
	}

	public static boolean canLiftBreakBlock(World world, TilePosc pos) {
		Block<?> block = world.getBlockType(pos);
		return !block.getMaterial().isSolid() || block == Blocks.COBWEB;
	}

	public static void breakBlockWithDrops(World world, TilePosc pos) {
		Block<?> block = world.getBlockType(pos);
		int data = world.getBlockData(pos);
		TileEntity tileEntity = world.getTileEntity(pos);

		world.playBlockEvent(pos, BLOCK_BREAK_EVENT, block.id());
		world.setBlockTypeNotify(pos, Blocks.AIR);
		block.dropWithCause(world, EnumDropCause.WORLD, pos, data, tileEntity, null);
	}

	public static void positionAllNonPlayerMoveableEntitiesOutsideOfLocation(World world, TilePosc pos) {
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, blockBox(pos));

		for (Entity entity : list) {
			if ((entity.isPushable() || entity instanceof EntityItem) && !(entity instanceof Player)) {
				positionEntityOutsideOfLocation(entity, pos);
			}
		}
	}

	public static void positionAllPlayerEntitiesOutsideOfLocation(World world, TilePosc pos) {
		List<Player> list = world.getEntitiesWithinAABB(Player.class, blockBox(pos));

		for (Player player : list) {
			positionEntityOutsideOfLocation(player, pos);
		}
	}

	private static AABBd blockBox(TilePosc pos) {
		return new AABBd(pos.x(), pos.y(), pos.z(), pos.x() + 1, pos.y() + 1, pos.z() + 1);
	}

	private static void positionEntityOutsideOfLocation(Entity entity, TilePosc pos) {
		double minX = pos.x();
		double minY = pos.y();
		double minZ = pos.z();
		double maxX = pos.x() + 1;
		double maxY = pos.y() + 1;
		double maxZ = pos.z() + 1;

		boolean xOverlap = false;
		boolean yOverlap = false;
		boolean zOverlap = false;

		double xOffset = 0.0D;
		double yOffset = 0.0D;
		double zOffset = 0.0D;

		boolean overFootprint = entity.bb.maxX > minX && entity.bb.minX < maxX && entity.bb.maxZ > minZ && entity.bb.minZ < maxZ;

		if (overFootprint && entity.bb.minY < maxY && entity.bb.minY >= maxY - 0.5D) {
			moveEntity(entity, entity.x, entity.y + maxY - entity.bb.minY + 0.01D, entity.z);
			return;
		}

		if (entity.bb.minX <= maxX && entity.bb.maxX >= minX) {
			xOverlap = true;
			xOffset = Math.abs(maxX - entity.bb.minX) < Math.abs(minX - entity.bb.maxX)
				? maxX - entity.bb.minX + 0.01D
				: minX - entity.bb.maxX - 0.01D;
		}

		if (entity.bb.minY <= maxY && entity.bb.maxY >= minY) {
			yOverlap = true;
			yOffset = Math.abs(maxY - entity.bb.minY) < Math.abs(minY - entity.bb.maxY)
				? maxY - entity.bb.minY + 0.01D
				: minY - entity.bb.maxY - 0.01D;
		}

		if (entity.bb.minZ <= maxZ && entity.bb.maxZ >= minZ) {
			zOverlap = true;
			zOffset = Math.abs(maxZ - entity.bb.minZ) < Math.abs(minZ - entity.bb.maxZ)
				? maxZ - entity.bb.minZ + 0.01D
				: minZ - entity.bb.maxZ - 0.01D;
		}

		double x = entity.x;
		double y = entity.y;
		double z = entity.z;

		if (xOverlap && Math.abs(xOffset) < 0.2D
			&& (!yOverlap || Math.abs(xOffset) < Math.abs(yOffset))
			&& (!zOverlap || Math.abs(xOffset) < Math.abs(zOffset))) {
			x += xOffset;
		} else if (yOverlap && Math.abs(yOffset) < 0.2D && (!zOverlap || Math.abs(yOffset) < Math.abs(zOffset))) {
			y += yOffset;
		} else if (zOverlap && Math.abs(zOffset) < 0.2D) {
			z += zOffset;
		} else {
			return;
		}

		moveEntity(entity, x, y, z);
	}

	private static void moveEntity(Entity entity, double x, double y, double z) {
		if (entity instanceof LiftTeleportable teleportable) {
			teleportable.btx$liftTeleportTo(x, y, z);
		} else {
			entity.setPos(x, y, z);
		}
	}

	public static void markClientRider(Entity entity, MovingLiftEntity lift) {
		CLIENT_RIDERS.put(entity, lift);
	}

	public static void snapClientRider(Entity entity) {
		MovingLiftEntity lift = CLIENT_RIDERS.get(entity);

		if (lift == null) {
			return;
		}

		boolean overLift = entity.bb.maxX > lift.bb.minX && entity.bb.minX < lift.bb.maxX && entity.bb.maxZ > lift.bb.minZ && entity.bb.minZ < lift.bb.maxZ;
		double offset = lift.bb.maxY + 0.01D - entity.bb.minY;

		if (entity instanceof EntityMinecart) {
			offset += MINECART_RAIL_RENDER_HEIGHT - entity.heightOffset - 0.01D;
		}

		if (lift.removed || !overLift || Math.abs(offset) > 0.75D) {
			CLIENT_RIDERS.remove(entity);
			return;
		}

		entity.setPos(entity.x, entity.y + offset, entity.z);
		settleOnLift(entity);
	}

	public static void liftPlayerOutOfPlatformBlock(Player player) {
		int y = MathHelper.floor(player.bb.minY);
		double top = y + 1.0D;

		if (player.bb.minY >= top || player.bb.minY < top - 0.5D) {
			return;
		}

		for (int x = MathHelper.floor(player.bb.minX); x <= MathHelper.floor(player.bb.maxX - 1.0E-6D); x++) {
			for (int z = MathHelper.floor(player.bb.minZ); z <= MathHelper.floor(player.bb.maxZ - 1.0E-6D); z++) {
				if (player.world.getBlockType(new TilePos(x, y, z)) == BTXBlocks.PLATFORM) {
					player.setPos(player.x, player.y + top - player.bb.minY + 0.01D, player.z);
					settleOnLift(player);
					return;
				}
			}
		}
	}

	public static void settleOnLift(Entity entity) {
		if (entity.yd < 0.0D) {
			entity.yd = 0.0D;
		}

		entity.onGround = true;
		entity.fallDistance = 0.0F;
	}
}