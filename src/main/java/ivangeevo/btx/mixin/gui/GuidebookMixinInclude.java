package ivangeevo.btx.mixin.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookSections;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GuidebookSections.class)
public abstract class GuidebookMixinInclude {
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/guidebook/GuidebookSections;register(Lnet/minecraft/client/gui/guidebook/GuidebookSection;)Lnet/minecraft/client/gui/guidebook/GuidebookSection;", ordinal = 6, shift = At.Shift.AFTER))
    private static void includeAetherSection(CallbackInfo ci) {
        //GuidebookSections.register(new GuidebookSectionMillstone("btx.guidebook.section.millstone", new ItemStack(BTXBlocks.MILLSTONE), 0x606060, 0x00A29C));
    }
}
