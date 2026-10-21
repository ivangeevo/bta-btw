package ivangeevo.btx.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import ivangeevo.btx.util.SoulforgedArmorHelper;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.DamageType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerSoulforgedArmorMixin {
	@ModifyExpressionValue(
		method = "damageEntity",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/core/entity/player/Player;getTotalProtectionAmount(Lnet/minecraft/core/util/helper/DamageType;)F")
	)
	private float btx$brokenPlateGivesNoProtection(float original, @Local(argsOnly = true) DamageType damageType) {
		return original - SoulforgedArmorHelper.brokenProtection((Player) (Object) this, damageType);
	}

	@Inject(method = "onLivingUpdate", at = @At("TAIL"))
	private void btx$plateWeightAndTempering(CallbackInfo ci) {
		Player player = (Player) (Object) this;
		int pieces = SoulforgedArmorHelper.piecesWorn(player);

		if (pieces > 0) {
			player.speed *= 1.0F - SoulforgedArmorHelper.SPEED_REDUCTION_PER_PIECE * pieces;
			//if (player.isInWater()) {
				//player.yd -= SoulforgedArmorHelper.SINK_PER_PIECE * pieces;
			//}
		}

		if (player.remainingFireTicks > 0 && SoulforgedArmorHelper.hasFullIntactSet(player)) {
			player.remainingFireTicks = 0;
		}
	}
}
