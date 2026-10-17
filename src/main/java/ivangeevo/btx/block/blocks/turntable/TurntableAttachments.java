package ivangeevo.btx.block.blocks.turntable;

import ivangeevo.btx.util.utils.MiscUtils;
import net.minecraft.core.block.*;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class TurntableAttachments {
	private static final Map<Block<?>, TurntableAttachable> RULES = new HashMap<>();
	private TurntableAttachments() {}

	//------------- Registry -------------//

	public static void register(Block<?> block, TurntableAttachable rule) { RULES.put(block, rule); }

	public static @Nullable TurntableAttachable find(Block<?> block) {
		if (block.getLogic() instanceof TurntableAttachable a) return a;
		return RULES.get(block);
	}

	//------------- Defaults -------------//

	public static void registerDefaults() {
		// Side-mounted blocks that can't survive rotation
		registerSideMountedDrop(Blocks.LEVER_COBBLE_STONE);
		registerSideMountedDrop(Blocks.SIGN_WALL_PLANKS_OAK);
		registerSideMountedDrop(Blocks.SIGN_WALL_PLANKS_OAK_PAINTED);
		for (Block<?> block : Blocks.blocksList) {
			if (block != null && block.getLogic() instanceof BlockLogicButton) {
				registerSideMountedDrop(block);
			}
		}

		// Ladder: moves to the new side
		register(Blocks.LADDER_OAK, rule()
			.attachedWhen((w, p, f) -> ladderSupportSide(w.getBlockData(p)) == f)
			.onRotated((w, p, f) -> true) // allow the move, otherwise the default vetoes it
			.newData((w, p, f, nf) -> nf.opposite().id)
		);

		// Torches: move to the new side
		TurntableAttachable torch = rule()
			.attachedWhen((w, p, f) -> rotationToSide(w.getBlockData(p)) == f)
			.onRotated((w, p, f) -> true)
			.newData((w, p, f, nf) -> {
				int rot = sideToRotation(nf);
				return rot < 0 ? w.getBlockData(p) : rot;
			});
		register(Blocks.TORCH_COAL, torch);
		register(Blocks.TORCH_REDSTONE_ACTIVE, torch);
		register(Blocks.TORCH_REDSTONE_IDLE, torch);
	}

	private static void registerSideMountedDrop(Block<?> block) {
		register(block, rule()
			.attachedWhen((w, p, f) -> leverSupportSide(w.getBlockData(p)) == f)
			.dropsOnRotate()
		);
	}

	//------------- Rule builder -------------//

	@FunctionalInterface
	public interface AttachCheck {
		boolean test(World world, TilePosc tilePos, Direction facing);
	}

	@FunctionalInterface
	public interface DataMapper {
		int map(World world, TilePosc tilePos, Direction facing, Direction newFacing);
	}

	public static Builder rule() {
		return new Builder();
	}

	public static final class Builder implements TurntableAttachable {
		private AttachCheck attached = (world, tilePos, facing) -> false;
		private AttachCheck onRotated = (world, tilePos, facing) -> false;
		private DataMapper newData = (world, tilePos, facing, newFacing) -> world.getBlockData(tilePos);

		/** When is this block attached to the column block on side `facing`? */
		public Builder attachedWhen(AttachCheck check) { this.attached = check; return this; }

		/** Optional: runs on rotation. Return false to veto (e.g. after dropping the block yourself). */
		public Builder onRotated(AttachCheck handler) { this.onRotated = handler; return this; }

		/** Optional: the block data after the move. Default keeps the current data. */
		public Builder newData(DataMapper mapper) { this.newData = mapper; return this; }

		/** Shortcut for blocks whose data is simply the side id they're attached to. */
		public Builder dataIsAttachedSide() {
			this.attached = (w, p, f) -> w.getBlockData(p) == f.id;
			this.newData = (w, p, f, nf) -> nf.id;
			return this;
		}

		/** Shortcut for blocks that can't survive rotation: they just drop. */
		public Builder dropsOnRotate() {
			this.onRotated = (w, p, f) -> {
				Block<?> b = w.getBlockType(p);
				ItemStack[] drops = b.getLogic().getBreakResult(w, EnumDropCause.PROPER_TOOL, w.getBlockData(p), null);
				if (drops != null) {
					for (ItemStack s : drops) MiscUtils.ejectStackWithRandomOffset(w, p, s);
				}
				w.setBlockTypeNotify(p, Blocks.AIR);
				return false; // handled, don't move it
			};
			return this;
		}

		@Override
		public boolean canRotateAroundBlockOnTurntableToFacing(World w, TilePosc p, Direction f) {
			return attached.test(w, p, f);
		}

		@Override
		public boolean onRotatedAroundBlockOnTurntableToFacing(World w, TilePosc p, Direction f) {
			return onRotated.test(w, p, f);
		}

		@Override
		public int getNewMetadataRotatedAroundBlockOnTurntableToFacing(World w, TilePosc p, Direction f, Direction nf) {
			return newData.map(w, p, f, nf);
		}
	}

	//------------- Facing helpers -------------//

	/** BTW: Block.rotateFacingAroundY. Clockwise seen from above: 2(-z) -> 5(+x) -> 3(+z) -> 4(-x). */
	public static int rotateFacingAroundY(int facing, boolean reverse) {
		int[] cw = {2, 5, 3, 4};
		for (int i = 0; i < 4; i++) {
			if (cw[i] == facing) return cw[(i + (reverse ? 3 : 1)) % 4];
		}
		return facing;
	}

	public static Direction rotateFacingAroundY(Direction facing, boolean reverse) {
		int i = horizontalIndex(facing);
		return i < 0 ? facing : Direction.horizontal[(i + (reverse ? 3 : 1)) % 4];
	}

	public static int horizontalIndex(Direction facing) {
		for (int i = 0; i < 4; i++) {
			if (Direction.horizontal[i] == facing) return i;
		}
		return -1;
	}

	//------------- Data decoding -------------//

	// The ladder faces away from its support block
	private static Direction ladderSupportSide(int data) {
		return Direction.fromId(data).opposite();
	}

	private static Direction leverSupportSide(int data) {
		return rotationToSide(BlockLogicLever.getRotation(data));
	}

	// Lever/torch rotation value <-> the side the block is mounted on
	private static Direction rotationToSide(int rotation) {
		return switch (rotation) {
			case 1 -> Direction.WEST;
			case 2 -> Direction.EAST;
			case 3 -> Direction.NORTH;
			case 4 -> Direction.SOUTH;
			default -> Direction.NONE; // floor/ceiling is never beside the column
		};
	}

	private static int sideToRotation(Direction supportSide) {
		if (supportSide == Direction.WEST)  return 1;
		if (supportSide == Direction.EAST)  return 2;
		if (supportSide == Direction.NORTH) return 3;
		if (supportSide == Direction.SOUTH) return 4;
		return -1;
	}
}
