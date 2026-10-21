package ivangeevo.btx.mixin.entity;

import ivangeevo.btx.item.items.ItemArmorSoulforged;
import ivangeevo.btx.util.SoulforgedSounds;
import ivangeevo.btx.util.SoulforgedToolHelper;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackSoulforgedArmorMixin {
	@Inject(method = "damageItem", at = @At("HEAD"), cancellable = true)
	private void btx$soulforgedGearNeverShatters(int amount, Entity entity, CallbackInfo ci) {
		ItemStack stack = (ItemStack) (Object) this;

		if (!ItemArmorSoulforged.isSoulforged(stack) && !SoulforgedToolHelper.isSoulforgedTool(stack.getItem())) {
			return;
		}

		if (entity instanceof Player player && !player.getGamemode().hasToolDurability()) {
			return;
		}

		int max = stack.getMaxDamage();
		boolean wasIntact = stack.getMetadata() < max;

		stack.setMetadata(Math.min(stack.getMetadata() + amount, max));

		if (wasIntact && stack.getMetadata() >= max && entity != null && !entity.world.isClientSide) {
			SoulforgedSounds.playSoulPowerDown(entity.world, entity);
		}

		ci.cancel();
	}

	@Inject(method = "getStrVsBlock", at = @At("HEAD"), cancellable = true)
	private void btx$brokenToolMinesLikeHand(Block<?> block, CallbackInfoReturnable<Float> cir) {
		if (SoulforgedToolHelper.isBrokenTool((ItemStack) (Object) this)) {
			cir.setReturnValue(1.0F);
		}
	}

	@Inject(method = "canHarvestBlock", at = @At("HEAD"), cancellable = true)
	private void btx$brokenToolCannotHarvest(Mob mob, Block<?> block, CallbackInfoReturnable<Boolean> cir) {
		if (SoulforgedToolHelper.isBrokenTool((ItemStack) (Object) this)) {
			cir.setReturnValue(false);
		}
	}
}
