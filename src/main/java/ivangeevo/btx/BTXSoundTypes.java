package ivangeevo.btx;

import net.minecraft.core.sound.SoundTypes;

public class BTXSoundTypes {
	public static final String BELLOWS_BLOW = create("bellows.blow");
	public static final String BELLOWS_RETRACT = create("bellows.retract");

	public static void init() {
		SoundTypes.loadSoundsJson("btx");
	}

	private static String create(String key) {
		return BTXMod.MOD_ID + ":" + key; // "btx:bellows.blow"
	}
}
