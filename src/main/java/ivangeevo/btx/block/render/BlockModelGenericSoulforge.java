package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.BlockLogicSoulforge;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.client.render.block.model.BlockModelDispatcher.loadDataModel;

public class BlockModelGenericSoulforge<T extends BlockLogicSoulforge> extends BlockModelGeneric<T> {
	private static final int[] TURNS_BY_FACING = {0, 0, 0, 2, 3, 1};

	public BlockModelGenericSoulforge(@NotNull Block<T> block) {
		super(block, loadDataModel("btx:block/soulforge"));
	}

	@Override
	public boolean renderAttached(@NotNull TessellatorGeneral t, @NotNull WorldSource ws, @NotNull TilePosc pos,
								  boolean cullFaces, @Nullable IconCoordinate override) {
		int data = ws.getBlockData(pos);
		int turns = TURNS_BY_FACING[BlockLogicSoulforge.getFacing(data)];
		return getModelFromData(data).renderAttached(this, t, ws, pos, 0, turns, 0, 0.0, 0.0, 0.0, true, cullFaces, override);
	}
}