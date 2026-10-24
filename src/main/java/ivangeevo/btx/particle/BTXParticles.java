package ivangeevo.btx.particle;

import ivangeevo.btx.particle.particles.ParticleWhiteSmoke;
import net.minecraft.client.render.particle.ParticleDispatcher;
import net.minecraft.client.render.particle.ParticleEntry;

public class BTXParticles {
	public static final String WHITE_SMOKE = "whitesmoke";

	public static void init() {
		// after start: ParticleDispatcher grabs the Minecraft instance when it's first created
		registerParticle(WHITE_SMOKE, ParticleWhiteSmoke::new);
	}

	private static void registerParticle(String id, ParticleEntry entry) {
		ParticleDispatcher.getInstance().addDispatch(id, entry);
	}
}
