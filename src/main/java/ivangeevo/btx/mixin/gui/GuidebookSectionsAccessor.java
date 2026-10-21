package ivangeevo.btx.mixin.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookSection;
import net.minecraft.client.gui.guidebook.GuidebookSections;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(GuidebookSections.class)
public interface GuidebookSectionsAccessor {
	@Accessor("sectionList")
	static List<GuidebookSection> btx$getSectionList() {
		throw new AssertionError();
	}
}