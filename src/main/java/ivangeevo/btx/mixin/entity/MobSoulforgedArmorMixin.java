package ivangeevo.btx.mixin.entity;

import ivangeevo.btx.util.SoulforgedArmorHelper;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobSoulforgedArmorMixin {
	@Unique private double btx$knockbackStartXd;
	@Unique private double btx$knockbackStartYd;
	@Unique private double btx$knockbackStartZd;

	@Inject(method = "knockBack", at = @At("HEAD"))
	private void btx$captureVelocityBeforeKnockback(Entity attacker, int damage, double dx, double dz, CallbackInfo ci) {
		Mob mob = (Mob) (Object) this;
		this.btx$knockbackStartXd = mob.xd;
		this.btx$knockbackStartYd = mob.yd;
		this.btx$knockbackStartZd = mob.zd;
	}

	@Inject(method = "knockBack", at = @At("TAIL"))
	private void btx$plateResistsKnockback(Entity attacker, int damage, double dx, double dz, CallbackInfo ci) {
		if (!((Object) this instanceof Player player)) {
			return;
		}

		int pieces = SoulforgedArmorHelper.intactPieces(player);
		if (pieces <= 0) {
			return;
		}

		double kept = Math.max(0.0D, 1.0D - SoulforgedArmorHelper.KNOCKBACK_REDUCTION_PER_PIECE * pieces);
		player.xd = this.btx$knockbackStartXd + (player.xd - this.btx$knockbackStartXd) * kept;
		player.yd = this.btx$knockbackStartYd + (player.yd - this.btx$knockbackStartYd) * kept;
		player.zd = this.btx$knockbackStartZd + (player.zd - this.btx$knockbackStartZd) * kept;
	}

	//@ModifyConstant(method = "onLivingUpdate", constant = @Constant(doubleValue = 0.04D, ordinal = 0))
	private double btx$plateWeakensSwimming(double original) {
		if ((Object) this instanceof Player player) {
			int pieces = SoulforgedArmorHelper.piecesWorn(player);
			return original * Math.max(0.0D, 1.0D - SoulforgedArmorHelper.SWIM_REDUCTION_PER_PIECE * pieces);
		}
		return original;
	}

	@ModifyArg(
		method = "causeFallDamage",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/core/entity/Mob;hurt(Lnet/minecraft/core/entity/Entity;ILnet/minecraft/core/util/helper/DamageType;)Z"),
		index = 1
	)
	private int btx$plateIncreasesFallDamage(int damage) {
		if ((Object) this instanceof Player player) {
			int pieces = SoulforgedArmorHelper.piecesWorn(player);
			if (pieces > 0) {
				return Math.round(damage * (1.0F + SoulforgedArmorHelper.FALL_DAMAGE_INCREASE_PER_PIECE * pieces));
			}
		}
		return damage;
	}
}
