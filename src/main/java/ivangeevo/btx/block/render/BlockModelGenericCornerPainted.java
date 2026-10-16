package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.mini_blocks.BlockLogicCornerPainted;
import ivangeevo.btx.block.blocks.mini_blocks.BlockLogicSidingPainted;
import net.minecraft.client.Minecraft;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.util.helper.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.useless.dragonfly.data.block.mojang.BlockModelMojangData;
import org.useless.dragonfly.models.block.StaticBlockModel;

public class BlockModelGenericCornerPainted<T extends BlockLogic> extends BlockModelGenericCorner<T> {
	private static final String TEMPLATE = "btx:block/corner/corner_template";

	private final StaticBlockModel[] models = new StaticBlockModel[16];

	public BlockModelGenericCornerPainted(@NotNull Block<T> block) {
		super(block, buildData(DyeColor.WHITE));
		for (DyeColor color : DyeColor.blockOrderedColors()) {
			this.models[color.blockMeta] = buildData(color).asModel();
		}
	}

	@NotNull
	@Override
	public StaticBlockModel getModelFromData(int data) {
		return this.models[BlockLogicCornerPainted.colorIndex(data)];
	}

	private static BlockModelMojangData buildData(DyeColor c) {
		String tex = "minecraft:block/planks/" + c.colorID;
		ModelBuilder builder = new ModelBuilder();
		builder.setParent(TEMPLATE);
		builder.setTexture("top", tex);
		builder.setTexture("side", tex);
		builder.setTexture("bottom", tex);
		return builder.create("btx:block/corner/corner_" + c.colorID);
	}

	private static class ModelBuilder extends BlockModelMojangData.Builder {
		BlockModelMojangData create(String id) {
			return build(Minecraft.getMinecraft().texturePackList, id);
		}
	}
}
