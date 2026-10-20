package ivangeevo.btx.block.blocks.pottery;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

public class BlockLogicPlanter extends BlockLogic {
	private static final double BAND_HEIGHT = 5.0 / 16.0;

	protected BlockLogicPlanter(@NotNull Block<?> block) {
		super(block, Materials.GLASS);
	}

	public static BlockLogicSupplier<BlockLogicPlanter> properties() {
		return block -> new BlockLogicPlanter(applyCommon(block));
	}

	protected static Block<?> applyCommon(Block<?> block) {
		return block
			.withSound(BlockSounds.GLASS)
			.withHardness(0.6F)
			.withTags(BlockTags.MINEABLE_BY_PICKAXE);
	}

	@Override public boolean isSolidRender() {
		return false;
	}

	@Override public boolean isCubeShaped() {
		return false;
	}

	@Override
	public @NotNull AABBdc getBoundsFromState(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return new AABBd(0, 0, 0, 1, 1, 1);
	}

	@Override
	public @NotNull AABBdc getSelectionAABB(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return new AABBd(0, 1 - BAND_HEIGHT, 0, 1, 1, 1).translate(tilePos.x(), tilePos.y(), tilePos.z());
	}

	// BTA traces the player's look against getSelectionAABB; trace the full block instead so the
	// lower part is still clickable, like CE (where the outline is display-only)
	@Override
	public HitResult collisionRayTrace(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Vector3dc start, @NotNull Vector3dc end, boolean useSelectionBounds) {
		return super.collisionRayTrace(world, tilePos, start, end, false);
	}
}
