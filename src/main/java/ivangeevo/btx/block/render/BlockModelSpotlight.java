package ivangeevo.btx.block.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.models.block.StaticBlockModel;

import static net.minecraft.client.render.block.model.BlockModelDispatcher.loadDataModel;

@Environment(EnvType.CLIENT)
public class BlockModelSpotlight<T extends BlockLogic> extends BlockModelGeneric<T> {
	private final StaticBlockModel[] faceModels = new StaticBlockModel[Direction.all.length];
	private final TilePos queryPos = new TilePos();

	public BlockModelSpotlight(@NotNull Block<T> block) {
		super(block, loadDataModel("btx:block/lens/spotlight_down"));

		for (Direction dir : Direction.all) {
			this.faceModels[dir.id] = loadDataModel("btx:block/lens/spotlight_" + dir.name().toLowerCase()).asModel();
		}
	}

	@Override
	public boolean renderAttached(@NotNull TessellatorGeneral t, @NotNull WorldSource ws, @NotNull TilePosc pos,
								  boolean cullFaces, @Nullable IconCoordinate override) {
		boolean rendered = false;

		for (Direction dir : Direction.all) {
			if (ws.isBlockNormalCube(pos.add(dir, this.queryPos))) {
				rendered |= this.faceModels[dir.id].renderAttached(this, t, ws, pos, 0, 0, 0, 0.0, 0.0, 0.0, false, false, override);
			}
		}

		return rendered;
	}

	@Override
	public void renderStandalone(@NotNull TessellatorGeneral t, int metadata, byte lightIndex) {}
}
