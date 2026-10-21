package ivangeevo.btx.block.render;

import net.minecraft.client.render.block.model.BlockModelStandard;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.enums.LightLayer;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public class BlockModelStokedFire<T extends BlockLogic> extends BlockModelStandard<T> {
	private static final float HEIGHT = 1.0F;

	// The inner planes lean outwards: narrower at the bottom, wider at the top (offsets from the block center)
	private static final double BOTTOM_INSET = 0.2;
	private static final double TOP_INSET = 0.3;

	private IconCoordinate base0;
	private IconCoordinate base1;

	public BlockModelStokedFire(@NotNull Block<T> block) {
		super(block);
	}

	@Override
	public boolean render(@NotNull TessellatorGeneral t, @NotNull WorldSource ws, @NotNull TilePosc pos) {
		loadTextures();

		int x = pos.x(), y = pos.y(), z = pos.z();
		IconCoordinate[] columnTextures = pickColumnTextures(x, z);
		IconCoordinate tex1 = columnTextures[0];
		IconCoordinate tex2 = columnTextures[1];

		t.setLightmapCoord2i(ws.getSavedLightValue(LightLayer.Block, pos), 15);

		double top = y + HEIGHT;

		drawInnerPlanes(t, tex1, tex2, x, y, z, top);
		drawOuterPlanes(t, tex1, tex2, x, y, z, top);

		return true;
	}

	@Override
	public void renderStandalone(@NotNull TessellatorGeneral t, int metadata, byte lightIndex) {
		loadTextures();

		t.offsetTranslation(-0.5, -0.5, -0.5);
		t.startDrawingQuads();
		t.setLightmapCoord1i(lightIndex);
		t.setColor1i(0xFFFFFF);
		drawInnerPlanes(t, base0, base1, 0, 0, 0, HEIGHT);
		drawOuterPlanes(t, base0, base1, 0, 0, 0, HEIGHT);

		// the planes are single quads seen from both sides, like in the world
		GLRenderer.disableState(State.CULL_FACE);
		t.draw();
		GLRenderer.enableState(State.CULL_FACE);
		t.offsetTranslation(0.5, 0.5, 0.5);
	}

	private void loadTextures() {
		if (base0 == null) {
			base0 = TextureRegistry.getTexture("btx:block/stoked_fire/stoked_fire_base_0");
			base1 = TextureRegistry.getTexture("btx:block/stoked_fire/stoked_fire_base_1");
		}
	}

	/** Returns {tex1, tex2}. BTW swaps them per column so neighbors don't look identical. */
	private IconCoordinate[] pickColumnTextures(int x, int z) {
		// block-breaking overlay replaces both
		if (renderBlocks.overrideBlockTexture != null) {
			return new IconCoordinate[]{renderBlocks.overrideBlockTexture, renderBlocks.overrideBlockTexture};
		}
		return ((x + z) & 1) != 0
			? new IconCoordinate[]{base1, base0}
			: new IconCoordinate[]{base0, base1};
	}

	//------------- Geometry -------------//

	/** Four slanted planes: two along z (tex1), two along x (tex2). */
	private void drawInnerPlanes(TessellatorGeneral t, IconCoordinate tex1, IconCoordinate tex2,
								 int x, int y, int z, double top) {
		double cx = x + 0.5, cz = z + 0.5;

		double bottomLowX = cx - BOTTOM_INSET, bottomHighX = cx + BOTTOM_INSET;
		double topLowX = cx - TOP_INSET, topHighX = cx + TOP_INSET;
		double bottomLowZ = cz - BOTTOM_INSET, bottomHighZ = cz + BOTTOM_INSET;
		double topLowZ = cz - TOP_INSET, topHighZ = cz + TOP_INSET;

		// tex1: planes running along z, leaning in x
		plane(t, tex1, false, y, top,
			new Edge(topLowX, z + 1, bottomHighX, z + 1),
			new Edge(topLowX, z, bottomHighX, z));
		plane(t, tex1, false, y, top,
			new Edge(topHighX, z, bottomLowX, z),
			new Edge(topHighX, z + 1, bottomLowX, z + 1));

		// tex2: planes running along x, leaning in z
		plane(t, tex2, false, y, top,
			new Edge(x + 1, topHighZ, x + 1, bottomLowZ),
			new Edge(x, topHighZ, x, bottomLowZ));
		plane(t, tex2, false, y, top,
			new Edge(x, topLowZ, x, bottomHighZ),
			new Edge(x + 1, topLowZ, x + 1, bottomHighZ));
	}

	/** Four flat planes on the block faces. Mirrored UVs, and the texture order is the opposite of the inner planes (as in BTW). */
	private void drawOuterPlanes(TessellatorGeneral t, IconCoordinate tex1, IconCoordinate tex2,
								 int x, int y, int z, double top) {
		// west and east faces use tex2
		plane(t, tex2, true, y, top, Edge.vertical(x, z), Edge.vertical(x, z + 1));
		plane(t, tex2, true, y, top, Edge.vertical(x + 1, z + 1), Edge.vertical(x + 1, z));

		// south and north faces use tex1
		plane(t, tex1, true, y, top, Edge.vertical(x, z + 1), Edge.vertical(x + 1, z + 1));
		plane(t, tex1, true, y, top, Edge.vertical(x + 1, z), Edge.vertical(x, z));
	}

	/** One vertical edge of a plane: where it sits at the top and at the bottom. */
	private record Edge(double topX, double topZ, double bottomX, double bottomZ) {
		static Edge vertical(double x, double z) {
			return new Edge(x, z, x, z);
		}
	}

	/**
	 * Draws one quad between two edges. Vertex order is edge1 top, edge1 bottom, edge2 bottom, edge2 top.
	 * {@code mirrored} swaps which edge gets the min and max U.
	 */
	private void plane(TessellatorGeneral t, IconCoordinate icon, boolean mirrored,
					   double yBottom, double yTop, Edge edge1, Edge edge2
	) {
		double minU = icon.getIconUMin(), maxU = icon.getIconUMax();
		double minV = icon.getIconVMin(), maxV = icon.getIconVMax();

		double u1 = mirrored ? minU : maxU;
		double u2 = mirrored ? maxU : minU;

		t.addVertexWithUV(edge1.topX, yTop, edge1.topZ, u1, minV);
		t.addVertexWithUV(edge1.bottomX, yBottom, edge1.bottomZ, u1, maxV);
		t.addVertexWithUV(edge2.bottomX, yBottom, edge2.bottomZ, u2, maxV);
		t.addVertexWithUV(edge2.topX, yTop, edge2.topZ, u2, minV);
	}

	@Override
	public boolean shouldItemRender3d() {
		return false;
	}
}
