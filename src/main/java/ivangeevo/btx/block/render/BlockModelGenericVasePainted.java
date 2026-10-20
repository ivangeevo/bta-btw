package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.pottery.BlockLogicVasePainted;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.util.helper.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.useless.dragonfly.data.block.mojang.BlockModelMojangData;
import org.useless.dragonfly.models.block.StaticBlockModel;

public class BlockModelGenericVasePainted<T extends BlockLogic> extends BlockModelGeneric<T> {
	private static final String TEMPLATE = "btx:block/vase/vase_template";

	private final StaticBlockModel[] models = new StaticBlockModel[16];

	public BlockModelGenericVasePainted(@NotNull Block<T> block) {
		super(block, buildData(DyeColor.WHITE));
		for (DyeColor color : DyeColor.blockOrderedColors()) {
			this.models[color.blockMeta] = buildData(color).asModel();
		}
	}

	@NotNull
	@Override
	public StaticBlockModel getModelFromData(int data) {
		return this.models[data & BlockLogicVasePainted.MASK_COLOR];
	}

	private static BlockModelMojangData buildData(DyeColor color) {
		String name = color.colorID;
		ModelBuilder b = new ModelBuilder();
		b.setParent(TEMPLATE);
		b.setTexture("all", "btx:block/vase/vase_" + name);
		return b.create("btx:block/vase/vase_painted_" + name);
	}

	private static class ModelBuilder extends BlockModelMojangData.Builder {
		BlockModelMojangData create(String id) {
			return build(Minecraft.getMinecraft().texturePackList, id);
		}
	}
}
