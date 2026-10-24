package ivangeevo.btx.mixin.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.PlayerSkinParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.image.BufferedImage;

@Environment(EnvType.CLIENT)
@Mixin(PlayerSkinParser.class)
public abstract class PlayerSkinParserMixin {
	@Inject(method = "parseImage", at = @At("RETURN"))
	private void btx$clearOpaqueLegacyHat(BufferedImage image, CallbackInfoReturnable<BufferedImage> cir) {
		BufferedImage result = cir.getReturnValue();

		if (image == null || result == null || image.getHeight() != 32) {
			return;
		}

		for (int x = 32; x < 64; x++) {
			for (int y = 0; y < 16; y++) {
				if ((result.getRGB(x, y) >>> 24) != 0xFF) {
					return;
				}
			}
		}

		for (int x = 32; x < 64; x++) {
			for (int y = 0; y < 16; y++) {
				result.setRGB(x, y, 0);
			}
		}
	}
}