package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.mini_blocks.BlockLogicSiding;
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

public class BlockModelGenericSiding<T extends BlockLogic> extends BlockModelGeneric<T> {
	public BlockModelGenericSiding(@NotNull Block<T> block, @NotNull BlockModelData data) {
		super(block, data);
	}

	@Override
	public boolean renderAttached(@NotNull TessellatorGeneral tessellator, @NotNull WorldSource worldSource, @NotNull TilePosc tilePos, boolean cullFaces, @Nullable IconCoordinate overrideTexture) {
		int rx = 0, ry = 0, rz = 0;
		switch (BlockLogicSiding.getFacing(worldSource.getBlockData(tilePos))) {
			case 0 -> {
				rz = 3;
				ry = 3;
			}
			case 1 -> {
				rz = 1;
				ry = 3;
			}
			case 3 -> ry = 2;
			case 4 -> ry = 1;
			case 5 -> ry = 3;
			default -> {}
		}
		return getModel(worldSource, tilePos).renderAttached(
			this, tessellator, worldSource, tilePos,
			rx, ry, rz,
			0.0, 0.0, 0.0,
			true, cullFaces, overrideTexture
		);
	}

}
