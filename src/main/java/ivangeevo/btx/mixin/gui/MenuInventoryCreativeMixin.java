package ivangeevo.btx.mixin.gui;

import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.menu.MenuInventoryCreative;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Locale;

/**
 * Creative search by namespace: "@btx" lists every block and item registered under btx (block items carry
 * their block's id, so blocks show up too). A partial namespace works ("@bt"). Plain searches are unchanged.
 */
@Mixin(MenuInventoryCreative.class)
public abstract class MenuInventoryCreativeMixin {
	@Shadow protected List<ItemStack> searchedItems;
	@Shadow public String searchText;

	@Inject(method = "refillSearchedItemsMatchingCurrentQuery", at = @At("HEAD"), cancellable = true)
	private void btx$searchByNamespace(CallbackInfo ci) {
		if (searchText == null || !searchText.startsWith("@")) {
			return;
		}

		String namespace = searchText.substring(1).toLowerCase(Locale.ROOT);

		searchedItems.clear();

		for (ItemStack stack : MenuInventoryCreative.creativeContents) {
			if (stack != null && stack.getItem() != null
				&& stack.getItem().namespaceID.namespace().toLowerCase(Locale.ROOT).startsWith(namespace)) {
				searchedItems.add(stack);
			}
		}

		ci.cancel();
	}
}
