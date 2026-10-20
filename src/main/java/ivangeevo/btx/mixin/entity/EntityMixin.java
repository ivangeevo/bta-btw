package ivangeevo.btx.mixin.entity;

import ivangeevo.btx.block.blocks.util.ItemOnTopCollision;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.joml.primitives.AABBd;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow public World world;
	@Shadow @Final public AABBd bb;

	/**
	 * Added so that blocks like Hoppers get collision events with items on top.
	 */
	@Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/entity/Entity;isInWaterOrRain()Z"))
	private void btx$itemCollidesWithBlockBelow(double xd, double yd, double zd, CallbackInfo ci) {
		if ((Object) this instanceof EntityItem) {
			int belowY = MathHelper.floor(this.bb.minY - 0.01);
			int loopMinY = MathHelper.floor(this.bb.minY + 0.001);

			if (belowY >= loopMinY) return; // already covered by the vanilla loop

			int minX = MathHelper.floor(this.bb.minX + 0.001);
			int minZ = MathHelper.floor(this.bb.minZ + 0.001);
			int maxX = MathHelper.floor(this.bb.maxX - 0.001);
			int maxZ = MathHelper.floor(this.bb.maxZ - 0.001);

			AABBd boxToCheck = new AABBd(minX, belowY, minZ, maxX, belowY, maxZ);

			if (!this.world.areBlocksLoaded(boxToCheck)) return;

			TilePos queryPos = new TilePos();
			queryPos.y = belowY;

			for (queryPos.x = minX; queryPos.x <= maxX; queryPos.x++) {
				for (queryPos.z = minZ; queryPos.z <= maxZ; queryPos.z++) {
					Block<?> block = this.world.getBlockType(queryPos);

					if (block.getLogic() instanceof ItemOnTopCollision) {
						block.onEntityCollision(this.world, queryPos, (Entity) (Object) this);
					}
				}
			}
		}
	}
}
