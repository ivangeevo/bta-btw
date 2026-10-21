package ivangeevo.btx.util;

import org.jetbrains.annotations.Nullable;

public interface BTXDamageTracked {
	@Nullable String btx$getLastDamageSource();
	void btx$setLastDamageSource(String source, long worldTime);
	long btx$getLastDamageTime();
}
