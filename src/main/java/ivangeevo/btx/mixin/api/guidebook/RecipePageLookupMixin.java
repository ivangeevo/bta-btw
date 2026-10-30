package ivangeevo.btx.mixin.api.guidebook;

import ivangeevo.btx.impl.guidebook.GuidebookLookupImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPageManager;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.crafting.RecipePageCrafting;
import net.minecraft.client.gui.guidebook.smelting.RecipePageBlastSmelting;
import net.minecraft.client.gui.guidebook.smelting.RecipePageSmelting;
import net.minecraft.client.gui.guidebook.trommeling.RecipePageTrommel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin({RecipePageCrafting.class, RecipePageSmelting.class, RecipePageBlastSmelting.class, RecipePageTrommel.class})
public abstract class RecipePageLookupMixin {
	@Redirect(method = "keyTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/guidebook/GuidebookPageManager;getSectionIndex(Lnet/minecraft/client/gui/guidebook/GuidebookSection;)I"))
	private int btx$lookupIndex(GuidebookPageManager manager, GuidebookSection section) {
		return GuidebookLookupImpl.getLookupIndex(manager, section);
	}
}