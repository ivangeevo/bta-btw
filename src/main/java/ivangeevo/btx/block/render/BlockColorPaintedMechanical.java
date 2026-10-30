package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.PaintedMechanicalDevice;
import ivangeevo.btx.util.PaintTints;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.color.BlockColor;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class BlockColorPaintedMechanical extends BlockColor {
	@Override
	public int getFallbackColor(int meta, int tintIndex) {
		return 0xFF000000 | PaintTints.getRGB(PaintedMechanicalDevice.colorIndex(meta));
	}

	@Override
	public int getWorldColor(@NotNull WorldSource source, @NotNull TilePosc tilePos, int tintIndex) {
		return 0xFF000000 | PaintTints.getRGB(PaintedMechanicalDevice.colorIndex(source.getBlockData(tilePos)));
	}
}