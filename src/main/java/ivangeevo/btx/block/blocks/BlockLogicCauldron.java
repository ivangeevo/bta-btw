package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.entity.TileEntityCauldron;
import ivangeevo.btx.block.entity.TileEntityCookingVessel;
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

public class BlockLogicCauldron extends BlockLogicCookingVessel {

	public BlockLogicCauldron(@NotNull Block<?> block) {
		super(block, Materials.IRON);
	}

	public static BlockLogicSupplier<BlockLogicCauldron> properties() {
		return block -> new BlockLogicCauldron(
			block
				.setTicking(true)
				.withEntity(TileEntityCauldron::new)
				.withSound(BlockSounds.METAL)
				.withHardness(3.5F)
				.withBlastResistance(10F)
				.withTags(BlockTags.MINEABLE_BY_PICKAXE)
		);
	}

	@Override
	protected void validateFireUnderState(World world, TilePosc tilePos) {
		if (!world.isClientSide) {
			if (world.getTileEntity(tilePos) instanceof TileEntityCauldron tileEntityCauldron) {
				tileEntityCauldron.validateFireUnderType();
			}
		}
	}

	@Override
	protected void displayScreen(@NotNull Player player, @NotNull TileEntityCookingVessel tileEntity) {
		if (tileEntity instanceof TileEntityCauldron tileEntityCauldron) {
			((BTXScreens) player).btx$displayCauldronScreen(tileEntityCauldron);
		}
	}
}
