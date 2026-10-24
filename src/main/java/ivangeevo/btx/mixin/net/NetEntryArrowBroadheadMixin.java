package ivangeevo.btx.mixin.net;

import com.mojang.nbt.tags.CompoundTag;
import ivangeevo.btx.entity.entities.EntityBroadheadArrow;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.net.entity.entries.NetEntryArrow;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetEntryArrow.class)
public abstract class NetEntryArrowBroadheadMixin {
	@Inject(method = "getEntity", at = @At("HEAD"), cancellable = true)
	private void btx$spawnBroadheadArrow(World world, double x, double y, double z, int metadata, boolean hasVelocity,
										 double xd, double yd, double zd, Entity owner, CompoundTag tag, CallbackInfoReturnable<Entity> cir) {
		if (metadata == EntityBroadheadArrow.TYPE_BROADHEAD) {
			cir.setReturnValue(new EntityBroadheadArrow(world, x, y, z));
		}
	}
}