package ivangeevo.btx.mixin.entity;

import ivangeevo.btx.entity.LiftTeleportable;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public abstract class PlayerLiftMixin implements LiftTeleportable {
	@Override
	public void btx$liftTeleportTo(double x, double y, double z) {
		((Player) (Object) this).setPos(x, y, z);
	}
}