package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.mini_blocks.BlockLogicSidingPainted;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.util.helper.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.useless.dragonfly.data.block.mojang.BlockModelMojangData;
import org.useless.dragonfly.models.block.StaticBlockModel;

public class BlockModelGenericSidingPainted<T extends BlockLogic> extends BlockModelGenericSiding<T> {
	private static final String TEMPLATE = "btx:block/siding/siding_template";

	private final StaticBlockModel[] models = new StaticBlockModel[16];

	public BlockModelGenericSidingPainted(@NotNull Block<T> block) {
		super(block, buildData(DyeColor.WHITE));
		for (DyeColor color : DyeColor.blockOrderedColors()) {
			this.models[color.blockMeta] = buildData(color).asModel();
		}
	}

	@NotNull
	@Override
	public StaticBlockModel getModelFromData(int data) {
		return this.models[BlockLogicSidingPainted.colorIndex(data)];
	}

	private static BlockModelMojangData buildData(DyeColor c) {
		String tex = "minecraft:block/planks/" + c.colorID;
		ModelBuilder b = new ModelBuilder();
		b.setParent(TEMPLATE);
		b.setTexture("top", tex);
		b.setTexture("side", tex);
		b.setTexture("bottom", tex);
		return b.create("btx:block/siding/siding_" + c.colorID);
	}

	private static class ModelBuilder extends BlockModelMojangData.Builder {
		BlockModelMojangData create(String id) {
			return build(Minecraft.getMinecraft().texturePackList, id);
		}
	}
}
