package ivangeevo.btx.mixin;

import ivangeevo.btx.block.blocks.pottery.BlockLogicVase;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.projectile.ProjectileArrow;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Arrows shatter vases they hit */
@Mixin(ProjectileArrow.class)
public abstract class MixinProjectileArrow {
	@Inject(method = "onHit", at = @At("HEAD"))
	private void btx$shatterVase(HitResult hitResult, CallbackInfo ci) {
		if (!(hitResult instanceof HitResult.Tile tileHit)) return;

		World world = ((Entity) (Object) this).world;
		if (world.isClientSide) return;

		TilePosc pos = tileHit.tilePos;
		if (world.getBlockType(pos).getLogic() instanceof BlockLogicVase vase) {
			vase.breakVase(world, pos);
		}
	}
}
