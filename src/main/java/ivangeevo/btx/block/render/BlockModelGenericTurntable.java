package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.turntable.BlockLogicTurntable;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.data.block.BlockModelData;
import org.useless.dragonfly.models.block.StaticBlockModel;

import static net.minecraft.client.render.block.model.BlockModelDispatcher.loadDataModel;

public class BlockModelGenericTurntable extends BlockModelGeneric<BlockLogicTurntable> {

	private final StaticBlockModel[] switches = new StaticBlockModel[4]; // [switch setting]

	private static final BlockModelData[] switchModelsData = new BlockModelData[] {
		loadDataModel("btx:block/turntable/turntable_switches_0"),
		loadDataModel("btx:block/turntable/turntable_switches_1"),
		loadDataModel("btx:block/turntable/turntable_switches_2"),
		loadDataModel("btx:block/turntable/turntable_switches_3")
	};

	public BlockModelGenericTurntable(@NotNull Block<BlockLogicTurntable> block) {
		super(block, loadDataModel("btx:block/turntable/turntable_base"));
		for (int i = 0; i < 4; i++) {
			this.switches[i] = switchModelsData[i].asModel();
		}
	}

	@Override
	public boolean renderAttached(
		@NotNull TessellatorGeneral tsltr, @NotNull WorldSource source, @NotNull TilePosc pos,
		boolean cullFaces, @Nullable IconCoordinate override
	) {
		BlockLogicTurntable logic = this.block.getLogic();
		int data = source.getBlockData(pos);

		boolean drew = getModelFromData(data).renderAttached(
			this, tsltr, source, pos,
			0, 0, 0,
			0.0, 0.0, 0.0,
			true, cullFaces, override
		);

		// the switch bars stick out of the block, so don't cull them
		int setting = logic.getSwitchSetting(source, pos);
		drew |= this.switches[setting].renderAttached(this, tsltr, source, pos,
			0, 0, 0, 0.0, 0.0, 0.0, false, false, override);

		return drew;
	}

	@Override
	public void renderStandalone(@NotNull TessellatorGeneral tessellator, int data, byte lightIndex) {
		var color = BlockColorDispatcher.getInstance().getDispatch(this.block);

		getModelFromData(0).renderStandalone(this, tessellator, 0.0, 0.0, 0.0, 0, lightIndex, color);

		// item stacks have no world data, so show the default switch setting
		this.switches[0].renderStandalone(this, tessellator, 0.0, 0.0, 0.0, 0, lightIndex, color);
	}

}
