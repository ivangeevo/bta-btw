package ivangeevo.btx.networking;

import net.minecraft.core.net.entity.ITrackedEntry;

public interface IOversizedEntry<T> extends ITrackedEntry<T> {
	/**
	 * Partially disables server-side visibility tests for interacting with an entity
	 */
	boolean shouldServerTreatAsOversized();
}
