package ivangeevo.btx.particle;

import net.minecraft.client.render.particle.ParticleSmoke;
import net.minecraft.core.world.World;

public class ParticleWhiteSmoke extends ParticleSmoke {
	private static final float SCALE = 2.5F;

	public ParticleWhiteSmoke(World world, double x, double y, double z, double xa, double ya, double za) {
		super(world, x, y, z, xa, ya, za, SCALE, 0);

		this.rCol = this.gCol = this.bCol = 1.0F - (float) (Math.random() * 0.3);

		this.lifetime = (int) (8.0 / (Math.random() * 0.8 + 0.3));
		this.lifetime = (int) (this.lifetime * SCALE);
	}
}
