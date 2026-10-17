package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.kiln.BlockLogicKiln;
import ivangeevo.btx.recipe.BTXRecipes;
import net.minecraft.client.render.block.model.BlockModel;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.Nullable;

public final class KilnCookOverlay {
	private static IconCoordinate[] icons;

	private KilnCookOverlay() {}

	// Looked up lazily because the texture registry isn't filled when classes load
	public static @Nullable IconCoordinate cookIcon(int stage) {
		if (icons == null) {
			icons = new IconCoordinate[7];
			for (int i = 0; i < 7; i++) {
				icons[i] = TextureRegistry.getTexture("btx:block/kiln/kiln_cooking_overlay_" + (i + 1));
			}
		}
		return icons[stage];
	}

	/** Call after a block's normal render. Returns true if an overlay was drawn. */
	public static boolean render(
		BlockModel<?> model, TessellatorGeneral t, WorldSource ws, TilePosc pos,
		boolean firstPassDrew, @Nullable IconCoordinate override
	) {
		if (!firstPassDrew || override != null) return false; // CE: bFirstPassResult + recursion guard

		if (!BTXRecipes.KILN.canCook(ws.getBlockType(pos))) return false;

		TilePos below = new TilePos(pos.x(), pos.y() - 1, pos.z());

		if (!(ws.getBlockType(below).getLogic() instanceof BlockLogicKiln kiln)) return false;

		int stage = kiln.getCookStage(ws, below);
		IconCoordinate icon = stage < 0 ? null : cookIcon(stage);

		if (icon == null) return false;

		return model.renderWithOverrideTexture(t, ws, pos, icon);
	}
}
