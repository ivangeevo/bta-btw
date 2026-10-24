package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.BlockLogicBuddy;
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
public class BlockModelBuddy<T extends BlockLogicBuddy> extends BlockModelGenericVeryRotatable<T> {
	private final StaticBlockModel offModel;
	private final StaticBlockModel onModel;

	public BlockModelBuddy(@NotNull Block<T> block) {
		super(block, loadDataModel("btx:block/buddy_block/buddy_block"));
		this.offModel = loadDataModel("btx:block/buddy_block/buddy_block").asModel();
		this.onModel = loadDataModel("btx:block/buddy_block/buddy_block_on").asModel();
	}

	@Override
	public @NotNull StaticBlockModel getModel(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return getModelFromData(source.getBlockData(tilePos));
	}

	@Override
	public @NotNull StaticBlockModel getModelFromData(int data) {
		return BlockLogicBuddy.isOn(data) ? this.onModel : this.offModel;
	}

	@Override
	public IconCoordinate getParticleTexture(@NotNull Side side, int meta) {
		return this.offModel.getParticle(side);
	}
}
