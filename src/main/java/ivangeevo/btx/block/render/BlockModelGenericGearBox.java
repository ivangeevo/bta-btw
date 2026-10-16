package ivangeevo.btx.block.render;

import ivangeevo.btx.util.utils.FacingUtils;
import ivangeevo.btx.util.utils.MechPowerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicRotatable;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.useless.dragonfly.data.block.mojang.BlockModelMojangData;
import org.useless.dragonfly.models.block.StaticBlockModel;

@Environment(EnvType.CLIENT)
public class BlockModelGenericGearBox<T extends BlockLogic> extends BlockModelGeneric<T> {
	private static final String TEMPLATE = "btx:block/gearbox";
	private static final String SIDE_TEXTURE = "btx:block/gearbox/gearbox";
	private static final String INPUT_TEXTURE = "btx:block/gearbox/gearbox_input";
	private static final String OUTPUT_TEXTURE = "btx:block/gearbox/gearbox_output";
	private static final String[] FACE_NAMES = {"down", "up", "north", "south", "west", "east"};

	/** [inputFacing][mask], mask bit f = face f shows the output texture. */
	private final StaticBlockModel[][] models = new StaticBlockModel[6][64];

	public BlockModelGenericGearBox(@NotNull Block<T> block) {
		super(block, buildData(5, 1 << 4));

		for (int inputFacing = 0; inputFacing < 6; inputFacing++) {
			for (int mask = 0; mask < 64; mask++) {
				if ((mask & (1 << inputFacing)) == 0) {
					this.models[inputFacing][mask] = buildData(inputFacing, mask).asModel();
				}
			}
		}

	}

	@Override
	public @NotNull StaticBlockModel getModel(@NotNull WorldSource worldSource, @NotNull TilePosc tilePos) {
		int inputFacing = FacingUtils.facingFromDirection(
			BlockLogicRotatable.getDirectionFromMeta(worldSource.getBlockData(tilePos)));

		int mask = 0;

		for (int face = 0; face < 6; face++) {
			if (face != inputFacing && MechPowerUtils.doesBlockHaveFacingAxleToSide(worldSource, tilePos, face)) {
				mask |= 1 << face;
			}
		}

		if (mask == 0) {
			// no output axles connected: the face opposite the input shows the output texture as a hint
			mask = 1 << (inputFacing ^ 1);
		}

		return this.models[inputFacing][mask];
	}

	private static BlockModelMojangData buildData(int inputFacing, int mask) {
		ModelBuilder builder = new ModelBuilder();
		builder.setParent(TEMPLATE);

		for (int face = 0; face < 6; face++) {
			String texture;

			if (face == inputFacing) {
				texture = INPUT_TEXTURE;
			} else if ((mask & (1 << face)) != 0) {
				texture = OUTPUT_TEXTURE;
			} else {
				texture = SIDE_TEXTURE;
			}

			builder.setTexture(FACE_NAMES[face], texture);
		}

		// the id only has to be unique: models built this way never go through the file cache
		return builder.create("btx:block/gearbox/gearbox_" + FACE_NAMES[inputFacing] + "_" + mask);
	}

	/** Builder.build(...) is protected, so it needs a subclass to call it. */
	private static class ModelBuilder extends BlockModelMojangData.Builder {
		BlockModelMojangData create(String id) {
			return build(Minecraft.getMinecraft().texturePackList, id);
		}
	}

}
