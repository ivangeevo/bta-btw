package ivangeevo.btx.mixin.entity;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicHopper;
import net.minecraft.core.entity.projectile.ProjectileArrow;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.pos.TilePos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileArrow.class)
public abstract class ProjectileArrowMixin {
	@Shadow protected TilePos tilePos;
	@Shadow protected ItemStack stack;

	@Shadow public abstract boolean arrowBelongsToPlayer();

	/** A pickup-able arrow landing in the top of a hopper goes into it, if the filter allows it. **/
	@Inject(method = "inGroundAction", at = @At("HEAD"), cancellable = true)
	private void btx$collectIntoHopper(CallbackInfo ci) {
		ProjectileArrow arrow = (ProjectileArrow) (Object) this;

		if (arrow.world.isClientSide || tilePos == null || stack == null || !arrowBelongsToPlayer()) {
			return;
		}

		if (arrow.world.getBlockType(tilePos) != BTXBlocks.HOPPER) {
			return;
		}

		BlockLogicHopper hopper = BTXBlocks.HOPPER.getLogic();

		boolean inCollectionZone = arrow.x >= tilePos.x && arrow.x <= tilePos.x + 1
			&& arrow.z >= tilePos.z && arrow.z <= tilePos.z + 1
			&& arrow.y >= tilePos.y + 0.9 && arrow.y <= tilePos.y + 1.1;

		if (inCollectionZone && hopper.collectStack(arrow.world, tilePos, stack.copy())) {
			arrow.remove();
			ci.cancel();
		}
	}
}
