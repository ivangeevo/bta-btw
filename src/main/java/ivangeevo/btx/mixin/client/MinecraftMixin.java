package ivangeevo.btx.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ivangeevo.btx.util.IExactHitPlacement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.controller.PlayerController;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.IPlaceable;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

	@Shadow public HitResult objectMouseOver;

	@WrapOperation(
		method = "clickMouse",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/controller/PlayerController;useOrPlaceItemStackOnTile(Lnet/minecraft/core/entity/player/Player;Lnet/minecraft/core/world/World;Lnet/minecraft/core/item/ItemStack;Lnet/minecraft/core/world/pos/TilePosc;Lnet/minecraft/core/util/helper/Side;DD)Z"
		)
	)
	private boolean btx$sendExactHit(
		PlayerController controller, Player player, World world, ItemStack stack,
		TilePosc tilePos, Side side, double xPlaced, double yPlaced, Operation<Boolean> original
	) {
		if (stack != null
			&& stack.getItem() instanceof IPlaceable.PlaceableBlock<?> placeable
			&& placeable.getBlock().getLogic() instanceof IExactHitPlacement
			&& this.objectMouseOver instanceof HitResult.Tile hit) {

			double[] packed = IExactHitPlacement.encodeFromHit(hit);
			xPlaced = packed[0];
			yPlaced = packed[1];
		}

		return original.call(controller, player, world, stack, tilePos, side, xPlaced, yPlaced);
	}
}
