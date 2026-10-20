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
import org.useless.dragonfly.models.block.StaticBlockModel;

import static net.minecraft.client.render.block.model.BlockModelDispatcher.loadDataModel;

public class BlockModelGenericBellows<T extends BlockLogicBellows> extends BlockModelGeneric<T> {

	private static final int[] TURNS_BY_FACING = {0, 0, 0, 2, 3, 1};

	private final StaticBlockModel onModel;

	public BlockModelGenericBellows(@NotNull Block<T> block) {
		super(block, loadDataModel("btx:block/bellows/bellows_off"));
		this.onModel = loadDataModel("btx:block/bellows/bellows_on").asModel();
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

	@Override
	public void renderStandalone(@NotNull TessellatorGeneral t, int metadata, byte lightIndex) {
		this.onModel.renderStandalone(this, t, 0.0, 0.0, 0.0, metadata, lightIndex,
			BlockColorDispatcher.getInstance().getDispatch(this.block));
	}
}
