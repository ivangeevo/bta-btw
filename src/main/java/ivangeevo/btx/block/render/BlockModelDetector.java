package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.BlockLogicDetector;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.generic.BlockModelGenericVeryRotatable;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.useless.dragonfly.models.block.StaticBlockModel;

import static net.minecraft.client.render.block.model.BlockModelDispatcher.loadDataModel;

@Environment(EnvType.CLIENT)
public class BlockModelDetector<T extends BlockLogicDetector> extends BlockModelGenericVeryRotatable<T> {
	private final StaticBlockModel offModel;
	private final StaticBlockModel onModel;

	public BlockModelDetector(@NotNull Block<T> block) {
		super(block, loadDataModel("btx:block/detector_block/detector_block"));
		this.offModel = loadDataModel("btx:block/detector_block/detector_block").asModel();
		this.onModel = loadDataModel("btx:block/detector_block/detector_block_on").asModel();
	}

	@Override
	public @NotNull StaticBlockModel getModel(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return getModelFromData(source.getBlockData(tilePos));
	}

	@Override
	public @NotNull StaticBlockModel getModelFromData(int data) {
		return BlockLogicDetector.isOn(data) ? this.onModel : this.offModel;
	}

	@Override
	public IconCoordinate getParticleTexture(@NotNull Side side, int meta) {
		return this.offModel.getParticle(side);
	}
}
