package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.BlockLogicSaw;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

@Environment(EnvType.CLIENT)
public class BlockModelGenericSaw<T extends BlockLogicSaw> extends BlockModelGeneric<T> {
	// quarter turns {x, y, z} taking the up-facing model to each BTW facing (down, up, north, south, west, east)
	private static final int[][] ROTATIONS = {
		{2, 0, 0},
		{0, 0, 0},
		{3, 0, 0},
		{1, 0, 0},
		{0, 1, 1},
		{0, 1, 3}
	};

	private final StaticBlockModel offModel;
	private final StaticBlockModel onModel;

	public BlockModelGenericSaw(@NotNull Block<T> block) {
		super(block, loadDataModel("btx:block/saw/saw_off"));
		this.offModel = loadDataModel("btx:block/saw/saw_off").asModel();
		this.onModel = loadDataModel("btx:block/saw/saw_on").asModel();
	}

	@Override
	public boolean renderAttached(@NotNull TessellatorGeneral t, @NotNull WorldSource ws, @NotNull TilePosc pos,
								  boolean cullFaces, @Nullable IconCoordinate override) {
		BlockLogicSaw logic = this.block.getLogic();
		int data = ws.getBlockData(pos);
		int[] rotation = ROTATIONS[logic.getFacing(data) % ROTATIONS.length];
		StaticBlockModel model = logic.isBlockOn(data) ? this.onModel : this.offModel;

		return model.renderAttached(this, t, ws, pos, rotation[0], rotation[1], rotation[2], 0.0, 0.0, 0.0, false, cullFaces, override);
	}

	@Override
	public void renderStandalone(@NotNull TessellatorGeneral t, int metadata, byte lightIndex) {
		this.offModel.renderStandalone(this, t, 0.0, 0.0, 0.0, metadata, lightIndex,
			BlockColorDispatcher.getInstance().getDispatch(this.block));
	}
}