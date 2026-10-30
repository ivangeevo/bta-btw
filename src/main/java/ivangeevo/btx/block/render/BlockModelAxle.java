package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.BlockLogicAxle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.useless.dragonfly.models.block.StaticBlockModel;

@Environment(EnvType.CLIENT)
public class BlockModelAxle<T extends BlockLogic> extends BlockModelGeneric<T> {
	private static final String[] AXES = {"y", "z", "x"};

	private final StaticBlockModel[] off = new StaticBlockModel[3];
	private final StaticBlockModel[] on = new StaticBlockModel[3];

	public BlockModelAxle(@NotNull Block<T> block) {
		this(block, false);
	}

	public BlockModelAxle(@NotNull Block<T> block, boolean painted) {
		super(block, BlockModelDispatcher.loadDataModel(modelPath(painted, "z", "off")));

		for (int i = 0; i < 3; i++) {
			this.off[i] = BlockModelDispatcher.loadDataModel(modelPath(painted, AXES[i], "off")).asModel();
			this.on[i] = BlockModelDispatcher.loadDataModel(modelPath(painted, AXES[i], "on")).asModel();
		}
	}

	private static String modelPath(boolean painted, String axis, String state) {
		return "btx:block/axle/axle_" + (painted ? "painted_" : "") + axis + "_" + state;
	}

	@Override
	public @NotNull StaticBlockModel getModel(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		int axis = Math.min((source.getBlockData(tilePos) & BlockLogicAxle.MASK_AXIS) >> 2, 2);
		boolean powered = this.block.getLogic() instanceof BlockLogicAxle axle && axle.clientCheckIfPowered(source, tilePos);
		return powered ? this.on[axis] : this.off[axis];
	}

	@Override
	public @NotNull StaticBlockModel getModelFromData(int data) {
		return this.off[2];
	}
}
