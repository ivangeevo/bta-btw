package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.BlockLogicBellows;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.data.block.BlockModelData;
import org.useless.dragonfly.models.block.StaticBlockModel;

public class BlockModelGenericBellows<T extends BlockLogicBellows> extends BlockModelGeneric<T> {

	/**
	 * Quarter turns around Y, indexed by facing (2..5). The base pose has the front on +z (facing 3).
	 * ASSUMPTION: I don't know which way a positive turn rotates. If the bellows front points the wrong
	 * way on the x axis (facing 4 and 5), swap those two entries.
	 */
	private static final int[] TURNS_BY_FACING = {0, 0, 2, 0, 1, 3};

	private final StaticBlockModel onModel;

	public BlockModelGenericBellows(@NotNull Block<T> block, @NotNull BlockModelData off, @NotNull BlockModelData on) {
		super(block, off);
		this.onModel = on.asModel();
	}

	@Override
	public boolean renderAttached(@NotNull TessellatorGeneral t, @NotNull WorldSource ws, @NotNull TilePosc pos,
								  boolean cullFaces, @Nullable IconCoordinate override) {
		BlockLogicBellows logic = this.block.getLogic();
		int data = ws.getBlockData(pos);

		int facing = logic.getFacing(data);
		int turns = TURNS_BY_FACING[facing];

		StaticBlockModel model = logic.isBlockMechanicalOn(ws, pos) ? this.onModel : getModelFromData(data);

		return model.renderAttached(this, t, ws, pos, 0, turns, 0, 0.0, 0.0, 0.0, true, cullFaces, override);
	}

	/** Inventory icon: extended bellows, front on +z. */
	@Override
	public void renderStandalone(@NotNull TessellatorGeneral t, int metadata, byte lightIndex) {
		getModelFromData(0).renderStandalone(this, t, 0.0, 0.0, 0.0, metadata, lightIndex,
			BlockColorDispatcher.getInstance().getDispatch(this.block));
	}
}
