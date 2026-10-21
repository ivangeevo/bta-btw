package ivangeevo.btx.util;

import ivangeevo.btx.sound.BTXSoundTypes;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.World;

public class SoulforgedSounds {
	public static void playSoulActivate(World world, Entity entity) {
		play(world, entity, BTXSoundTypes.SOULFORGED_ACTIVATE, 0.6F, 1.25F);
		play(world, entity, BTXSoundTypes.SOULFORGED_ACTIVATE_IGNITE, 0.35F, 1.4F);
		play(world, entity, BTXSoundTypes.SOULFORGED_ACTIVATE_HUM, 0.12F, 1.9F);
		play(world, entity, BTXSoundTypes.SOULFORGED_ACTIVATE_MOAN, 0.06F, 1.8F);
	}

	public static void playSoulDeactivate(World world, Entity entity) {
		play(world, entity, BTXSoundTypes.SOULFORGED_DEACTIVATE, 0.6F, 1.0F);
		play(world, entity, BTXSoundTypes.SOULFORGED_DEACTIVATE_FIZZ, 0.2F, 1.6F);
	}

	public static void playSoulPowerDown(World world, Entity entity) {
		play(world, entity, BTXSoundTypes.SOULFORGED_POWER_DOWN, 0.8F, 0.6F);
		play(world, entity, BTXSoundTypes.SOULFORGED_POWER_DOWN_FIZZ, 0.5F, 0.6F);
		play(world, entity, BTXSoundTypes.SOULFORGED_POWER_DOWN_MOAN, 0.25F, 0.5F);
	}

	private static void play(World world, Entity entity, String sound, float volume, float pitch) {
		world.playSoundAtEntity(null, entity, sound, volume, pitch);
	}
}
