package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.mini_blocks.BlockLogicMouldingBase;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.block.Block;
import net.minecraft.core.util.helper.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.useless.dragonfly.models.block.StaticBlockModel;

public class BlockModelGenericMouldingPainted<T extends BlockLogicMouldingBase> extends BlockModelGenericMoulding<T> {

	private final StaticBlockModel[][] strips = new StaticBlockModel[16][3]; // [color][axis]
	private final StaticBlockModel[] cubes = new StaticBlockModel[16];

	public BlockModelGenericMouldingPainted(@NotNull Block<T> block) {
		super(block, texture(DyeColor.WHITE), name(DyeColor.WHITE));
		for (DyeColor c : DyeColor.blockOrderedColors()) {
			for (int axis = 0; axis < 3; axis++) {
				this.strips[c.blockMeta][axis] = buildStrip(axis, texture(c), name(c)).asModel();
			}
			this.cubes[c.blockMeta] = buildConnector(texture(c), name(c)).asModel();
		}
	}

	@NotNull
	@Override
	public StaticBlockModel getModelFromData(int data) {
		return this.strips[BlockLogicMouldingBase.colorIndex(data)][0];
	}

	@Override
	protected StaticBlockModel getStripModelFromData(int data, int axis) {
		return this.strips[BlockLogicMouldingBase.colorIndex(data)][axis];
	}

	@Override
	protected StaticBlockModel getConnectorModelFromData(int data) {
		return this.cubes[BlockLogicMouldingBase.colorIndex(data)];
	}

	// item meta already uses the shifted layout, same as the block data
	@Override
	public void renderStandalone(@NotNull TessellatorGeneral t, int metadata, byte lightIndex) {
		super.renderStandalone(t, metadata, lightIndex);
	}

	private static String texture(DyeColor c) {
		return "minecraft:block/planks/" + c.colorID;
	}

	private static String name(DyeColor c) {
		return "painted_" + c.colorID;
	}
}
