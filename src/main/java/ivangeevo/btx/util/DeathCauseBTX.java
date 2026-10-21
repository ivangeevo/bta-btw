package ivangeevo.btx.util;

import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.entity.Mob;
import turniplabs.halplibe.util.deathcause.DeathCause;

public class DeathCauseBTX extends DeathCause {
	private String source = "";

	public DeathCauseBTX() {
		super();
	}

	public DeathCauseBTX(Mob victim, String source) {
		super(victim);
		this.source = source;
	}

	@Override
	protected String getTranslationKeyShard() {
		return "btx." + this.source;
	}

	@Override
	protected void serializeAdditional(CompoundTag tag) {
		tag.putString("btx:source", this.source);
	}

	@Override
	protected void deserializeAdditional(CompoundTag tag) {
		this.source = tag.getString("btx:source");
	}
}