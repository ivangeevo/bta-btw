package ivangeevo.btx.block.render;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicAnchor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.models.block.StaticBlockModel;

import static net.minecraft.client.render.block.model.BlockModelDispatcher.loadDataModel;

@Environment(EnvType.CLIENT)
public class BlockModelAnchor<T extends BlockLogicAnchor> extends BlockModelGeneric<T> {
	private static final int[][] ROTATIONS = {
		{2, 0, 0},
		{0, 0, 0},
		{3, 0, 0},
		{1, 0, 0},
		{0, 1, 1},
		{0, 1, 3}
	};

	private final StaticBlockModel anchorModel;
	private final StaticBlockModel ropeDownModel;
	private final StaticBlockModel ropeUpModel;

	public BlockModelAnchor(@NotNull Block<T> block) {
		super(block, loadDataModel("btx:block/anchor/anchor"));
		this.anchorModel = loadDataModel("btx:block/anchor/anchor").asModel();
		this.ropeDownModel = loadDataModel("btx:block/anchor/anchor_rope_down").asModel();
		this.ropeUpModel = loadDataModel("btx:block/anchor/anchor_rope_up").asModel();
	}

	@Override
	public boolean renderAttached(@NotNull TessellatorGeneral t, @NotNull WorldSource ws, @NotNull TilePosc pos,
								  boolean cullFaces, @Nullable IconCoordinate override) {
		int facing = BlockLogicAnchor.getFacing(ws.getBlockData(pos)) % ROTATIONS.length;
		int[] rotation = ROTATIONS[facing];
		boolean rendered = this.anchorModel.renderAttached(this, t, ws, pos, rotation[0], rotation[1], rotation[2], 0.0, 0.0, 0.0, false, cullFaces, override);

		if (facing == 1) {
			Block<?> above = ws.getBlockType(pos.up(new TilePos()));

			if (above == Blocks.ROPE || above == BTXBlocks.PULLEY) {
				rendered |= this.ropeUpModel.renderAttached(this, t, ws, pos, 0, 0, 0, 0.0, 0.0, 0.0, false, cullFaces, override);
			}
		} else if (BlockLogicAnchor.isAnchoredRope(ws, pos.down(new TilePos()))) {
			rendered |= this.ropeDownModel.renderAttached(this, t, ws, pos, 0, 0, 0, 0.0, 0.0, 0.0, false, cullFaces, override);
		}

		return rendered;
	}

	@Override
	public void renderStandalone(@NotNull TessellatorGeneral t, int metadata, byte lightIndex) {
		this.anchorModel.renderStandalone(this, t, 0.0, 0.0, 0.0, metadata, lightIndex,
			BlockColorDispatcher.getInstance().getDispatch(this.block));
	}
}
