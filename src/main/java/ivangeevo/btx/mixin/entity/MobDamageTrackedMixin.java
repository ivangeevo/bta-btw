package ivangeevo.btx.mixin.entity;

import ivangeevo.btx.util.BTXDamageTracked;
import net.minecraft.core.entity.Mob;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Mob.class)
public abstract class MobDamageTrackedMixin implements BTXDamageTracked {
	@Unique
	private @Nullable String btx$lastDamageSource = null;
	@Unique
	private long btx$lastDamageTime = 0L;

	@Override
	public void btx$setLastDamageSource(String source, long worldTime) {
		this.btx$lastDamageSource = source;
		this.btx$lastDamageTime = worldTime;
	}

	@Override
	public @Nullable String btx$getLastDamageSource() {
		return this.btx$lastDamageSource;
	}

	@Override
	public long btx$getLastDamageTime() {
		return this.btx$lastDamageTime;
	}
}