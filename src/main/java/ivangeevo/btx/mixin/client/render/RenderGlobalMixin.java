package ivangeevo.btx.mixin.client.render;

import ivangeevo.btx.util.rendering.PlacementOutline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.client.world.WorldClient;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin {

	@Shadow
	private WorldClient world;

	@Shadow
	@Final
	private Minecraft mc;

	@Inject(method = "drawTileSelectionBoxRaw", at = @At("TAIL"))
    private void btx$placementOutline(
		ICamera camera, TilePosc targetPos, double ox, double oy, double oz, CallbackInfo ci
	) {
        PlacementOutline.draw(this.mc, this.world, (RenderGlobal) (Object) this, targetPos, ox, oy, oz);
    }
}
