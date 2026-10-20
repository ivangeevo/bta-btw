package ivangeevo.btx.block.render;

import ivangeevo.btx.block.blocks.BlockLogicHopper;
import ivangeevo.btx.block.entity.TileEntityHopper;
import ivangeevo.btx.block.BTXBlocks;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.models.block.StaticBlockModel;

import static net.minecraft.client.render.block.model.BlockModelDispatcher.loadDataModel;

public class BlockModelGenericHopper<T extends BlockLogicHopper> extends BlockModelGeneric<T> {
	private static final double FILL_RANGE = 7.0 / 16.0;

	private final StaticBlockModel contents;
	private final Int2ObjectMap<StaticBlockModel> filterModels = new Int2ObjectOpenHashMap<>();

	public BlockModelGenericHopper(@NotNull Block<T> block) {
		super(block, loadDataModel("btx:block/hopper/hopper"));
		this.contents = loadDataModel("btx:block/hopper/hopper_contents").asModel();

		addFilter(BTXBlocks.WICKER_PANE.id(), "wicker");
		addFilter(BTXBlocks.GRATE.id(), "grate");
		addFilter(BTXBlocks.SLATS.id(), "slats");
		addFilter(Blocks.SOULSAND.id(), "soulsand");
		addFilter(Blocks.LADDER_OAK.id(), "ladder");
		addFilter(Blocks.MESH.id(), "mesh");
		addFilter(Blocks.TRAPDOOR_PLANKS_OAK.id(), "trapdoor_planks");
		addFilter(Blocks.TRAPDOOR_PLANKS_PAINTED.id(), "trapdoor_planks");
		addFilter(Blocks.TRAPDOOR_IRON.id(), "trapdoor_iron");
		addFilter(Blocks.TRAPDOOR_STEEL.id(), "trapdoor_steel");
		addFilter(Blocks.TRAPDOOR_GLASS.id(), "trapdoor_glass");
	}

	private void addFilter(int itemId, String name) {
		filterModels.put(itemId, loadDataModel("btx:block/hopper/filter_" + name).asModel());
	}

	@Override
	public boolean renderAttached(
		@NotNull TessellatorGeneral t, @NotNull WorldSource ws, @NotNull TilePosc pos,
		boolean cullFaces, @Nullable IconCoordinate override
	) {
		boolean drew = super.renderAttached(t, ws, pos, cullFaces, override);

		// override is set for e.g. the break overlay, which shouldn't paint the contents
		if (override != null || !(ws.getTileEntity(pos) instanceof TileEntityHopper hopper)) {
			return drew;
		}

		if (hopper.storageSlotsOccupied > 0) {
			double fillRatio = Math.min(1.0, (double) hopper.storageSlotsOccupied / TileEntityHopper.STORAGE_SLOTS);

			drew |= this.contents.renderAttached(this, t, ws, pos,
				0, 0, 0, 0.0, fillRatio * FILL_RANGE, 0.0, false, false, null);
		}

		StaticBlockModel filter = filterModels.get(hopper.filterItemId);

		if (filter != null) {
			drew |= filter.renderAttached(this, t, ws, pos,
				0, 0, 0, 0.0, 0.0, 0.0, false, false, null);
		}

		return drew;
	}

	@Override
	public void renderStandalone(@NotNull TessellatorGeneral t, int data, byte lightIndex) {
		getModelFromData(0).renderStandalone(this, t, 0.0, 0.0, 0.0, 0, lightIndex,
			BlockColorDispatcher.getInstance().getDispatch(this.block));
	}
}
