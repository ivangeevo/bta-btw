package ivangeevo.btx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BooleanSupplier;

public class BTXGlobals {
	public static final Logger LOGGER = LoggerFactory.getLogger(BTXMod.MOD_ID);

	/**
	 * Whether the local player breaks blocks instantly (creative). Set by BTXModClient; always false on a
	 * dedicated server. For block code that has no player, like collisionRayTrace.
	 */
	public static BooleanSupplier localPlayerInstantBreaks = () -> false;
}
