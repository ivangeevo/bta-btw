package ivangeevo.btx.util;

import ivangeevo.btx.block.blocks.turntable.TurntableInteraction;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class TurntableHelper {

	@FunctionalInterface
	public interface Check {
		boolean test(WorldSource source, TilePosc tilePos);
	}

	public record Behavior(Check canRotate, Check canTransmitHorizontally, Check canTransmitVertically) {
		public static Behavior rotate(Check canRotate) {
			return new Behavior(canRotate, null, null);
		}
		public static Behavior NONE = new Behavior(
			(world, tilePos) -> false,
			(world, tilePos) -> false,
			(world, tilePos) -> false
		);
	}

	private static final Map<Block<?>, Behavior> BY_BLOCK = new HashMap<>();
	private static final Map<Class<? extends BlockLogic>, Behavior> BY_CLASS = new HashMap<>();
	private static final Map<Class<?>, Behavior> CLASS_CACHE = new ConcurrentHashMap<>();
	private static final Behavior NO_ENTRY = new Behavior(null, null, null);

	public static void register(Block block, Behavior behavior) {
		BY_BLOCK.put(block, behavior);
	}

	public static void register(Class<? extends BlockLogic> logicClass, Behavior behavior) {
		BY_CLASS.put(logicClass, behavior);
		CLASS_CACHE.clear();
	}

	private static boolean defaultCheck(WorldSource source, TilePosc tilePos) {
		return source.isBlockNormalCube(tilePos);
	}

	private static Behavior find(Block<?> block) {
		BlockLogic logic = block.getLogic();

		if (logic instanceof TurntableInteraction aware) {
			return new Behavior(
				aware::canRotateOnTurntable,
				aware::canTransmitRotationHorizontally,
				aware::canTransmitRotationVertically
			);
		}

		Behavior behavior = BY_BLOCK.get(block);
		if (behavior != null) {
			return behavior;
		}

		behavior = CLASS_CACHE.computeIfAbsent(logic.getClass(), c -> {
			for (Class<?> clazz = c; clazz != null; clazz = clazz.getSuperclass()) {
				Behavior foundBehavior = BY_CLASS.get(clazz);
				if (foundBehavior != null) {
					return foundBehavior;
				}
			}

			return NO_ENTRY;
		});

		return behavior == NO_ENTRY ? null : behavior;
	}

	private static Check pick(Block<?> block, Function<Behavior, Check> getter) {
		Behavior behavior = find(block);
		Check check = behavior == null ? null : getter.apply(behavior);
		return check != null ? check : TurntableHelper::defaultCheck;
	}

	public static boolean canRotate(WorldSource source, TilePosc tilePos, Block<?> block) {
		return pick(block, Behavior::canRotate).test(source, tilePos);
	}

	public static boolean canTransmitHorizontally(WorldSource source, TilePosc tilePos, Block<?> block) {
		return pick(block, Behavior::canTransmitHorizontally).test(source, tilePos);
	}

	public static boolean canTransmitVertically(WorldSource source, TilePosc tilePos, Block<?> block) {
		return pick(block, Behavior::canTransmitVertically).test(source, tilePos);
	}
}
