package ivangeevo.btx.mixin.entity;

import ivangeevo.btx.entity.LiftTeleportable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.entity.player.PlayerServer;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.SERVER)
@Mixin(PlayerServer.class)
public abstract class PlayerServerLiftMixin implements LiftTeleportable {
	@Override
	public void btx$liftTeleportTo(double x, double y, double z) {
		((PlayerServer) (Object) this).playerNetServerHandler.teleport(x, y, z);
	}
}