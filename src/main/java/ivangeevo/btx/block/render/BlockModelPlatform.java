package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.BlockLogicPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.models.block.StaticBlockModel;

import static net.minecraft.client.render.block.model.BlockModelDispatcher.loadDataModel;

@Environment(EnvType.CLIENT)
public class BlockModelPlatform<T extends BlockLogicPlatform> extends BlockModelGeneric<T> {
	private static final Direction[] SIDES = {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};

	private final StaticBlockModel baseModel;
	private final StaticBlockModel[] sideModels;

	public BlockModelPlatform(@NotNull Block<T> block) {
		super(block, loadDataModel("btx:block/platform/platform_base"));
		this.baseModel = loadDataModel("btx:block/platform/platform_base").asModel();
		this.sideModels = new StaticBlockModel[]{
			loadDataModel("btx:block/platform/platform_west").asModel(),
			loadDataModel("btx:block/platform/platform_east").asModel(),
			loadDataModel("btx:block/platform/platform_north").asModel(),
			loadDataModel("btx:block/platform/platform_south").asModel()
		};
	}

	@Override
	public boolean renderAttached(@NotNull TessellatorGeneral t, @NotNull WorldSource ws, @NotNull TilePosc pos,
								  boolean cullFaces, @Nullable IconCoordinate override) {
		boolean rendered = this.baseModel.renderAttached(this, t, ws, pos, 0, 0, 0, 0.0, 0.0, 0.0, false, false, override);

		for (int i = 0; i < SIDES.length; i++) {
			TilePos neighbor = new TilePos(pos);
			neighbor.add(SIDES[i]);

			if (ws.getBlockType(neighbor) != this.block) {
				rendered |= this.sideModels[i].renderAttached(this, t, ws, pos, 0, 0, 0, 0.0, 0.0, 0.0, false, false, override);
			}
		}

		return rendered;
	}

	@Override
	public void renderStandalone(@NotNull TessellatorGeneral t, int metadata, byte lightIndex) {
		this.baseModel.renderStandalone(this, t, 0.0, 0.0, 0.0, 0, lightIndex, BlockColorDispatcher.getInstance().getDispatch(this.block));

		for (int i = 0; i < this.sideModels.length; i++) {
			if ((metadata & (1 << i)) == 0) {
				this.sideModels[i].renderStandalone(this, t, 0.0, 0.0, 0.0, 0, lightIndex, BlockColorDispatcher.getInstance().getDispatch(this.block));
			}
		}
	}
}
