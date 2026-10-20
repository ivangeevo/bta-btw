package ivangeevo.btx.mixin.client.render;

import ivangeevo.btx.block.blocks.pottery.BlockLogicPlanterGrass;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.block.model.generic.BlockModelGenericShifted;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Tall grass and ferns in a grass planter render centered instead of their random position offset. */
@Mixin(BlockModelGenericShifted.class)
public abstract class MixinBlockModelGenericShifted {
	@Inject(method = "renderAttached", at = @At("HEAD"), cancellable = true)
	private void btx$centerInPlanter(
		TessellatorGeneral tessellator, WorldSource worldSource,
		TilePosc tilePos, boolean cullFaces, IconCoordinate overrideTexture, CallbackInfoReturnable<Boolean> cir
	) {
		Block<?> block = worldSource.getBlockType(tilePos);

		if (block != Blocks.TALLGRASS && block != Blocks.TALLGRASS_FERN) return;

		TilePos posBelow = tilePos.down(new TilePos());
		Block<?> blockBelow = worldSource.getBlockType(posBelow);

		if (!(blockBelow.getLogic() instanceof BlockLogicPlanterGrass)) return;

		BlockModelGeneric<?> model = (BlockModelGeneric<?>) (Object) this;

		cir.setReturnValue(model.staticModel.renderAttached(model, tessellator, worldSource, tilePos,
			0, 0, 0, 0.0, 0.0, 0.0, false, cullFaces, overrideTexture));
	}
}
