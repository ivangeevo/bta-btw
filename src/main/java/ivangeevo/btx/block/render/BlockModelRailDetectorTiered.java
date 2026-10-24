package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.BlockLogicRailDetectorTiered;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.BlockModelRail;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.Block;
import net.minecraft.core.util.helper.Side;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BlockModelRailDetectorTiered<T extends BlockLogicRailDetectorTiered> extends BlockModelRail<T> {
	private final IconCoordinate straight;
	private final IconCoordinate active;

	public BlockModelRailDetectorTiered(@NotNull Block<T> block, @NotNull String textureFolder) {
		super(block);
		this.straight = TextureRegistry.getTexture(textureFolder + "/straight");
		this.active = TextureRegistry.getTexture(textureFolder + "/active");
		this.setAllTextures(this.straight);
	}

	@Override
	public @Nullable IconCoordinate getBlockTextureFromSideAndMetadata(@NotNull Side side, int data) {
		return (data & BlockLogicRailDetectorTiered.MASK_POWERED) != 0 ? this.active : this.straight;
	}
}