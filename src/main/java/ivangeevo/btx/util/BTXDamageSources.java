package ivangeevo.btx.util;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.util.helper.DamageType;
import org.jetbrains.annotations.Nullable;
import turniplabs.halplibe.util.deathcause.DeathCause;
import turniplabs.halplibe.util.deathcause.DeathCauseEvents;
import turniplabs.halplibe.util.deathcause.DeathCauseRegistry;
import turniplabs.halplibe.util.dependency.Key;

// BTA has no damage sources, so the source is remembered on the victim and HalpLibe's death cause
// handlers use it when the victim dies within RECENT_TICKS of the hit
public final class BTXDamageSources {
	public static final String SAW = "saw";

	private static final long RECENT_TICKS = 20L;

	private BTXDamageSources() {
	}

	public static void init(String modId) {
		DeathCauseRegistry.getInstance().register(modId, "source", DeathCauseBTX::new);
		DeathCauseEvents.PLAYER_DEATH_HANDLER.listen(Key.of(modId), BTXDamageSources::resolveDeathCause);
		DeathCauseEvents.MOB_DEATH_HANDLER.listen(Key.of(modId), BTXDamageSources::resolveDeathCause);
	}

	public static boolean hurt(Entity entity, int damage, DamageType type, String source) {
		if (entity instanceof BTXDamageTracked tracked) {
			tracked.btx$setLastDamageSource(source, entity.world.getWorldTime());
		}

		return entity.hurt(null, damage, type);
	}

	private static @Nullable DeathCause resolveDeathCause(Mob victim, @Nullable Entity killedBy) {
		String source = killedBy == null ? getRecentSource(victim) : null;
		return source != null ? new DeathCauseBTX(victim, source) : null;
	}

	private static @Nullable String getRecentSource(Entity entity) {
		if (!(entity instanceof BTXDamageTracked tracked) || tracked.btx$getLastDamageSource() == null) {
			return null;
		}

		long age = entity.world.getWorldTime() - tracked.btx$getLastDamageTime();

		return age >= 0 && age <= RECENT_TICKS ? tracked.btx$getLastDamageSource() : null;
	}
}
