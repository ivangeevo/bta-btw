package ivangeevo.btx.util;

import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public class SoundUtils {

	public static void playAtTileCenter(World world, TilePosc pos, SoundCategory category, String soundPath, float volume, float pitch) {
		world.playSoundEffect(null, category, pos.x() + 0.5f, pos.y() + 0.5f, pos.z() + 0.5f, soundPath, volume, pitch);
	}

}
