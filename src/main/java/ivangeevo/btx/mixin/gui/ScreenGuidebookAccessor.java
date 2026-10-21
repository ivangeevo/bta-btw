package ivangeevo.btx.mixin.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.ScreenGuidebook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(ScreenGuidebook.class)
public interface ScreenGuidebookAccessor {
	@Invoker("initTabList") void btx$initTabList();
}
