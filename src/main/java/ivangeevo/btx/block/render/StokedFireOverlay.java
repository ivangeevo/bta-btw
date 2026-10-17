package ivangeevo.btx.block.render;

import ivangeevo.btx.block.BTXBlocks;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.enums.LightLayer;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;

/**
 * Draws the orange "top" flames that make stoked fire look two blocks tall.
 * BTW: the stoked-fire branch of BlockFire.renderBlock.
 */
public final class StokedFireOverlay {
	private static final double HEIGHT = 1.0; // change to 1.4 to match stock BTA flame height

	private static IconCoordinate top0;
	private static IconCoordinate top1;

	private StokedFireOverlay() {}

	/** Mixin entry: called after a normal fire block renders. Draws only if stoked fire is below. */
	public static void render(TessellatorGeneral t, WorldSource ws, TilePosc pos) {
		TilePos below = new TilePos(pos.x(), pos.y() - 1, pos.z());
		if (ws.getBlockType(below) != BTXBlocks.STOKED_FIRE) {
			return;
		}
		draw(t, ws, pos.x(), pos.y(), pos.z());
	}

	/** Draws the overlay planes in the block space at x, y, z. Called by the stoked fire model as well. */
	public static void draw(TessellatorGeneral t, WorldSource ws, int x, int y, int z) {
		if (top0 == null) { // lazy: textures may not be stitched yet when this class loads
			top0 = TextureRegistry.getTexture("btx:block/stoked_fire/stoked_fire_top_0");
			top1 = TextureRegistry.getTexture("btx:block/stoked_fire/stoked_fire_top_1");
		}

		// BTW: swap the two textures per column so neighbors don't look identical
		IconCoordinate tex1 = top0;
		IconCoordinate tex2 = top1;
		if (((x + z) & 1) != 0) {
			tex1 = top1;
			tex2 = top0;
		}

		t.setLightmapCoord2i(ws.getSavedLightValue(LightLayer.Block, new TilePos(x, y, z)), 15);
		double top = y + HEIGHT;

		// planes on the x and x + 1 faces (tex2)
		double minU = tex2.getIconUMin(), maxU = tex2.getIconUMax();
		double minV = tex2.getIconVMin(), maxV = tex2.getIconVMax();
		t.addVertexWithUV(x,     top, z,     minU, minV);
		t.addVertexWithUV(x,     y,   z,     minU, maxV);
		t.addVertexWithUV(x,     y,   z + 1, maxU, maxV);
		t.addVertexWithUV(x,     top, z + 1, maxU, minV);
		t.addVertexWithUV(x + 1, top, z + 1, minU, minV);
		t.addVertexWithUV(x + 1, y,   z + 1, minU, maxV);
		t.addVertexWithUV(x + 1, y,   z,     maxU, maxV);
		t.addVertexWithUV(x + 1, top, z,     maxU, minV);

		// planes on the z and z + 1 faces (tex1)
		minU = tex1.getIconUMin(); maxU = tex1.getIconUMax();
		minV = tex1.getIconVMin(); maxV = tex1.getIconVMax();
		t.addVertexWithUV(x,     top, z + 1, minU, minV);
		t.addVertexWithUV(x,     y,   z + 1, minU, maxV);
		t.addVertexWithUV(x + 1, y,   z + 1, maxU, maxV);
		t.addVertexWithUV(x + 1, top, z + 1, maxU, minV);
		t.addVertexWithUV(x + 1, top, z,     minU, minV);
		t.addVertexWithUV(x + 1, y,   z,     minU, maxV);
		t.addVertexWithUV(x,     y,   z,     maxU, maxV);
		t.addVertexWithUV(x,     top, z,     maxU, minV);
	}
}
