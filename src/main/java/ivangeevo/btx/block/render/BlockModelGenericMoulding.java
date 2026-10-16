package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.mini_blocks.BlockLogicMouldingBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBdc;
import org.useless.dragonfly.data.block.BlockModelData;
import org.useless.dragonfly.data.block.mojang.BlockModelMojangData;
import org.useless.dragonfly.models.block.StaticBlockModel;

public class BlockModelGenericMoulding<T extends BlockLogicMouldingBase> extends BlockModelGeneric<T> {

	/*
	 * Each of the 3 strip models sits in one corner of the block and runs along one axis:
	 *   moulding_x -> alignment 2  (y low,  z high)
	 *   moulding_y -> alignment 4  (x low,  z low)
	 *   moulding_z -> alignment 3  (x low,  y low)
	 * Every other alignment is that model turned in 90 degree steps around ITS OWN axis only,
	 * so only one of rx/ry/rz is ever non-zero and the rotation order cannot matter.
	 *
	 * Alignment:  0  1  2  3 | 4  5  6  7 | 8  9  10 11
	 */
	/** Axis the strip runs along: 0 = X, 1 = Y, 2 = Z. */
	private static final int[] AXIS  = {0, 2, 0, 2,  1, 1, 1, 1,  0, 2, 0, 2};
	/** Quarter turns around that axis, counted from the base pose of that axis' model. */
	private static final int[] TURNS = {1, 1, 0, 0,  0, 3, 2, 1,  2, 2, 3, 3};

	private final StaticBlockModel stripY;
	private final StaticBlockModel stripZ;
	private final StaticBlockModel connectorModel;

	public BlockModelGenericMoulding(
		@NotNull Block<T> block,
		@NotNull BlockModelData stripX, @NotNull BlockModelData stripY, @NotNull BlockModelData stripZ,
		@NotNull BlockModelData connector
	)
	{
		super(block, stripX);
		this.stripY = stripY.asModel();
		this.stripZ = stripZ.asModel();
		this.connectorModel = connector.asModel();
	}

	/**
	 * Builds all four models in code from the templates.
	 * @param texture e.g. "minecraft:block/planks/oak"
	 * @param name unique model id part, e.g. "oak" (must differ per material, it keys the model cache)
	 */
	public BlockModelGenericMoulding(@NotNull Block<T> block, @NotNull String texture, @NotNull String name) {
		this(block,
			buildStrip(0, texture, name),
			buildStrip(1, texture, name),
			buildStrip(2, texture, name),
			buildConnector(texture, name));
	}

	//------------- Code-built models -------------//

	private static final String[] STRIP_TEMPLATE = {
		"btx:block/moulding/moulding_template_x",
		"btx:block/moulding/moulding_template_y",
		"btx:block/moulding/moulding_template_z"
	};
	private static final String[] AXIS_NAME = {"x", "y", "z"};
	private static final String CONNECTOR_TEMPLATE = "btx:block/moulding/connector/moulding_connector_template";

	protected static BlockModelMojangData buildStrip(int axis, String texture, String name) {
		// the axis is part of the id so the three models of one material never share a cache entry
		return build(STRIP_TEMPLATE[axis], name + "_" + AXIS_NAME[axis], texture);
	}

	protected static BlockModelMojangData buildConnector(String texture, String name) {
		return build(CONNECTOR_TEMPLATE, name + "_connector", texture);
	}

	private static BlockModelMojangData build(String template, String id, String texture) {
		ModelBuilder b = new ModelBuilder();
		b.setParent(template);
		b.setTexture("top", texture);
		b.setTexture("side", texture);
		b.setTexture("bottom", texture);
		return b.create("btx:block/moulding/" + id);
	}

	private static class ModelBuilder extends BlockModelMojangData.Builder {
		BlockModelMojangData create(String id) {
			return build(Minecraft.getMinecraft().texturePackList, id);
		}
	}

	/**
	 * Strip model for the given axis. The X model goes through getModelFromData so existing
	 * color handling keeps working; the painted subclass must override this to also return
	 * colored Y and Z models.
	 */
	protected StaticBlockModel getStripModelFromData(int data, int axis) {
		return switch (axis) {
			case 1 -> this.stripY;
			case 2 -> this.stripZ;
			default -> getModelFromData(data);
		};
	}

	/** Overridden by the painted model to pick the color. */
	protected StaticBlockModel getConnectorModelFromData(int data) {
		return this.connectorModel;
	}

	@Override
	public boolean renderAttached(@NotNull TessellatorGeneral t, @NotNull WorldSource ws, @NotNull TilePosc pos,
								  boolean cullFaces, @Nullable IconCoordinate override
	) {
		BlockLogicMouldingBase logic = this.block.getLogic();
		int data = ws.getBlockData(pos);

		int alignment = logic.getMouldingAlignmentFromBlockData(data);
		int axis = AXIS[alignment];
		int turns = TURNS[alignment];

		int rx = axis == 0 ? turns : 0;
		int ry = axis == 1 ? turns : 0;
		int rz = axis == 2 ? turns : 0;

		boolean drew = getStripModelFromData(data, axis).renderAttached(this, t, ws, pos,
			rx, ry, rz, 0.0, 0.0, 0.0, true, cullFaces, override
		);

		StaticBlockModel connector = getConnectorModelFromData(data);
		for (int a = 0; a < 3; a++) {
			AABBdc c = logic.getConnectionBoundsAlongAxis(ws, pos, a);
			if (c != null) {
				// every connection box is a single 8x8x8 cube, placed at its min corner
				drew |= connector.renderAttached(this, t, ws, pos, 0, 0, 0,
					c.minX(), c.minY(), c.minZ(), false, cullFaces, override);
			}
		}
		return drew;
	}

	/** Inventory icon: the strip in its base pose. */
	@Override
	public void renderStandalone(@NotNull TessellatorGeneral t, int metadata, byte lightIndex) {
		getModelFromData(metadata).renderStandalone(this, t, 0.0, 0.0, 0.0, metadata, lightIndex,
			BlockColorDispatcher.getInstance().getDispatch(this.block));
	}
}
