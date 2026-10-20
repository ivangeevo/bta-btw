package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.BlockLogicCompanionCube;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.models.block.StaticBlockModel;

import static net.minecraft.client.render.block.model.BlockModelDispatcher.loadDataModel;

@Environment(EnvType.CLIENT)
public class BlockModelGenericCompanionCube<T extends BlockLogic> extends BlockModelGeneric<T> {
	private final int[][] xTurns = new int[6][6];
	private final int[][] yTurns = new int[6][6];
	private final StaticBlockModel itemModel;

	public BlockModelGenericCompanionCube(@NotNull Block<T> block) {
		super(block, loadDataModel("btx:block/companion_cube/companion_cube"));
		this.itemModel = loadDataModel("btx:block/companion_cube/companion_cube_item").asModel();

		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				int facing = Direction.NORTH.rotateX(x).rotateY(y).id;
				int top = Direction.UP.rotateX(x).rotateY(y).id;

				if (facing == Direction.UP.id) {
					this.xTurns[facing][top] = x;
					this.yTurns[facing][top] = y;
				} else if (facing == Direction.DOWN.id) {
					int spin = Direction.fromId(top).opposite().id;
					this.xTurns[facing][spin] = x;
					this.yTurns[facing][spin] = y;
				} else if (x == 0) {
					for (int spin = 0; spin < 6; spin++) {
						this.xTurns[facing][spin] = 0;
						this.yTurns[facing][spin] = y;
					}
				}
			}
		}
	}

	@Override
	public boolean renderAttached(@NotNull TessellatorGeneral t, @NotNull WorldSource ws, @NotNull TilePosc pos,
								  boolean cullFaces, @Nullable IconCoordinate override) {
		int data = ws.getBlockData(pos);
		int facing = BlockLogicCompanionCube.getFacing(data).id;
		int spin = BlockLogicCompanionCube.getSpin(data).id;

		return getModelFromData(data).renderAttached(this, t, ws, pos,
			this.xTurns[facing][spin], this.yTurns[facing][spin], 0,
			0.0, 0.0, 0.0, false, cullFaces, override);
	}

	@Override
	public void renderStandalone(@NotNull TessellatorGeneral t, int metadata, byte lightIndex) {
		this.itemModel.renderStandalone(this, t, 0.0, 0.0, 0.0, metadata, lightIndex,
			BlockColorDispatcher.getInstance().getDispatch(this.block));
	}
}
