package ivangeevo.btx.mixin.gui;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.gui.guidebook.BTXGuidebook;
import ivangeevo.btx.gui.guidebook.GuidebookSectionBulk;
import ivangeevo.btx.gui.guidebook.GuidebookSectionCoverBTX;
import ivangeevo.btx.gui.guidebook.GuidebookSectionIndexBTX;
import ivangeevo.btx.gui.guidebook.GuidebookSectionKiln;
import ivangeevo.btx.gui.guidebook.GuidebookSectionMillstone;
import ivangeevo.btx.gui.guidebook.GuidebookSectionSaw;
import ivangeevo.btx.gui.guidebook.GuidebookSectionSoulforge;
import ivangeevo.btx.gui.guidebook.GuidebookSectionTurntable;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.recipe.BTXRecipes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.GuidebookSections;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(GuidebookSections.class)
public abstract class GuidebookMixinInclude {
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/guidebook/GuidebookSections;register(Lnet/minecraft/client/gui/guidebook/GuidebookSection;)Lnet/minecraft/client/gui/guidebook/GuidebookSection;", ordinal = 6, shift = At.Shift.AFTER))
    private static void includeBTXSections(CallbackInfo ci) {
        BTXGuidebook.register(new GuidebookSectionCoverBTX(
			"btx.guidebook.section.cover", new ItemStack(BTXItems.GEAR), 0x3A7A36, 0xC29A1C)
		);
        BTXGuidebook.register(new GuidebookSectionIndexBTX(
			"guidebook.section.index", new ItemStack(Items.TOOL_COMPASS), 0x0094FF, 0xC29A1C)
		);
        BTXGuidebook.register(GuidebookSections.SEARCH);
        BTXGuidebook.register(new GuidebookSectionMillstone(
			"btx.guidebook.section.millstone", new ItemStack(BTXBlocks.MILLSTONE), 0x606060, 0x00A29C)
		);
        BTXGuidebook.register(new GuidebookSectionTurntable(
			"btx.guidebook.section.turntable", new ItemStack(BTXBlocks.TURNTABLE), 0x6B4F2A, 0xC8A060)
		);
        BTXGuidebook.register(new GuidebookSectionSaw(
			"btx.guidebook.section.saw", new ItemStack(BTXBlocks.SAW), 0x8B6A3E, 0xD0D0D0)
		);
        BTXGuidebook.register(new GuidebookSectionKiln(
			"btx.guidebook.section.kiln", new ItemStack(Blocks.BRICK_CLAY), 0x7A3B2E, 0xD06040)
		);
        BTXGuidebook.register(new GuidebookSectionBulk(
			"btx.guidebook.section.cauldron", new ItemStack(BTXBlocks.CAULDRON), 0x404040, 0xC06030,
			() -> BTXRecipes.CAULDRON, () -> BTXRecipes.CAULDRON_STOKED)
		);
        BTXGuidebook.register(new GuidebookSectionBulk(
			"btx.guidebook.section.crucible", new ItemStack(BTXBlocks.CRUCIBLE), 0x5A3A2A, 0xE0A040,
			() -> BTXRecipes.CRUCIBLE, () -> BTXRecipes.CRUCIBLE_STOKED)
		);
        BTXGuidebook.register(new GuidebookSectionSoulforge(
			"btx.guidebook.section.soulforge", new ItemStack(BTXBlocks.SOULFORGE), 0x3A2E2E, 0x8B1A1A)
		);
    }

    @Inject(method = "getSectionList", at = @At("HEAD"), cancellable = true)
    private static void useActiveBookSections(CallbackInfoReturnable<List<GuidebookSection>> cir) {
        if (BTXGuidebook.getActive() == BTXGuidebook.Book.BTX) {
            cir.setReturnValue(BTXGuidebook.getSections(BTXGuidebook.Book.BTX));
        }
    }
}
