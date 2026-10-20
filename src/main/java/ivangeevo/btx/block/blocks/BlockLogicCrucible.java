package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.entity.TileEntityCookingVessel;
import ivangeevo.btx.block.entity.TileEntityCrucible;
import ivangeevo.btx.gui.BTXScreens;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public class BlockLogicCrucible extends BlockLogicCookingVessel {

	public BlockLogicCrucible(@NotNull Block<?> block) {
		super(block, Materials.STONE);
	}

	public static BlockLogicSupplier<BlockLogicCrucible> properties() {
		return block -> new BlockLogicCrucible(
			block
				.setTicking(true)
				.withEntity(TileEntityCrucible::new)
				.withSound(BlockSounds.GLASS)
				.withHardness(0.6F)
				.withBlastResistance(3F)
				.withTags(BlockTags.MINEABLE_BY_PICKAXE)
		);
	}

	@Override
	protected void validateFireUnderState(World world, TilePosc tilePos) {
		if (!world.isClientSide) {
			if (world.getTileEntity(tilePos) instanceof TileEntityCrucible tileEntityCrucible) {
				tileEntityCrucible.validateFireUnderType();
			}
		}
	}

	@Override
	protected void displayScreen(@NotNull Player player, @NotNull TileEntityCookingVessel tileEntity) {
		if (tileEntity instanceof TileEntityCrucible tileEntityCrucible) {
			((BTXScreens) player).btx$displayCrucibleScreen(tileEntityCrucible);
		}
	}
}
