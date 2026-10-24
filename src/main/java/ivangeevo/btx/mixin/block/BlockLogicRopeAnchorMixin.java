package ivangeevo.btx.mixin.block;

import ivangeevo.btx.block.blocks.BlockLogicAnchor;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicRope;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockLogicRope.class)
public abstract class BlockLogicRopeAnchorMixin extends BlockLogic {
	private BlockLogicRopeAnchorMixin(Block<?> block, Material material) {
		super(block, material);
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> neighbor) {
		super.onNeighborChanged(world, tilePos, neighbor);

		if (world.isClientSide || !BlockLogicAnchor.isAnchoredRope(world, tilePos)) {
			return;
		}

		if (!BlockLogicAnchor.canSupportAnchoredRope(world, tilePos.up(new TilePos()))) {
			this.dropWithCause(world, EnumDropCause.WORLD, tilePos, world.getBlockData(tilePos), null, null);
			world.setBlockTypeNotify(tilePos, Blocks.AIR);
		}
	}
}