package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.BlockLogicPaneBTX;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.models.block.StaticBlockModel;

import static net.minecraft.client.render.block.model.BlockModelDispatcher.loadDataModel;

public class BlockModelGenericPane<T extends BlockLogicPaneBTX> extends BlockModelGeneric<T> {
	private final StaticBlockModel half;

	public BlockModelGenericPane(@NotNull Block<T> block, @NotNull String halfModelKey) {
		super(block, loadDataModel(halfModelKey));
		this.half = loadDataModel(halfModelKey).asModel();
	}

	@Override
	public boolean renderAttached(
		@NotNull TessellatorGeneral t, @NotNull WorldSource ws, @NotNull TilePosc pos,
		boolean cullFaces, @Nullable IconCoordinate override
	) {
		T logic = this.block.getLogic();

		boolean north = logic.canConnectTo(ws, pos, Side.NORTH);
		boolean south = logic.canConnectTo(ws, pos, Side.SOUTH);
		boolean west = logic.canConnectTo(ws, pos, Side.WEST);
		boolean east = logic.canConnectTo(ws, pos, Side.EAST);

		if (!north && !south && !west && !east) {
			north = south = west = east = true; // CE: an unconnected pane is a cross
		}

		boolean drew = false;

		if (north) {
			drew |= this.half.renderAttached(this, t, ws, pos, 0, 0, 0, 0.0, 0.0, 0.0, false, cullFaces, override);
		}

		if (west) {
			drew |= this.half.renderAttached(this, t, ws, pos, 0, 1, 0, 0.0, 0.0, 0.0, true, cullFaces, override);
		}

		if (south) {
			drew |= this.half.renderAttached(this, t, ws, pos, 0, 2, 0, 0.0, 0.0, 0.0, true, cullFaces, override);
		}

		if (east) {
			drew |= this.half.renderAttached(this, t, ws, pos, 0, 3, 0, 0.0, 0.0, 0.0, true, cullFaces, override);
		}

		return drew;
	}

	@Override
	public void renderStandalone(@NotNull TessellatorGeneral t, int data, byte lightIndex) {
		this.half.renderStandalone(this, t, 0.0, 0.0, 0.0, 0, lightIndex,
			BlockColorDispatcher.getInstance().getDispatch(this.block));
	}
}
