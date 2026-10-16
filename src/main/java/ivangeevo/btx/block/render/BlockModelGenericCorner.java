package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.mini_blocks.BlockLogicCorner;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.data.block.BlockModelData;

public class BlockModelGenericCorner<T extends BlockLogic> extends BlockModelGeneric<T> {
	public BlockModelGenericCorner(@NotNull Block<T> block, @NotNull BlockModelData data) {
		super(block, data);
	}

	@Override
	public boolean renderAttached(@NotNull TessellatorGeneral tessellator, @NotNull WorldSource worldSource, @NotNull TilePosc tilePos, boolean cullFaces, @Nullable IconCoordinate overrideTexture) {
		int data = worldSource.getBlockData(tilePos);
		return this.getModelFromData(data).renderAttached(this, tessellator, worldSource, tilePos, 0, 0, 0,
			BlockLogicCorner.isXOffset(data) ? 0.25 : -0.25,
			BlockLogicCorner.isYOffset(data) ? 0.25 : -0.25,
			BlockLogicCorner.isZOffset(data) ? 0.25 : -0.25,
			false, cullFaces, overrideTexture
		);
	}

}
