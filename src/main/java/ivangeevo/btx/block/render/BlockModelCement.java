package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.BlockLogicCement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.BlockModelStandard;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBdc;

@Environment(EnvType.CLIENT)
public class BlockModelCement<T extends BlockLogicCement> extends BlockModelStandard<T> {
	private final IconCoordinate wet;
	private final IconCoordinate drying;
	private final TilePos queryPos = new TilePos();

	public BlockModelCement(@NotNull Block<T> block) {
		super(block);
		this.wet = TextureRegistry.getTexture("btx:block/cement/wet");
		this.drying = TextureRegistry.getTexture("btx:block/cement/drying");
		this.setAllTextures(this.wet);
	}

	@Override
	public boolean render(@NotNull TessellatorGeneral t, @NotNull WorldSource ws, @NotNull TilePosc pos) {
		int x = pos.x(), y = pos.y(), z = pos.z();
		AABBdc bounds = this.block.getBoundsFromState(ws, pos);
		boolean renderTop = shouldSideBeRendered(ws, bounds, pos.up(this.queryPos), Side.TOP);
		boolean renderBottom = shouldSideBeRendered(ws, bounds, pos.down(this.queryPos), Side.BOTTOM);
		boolean[] renderSides = {
			shouldSideBeRendered(ws, bounds, pos.north(this.queryPos), Side.NORTH),
			shouldSideBeRendered(ws, bounds, pos.south(this.queryPos), Side.SOUTH),
			shouldSideBeRendered(ws, bounds, pos.west(this.queryPos), Side.WEST),
			shouldSideBeRendered(ws, bounds, pos.east(this.queryPos), Side.EAST)
		};

		if (!renderTop && !renderBottom && !renderSides[0] && !renderSides[1] && !renderSides[2] && !renderSides[3]) {
			return false;
		}

		IconCoordinate tex = renderBlocks.overrideBlockTexture != null
			? renderBlocks.overrideBlockTexture
			: BlockLogicCement.isPartiallyDry(ws, pos) ? this.drying : this.wet;

		t.setColor1i(0xFFFFFF);

		float hNW = getCornerHeight(ws, x, y, z);
		float hSW = getCornerHeight(ws, x, y, z + 1);
		float hSE = getCornerHeight(ws, x + 1, y, z + 1);
		float hNE = getCornerHeight(ws, x + 1, y, z);
		boolean rendered = false;

		if (renderTop) {
			t.setLightmapCoord1i(this.block.getLightIndex(ws, pos.up(this.queryPos)));
			t.setNormal(0.0F, 1.0F, 0.0F);
			t.addVertexWithUV(x, y + hNW, z, tex.getIconUMin(), tex.getIconVMin());
			t.addVertexWithUV(x, y + hSW, z + 1, tex.getIconUMin(), tex.getIconVMax());
			t.addVertexWithUV(x + 1, y + hSE, z + 1, tex.getIconUMax(), tex.getIconVMax());
			t.addVertexWithUV(x + 1, y + hNE, z, tex.getIconUMax(), tex.getIconVMin());
			rendered = true;
		}

		if (renderBottom) {
			t.setLightmapCoord1i(this.block.getLightIndex(ws, pos.down(this.queryPos)));
			t.setNormal(0.0F, -1.0F, 0.0F);
			renderBlocks.renderBottomFace(t, bounds, x, y, z, tex);
			rendered = true;
		}

		for (int side = 0; side < 4; side++) {
			if (!renderSides[side]) {
				continue;
			}

			double h1, h2, x1, x2, z1, z2;
			int nx = x, nz = z;

			switch (side) {
				case 0 -> {
					h1 = hNW; h2 = hNE; x1 = x; x2 = x + 1; z1 = z; z2 = z;
					nz--;
					t.setNormal(0.0F, 0.0F, -1.0F);
				}
				case 1 -> {
					h1 = hSE; h2 = hSW; x1 = x + 1; x2 = x; z1 = z + 1; z2 = z + 1;
					nz++;
					t.setNormal(0.0F, 0.0F, 1.0F);
				}
				case 2 -> {
					h1 = hSW; h2 = hNW; x1 = x; x2 = x; z1 = z + 1; z2 = z;
					nx--;
					t.setNormal(-1.0F, 0.0F, 0.0F);
				}
				default -> {
					h1 = hNE; h2 = hSE; x1 = x + 1; x2 = x + 1; z1 = z; z2 = z + 1;
					nx++;
					t.setNormal(1.0F, 0.0F, 0.0F);
				}
			}

			this.queryPos.set(nx, y, nz);
			t.setLightmapCoord1i(this.block.getLightIndex(ws, this.queryPos));

			double u1 = tex.getIconUMin();
			double u2 = tex.getIconUMax();
			double v1 = tex.getSubIconV(1.0 - h1);
			double v2 = tex.getSubIconV(1.0 - h2);
			double vBottom = tex.getIconVMax();

			t.addVertexWithUV(x1, y + h1, z1, u1, v1);
			t.addVertexWithUV(x2, y + h2, z2, u2, v2);
			t.addVertexWithUV(x2, y, z2, u2, vBottom);
			t.addVertexWithUV(x1, y, z1, u1, vBottom);
			rendered = true;
		}

		t.setNormal(0.0F, 1.0F, 0.0F);

		return rendered;
	}

	private float getCornerHeight(WorldSource ws, int x, int y, int z) {
		int factors = 0;
		float depth = 0.0F;
		TilePos samplePos = new TilePos();
		TilePos abovePos = new TilePos();

		for (int i = 0; i < 4; i++) {
			samplePos.set(x - (i & 1), y, z - ((i >> 1) & 1));
			abovePos.set(samplePos.x, y + 1, samplePos.z);

			if (ws.getBlockMaterial(abovePos) == BlockLogicCement.CEMENT_MATERIAL) {
				return 1.0F;
			}

			Material material = ws.getBlockMaterial(samplePos);

			if (material == BlockLogicCement.CEMENT_MATERIAL) {
				if (ws.isBlockNormalCube(abovePos)) {
					return 1.0F;
				}

				float height = BlockLogicCement.getRenderHeight(ws, samplePos);

				if (BlockLogicCement.isSourceBlock(ws, samplePos)) {
					depth += height * 10.0F;
					factors += 10;
				}

				depth += height;
				factors++;
			} else if (!material.isSolid()) {
				depth += 0.6F;
				factors++;
			}
		}

		return factors > 0 ? 1.0F - depth / factors : 1.0F;
	}

	@Override
	public boolean shouldSideBeRendered(@NotNull WorldSource source, @NotNull AABBdc bounds, @NotNull TilePosc tilePos, @NotNull Side side) {
		Material material = source.getBlockMaterial(tilePos);

		if (material == BlockLogicCement.CEMENT_MATERIAL || material == Materials.ICE) {
			return false;
		}

		return side == Side.TOP || super.shouldSideBeRendered(source, bounds, tilePos, side);
	}

	@Override
	public boolean shouldItemRender3d() {
		return false;
	}
}
