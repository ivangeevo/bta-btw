package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.BlockLogicCauldron;
import ivangeevo.btx.block.entity.TileEntityCookingVessel;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.models.block.StaticBlockModel;

import static net.minecraft.client.render.block.model.BlockModelDispatcher.loadDataModel;

public class BlockModelGenericCauldron<T extends BlockLogicCauldron> extends BlockModelGeneric<T> {
	// same turns as BlockModelGenericCrucible
	private static final int[] TILT_TURNS_X = {0, 0, 3, 1, 0, 0};
	private static final int[] TILT_TURNS_Z = {0, 0, 0, 0, 1, 3};

	private static final int VESSEL_SLOT_COUNT = 27;
	private static final double FILL_RANGE = 4.0 / 16.0;

	private final StaticBlockModel contents;

	public BlockModelGenericCauldron(@NotNull Block<T> block) {
		super(block, loadDataModel("btx:block/cauldron/cauldron"));
		this.contents = loadDataModel("btx:block/cauldron/cauldron_contents").asModel();
	}

	@Override
	public boolean renderAttached(
		@NotNull TessellatorGeneral t, @NotNull WorldSource ws, @NotNull TilePosc pos,
		boolean cullFaces, @Nullable IconCoordinate override
	) {
		BlockLogicCauldron logic = this.block.getLogic();
		int data = ws.getBlockData(pos);
		boolean tilted = logic.getMechanicallyPoweredFlag(ws, pos);
		int facing = tilted ? logic.getTiltFacing(ws, pos) : 1;

		boolean drew = getModelFromData(data).renderAttached(this, t, ws, pos,
			TILT_TURNS_X[facing], 0, TILT_TURNS_Z[facing], 0.0, 0.0, 0.0, true, false, override);

		// override is set for e.g. the break overlay, which shouldn't paint the contents
		if (override != null || tilted) return drew;

		double fillRatio = 0.0;
		if (ws.getTileEntity(pos) instanceof TileEntityCookingVessel vessel) {
			fillRatio = Math.min(1.0, (double) vessel.storageSlotsOccupied / VESSEL_SLOT_COUNT);
		}

		drew |= this.contents.renderAttached(this, t, ws, pos,
			0, 0, 0, 0.0, fillRatio * FILL_RANGE, 0.0, false, false, null);

		return drew;
	}

	@Override
	public void renderStandalone(@NotNull TessellatorGeneral t, int data, byte lightIndex) {
		getModelFromData(0).renderStandalone(this, t, 0.0, 0.0, 0.0, 0, lightIndex,
			BlockColorDispatcher.getInstance().getDispatch(this.block));
		this.contents.renderStandalone(this, t, 0.0, 0.0, 0.0, 0, lightIndex,
			BlockColorDispatcher.getInstance().getDispatch(this.block));
	}
}
