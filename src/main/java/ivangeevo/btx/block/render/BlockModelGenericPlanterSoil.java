package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.pottery.BlockLogicPlanterSoil;
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

public class BlockModelGenericPlanterSoil<T extends BlockLogicPlanterSoil> extends BlockModelGeneric<T> {
	private final StaticBlockModel fertilizedModel;

	public BlockModelGenericPlanterSoil(@NotNull Block<T> block) {
		super(block, loadDataModel("btx:block/planter/planter_soil"));
		this.fertilizedModel = loadDataModel("btx:block/planter/planter_soil_fertilized").asModel();
	}

	@Override
	public boolean renderAttached(@NotNull TessellatorGeneral t, @NotNull WorldSource ws, @NotNull TilePosc pos,
								  boolean cullFaces, @Nullable IconCoordinate override) {
		int data = ws.getBlockData(pos);
		StaticBlockModel model = BlockLogicPlanterSoil.isFertilized(data) ? this.fertilizedModel : getModelFromData(data);

		return model.renderAttached(this, t, ws, pos, 0, 0, 0, 0.0, 0.0, 0.0, true, cullFaces, override);
	}

	@Override
	public void renderStandalone(@NotNull TessellatorGeneral t, int metadata, byte lightIndex) {
		getModelFromData(0).renderStandalone(this, t, 0.0, 0.0, 0.0, metadata, lightIndex,
			BlockColorDispatcher.getInstance().getDispatch(this.block));
	}
}
