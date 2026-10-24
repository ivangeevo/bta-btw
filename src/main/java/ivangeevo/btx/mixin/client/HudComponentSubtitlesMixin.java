package ivangeevo.btx.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.hud.component.HudComponentSubtitles;
import net.minecraft.client.option.GameSettings;
import net.minecraft.core.enums.HorizontalTextAlignment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// vanilla only aligns the box while it fits the component, a wider box always grows right and off a right anchored screen
@Mixin(HudComponentSubtitles.class)
public abstract class HudComponentSubtitlesMixin {
	@ModifyVariable(method = "drawChat", at = @At("STORE"), name = "blockLeft")
	private int btx$keepWideBoxOnScreen(int blockLeft,
										@Local(argsOnly = true, ordinal = 0) int xSizeScreen,
										@Local(name = "x") int x,
										@Local(name = "width") int width,
										@Local(name = "boxWidth") int boxWidth
	) {
		if (boxWidth <= width) {
			return blockLeft;
		}

		float xMult = ((HorizontalTextAlignment) GameSettings.HORIZONTAL_SUBTITLE_TEXT_ALIGNMENT.value).multiplier;
		int aligned = x + Math.round((width - boxWidth) * xMult);

		return Math.max(0, Math.min(aligned, xSizeScreen - boxWidth));
	}
}
