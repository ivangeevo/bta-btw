package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.BlockLogicLens;
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
public class BlockModelLens<T extends BlockLogicLens> extends BlockModelGenericVeryRotatable<T> {
	private final StaticBlockModel unlitModel;
	private final StaticBlockModel litModel;

	public BlockModelLens(@NotNull Block<T> block) {
		super(block, loadDataModel("btx:block/lens/lens"));
		this.unlitModel = loadDataModel("btx:block/lens/lens").asModel();
		this.litModel = loadDataModel("btx:block/lens/lens_lit").asModel();
	}

	@Override
	public @NotNull StaticBlockModel getModel(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return getModelFromData(source.getBlockData(tilePos));
	}

	@Override
	public @NotNull StaticBlockModel getModelFromData(int data) {
		return BlockLogicLens.isLit(data) ? this.litModel : this.unlitModel;
	}

	@Override
	public IconCoordinate getParticleTexture(@NotNull Side side, int meta) {
		return this.unlitModel.getParticle(side);
	}
}
