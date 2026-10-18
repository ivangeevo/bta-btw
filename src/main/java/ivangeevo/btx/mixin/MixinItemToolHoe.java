package ivangeevo.btx.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ivangeevo.btx.item.HoeDrops;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.tool.ItemToolHoe;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemToolHoe.class)
public abstract class MixinItemToolHoe {
	@Unique private static final ThreadLocal<Block<?>> btx$tilled = new ThreadLocal<>();

	@Inject(method = "till", at = @At("HEAD"))
	private void btx$remember(
		ItemStack stack, Player player, World world, TilePosc pos, Side side, CallbackInfoReturnable<Boolean> cir
	) {
		btx$tilled.set(world.getBlockType(pos));
	}

	// skip the vanilla wheat seed drop where a rule replaces it
	@WrapOperation(
		method = "till",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/core/world/World;dropItem(Lnet/minecraft/core/world/pos/TilePosc;Lnet/minecraft/core/item/ItemStack;)Lnet/minecraft/core/entity/EntityItem;")
	)
	private EntityItem btx$maybeSkipWheat(World instance, TilePosc tilePos, ItemStack itemStack, Operation<EntityItem> original) {
		Block<?> before = btx$tilled.get();
		if (before != null && HoeDrops.replacesVanillaSeeds(before)) return null;
		return original.call(instance, tilePos, itemStack);
	}

	@Inject(method = "till", at = @At("RETURN"))
	private void btx$tillDrops(ItemStack stack, Player player, World world, TilePosc pos, Side side,
							   CallbackInfoReturnable<Boolean> cir) {
		Block<?> before = btx$tilled.get();
		btx$tilled.remove();
		if (cir.getReturnValueZ() && !world.isClientSide && before != null) {
			HoeDrops.rollTill(new HoeDrops.Context(world, pos, before, stack, player), pos.up(new TilePos()));
		}
	}
}
