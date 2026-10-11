package ivangeevo.btx.block.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import org.jetbrains.annotations.NotNull;
import org.useless.dragonfly.models.block.StaticBlockModel;

@Environment(EnvType.CLIENT)
public class BlockModelGenericHandCrank<T extends BlockLogic> extends BlockModelGeneric<T> {
	private static final String OFF_MODEL = "btx:block/hand_crank/hand_crank_off";
	private static final String ON_MODEL = "btx:block/hand_crank/hand_crank_on";

    private final StaticBlockModel on;

    public BlockModelGenericHandCrank(@NotNull Block<T> block) {
        super(block, BlockModelDispatcher.loadDataModel(OFF_MODEL));
        this.on = BlockModelDispatcher.loadDataModel(ON_MODEL).asModel();
	}

	@Override
    public @NotNull StaticBlockModel getModelFromData(int data) {
        return data > 0 ? this.on : this.staticModel;
    }

	@Override
	public void renderStandalone(@NotNull TessellatorGeneral tessellator, int metadata, byte lightIndex) {
		this.on.renderStandalone(this, tessellator, 0.0, 0.0, 0.0, metadata, lightIndex,
			BlockColorDispatcher.getInstance().getDispatch(this.block)
		);
	}
}
