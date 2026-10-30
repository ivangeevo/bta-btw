package ivangeevo.btx.mixin.api.guidebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPageManager;
import net.minecraft.client.gui.guidebook.PageNavigationStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(GuidebookPageManager.class)
public interface GuidebookPageManagerAccessor {
	@Accessor("stack") PageNavigationStack btx$getStack();
}