package ivangeevo.btx.block.blocks;

import ivangeevo.btx.block.blocks.util.ItemOnTopCollision;
import ivangeevo.btx.block.entity.TileEntityCookingVessel;
import ivangeevo.btx.sound.BTXSoundTypes;
import ivangeevo.btx.util.utils.InventoryUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;

import java.util.List;
import java.util.Random;

public abstract class BlockLogicCookingVessel extends BlockLogicVessel implements ItemOnTopCollision {
	public BlockLogicCookingVessel(@NotNull Block<?> block, @NotNull Material material) {
		super(block, material);
	}

	@Override
	public void onEntityCollision(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Entity entity) {
		if (world.isClientSide) {
			// don't collect items on the client, as it's dependent on the state of the inventory
			return;
		}

		if (getMechanicallyPoweredFlag(world, tilePos)) {
			// tilted blocks can't collect
			return;
		}

		// check for items within the collection zone
		List<EntityItem> collisionList = world.getEntitiesWithinAABB(EntityItem.class,
			new AABBd(tilePos.x(), tilePos.y() + COLLISION_BOX_HEIGHT, tilePos.z(),
				tilePos.x() + 1, tilePos.y() + COLLISION_BOX_HEIGHT + 0.05D, tilePos.z() + 1));

		if (!collisionList.isEmpty()) {
			TileEntity tileEnt = world.getTileEntity(tilePos);

			if (!(tileEnt instanceof Container inventoryEntity)) {
				return;
			}

			for (EntityItem targetEntityItem : collisionList) {
				if (targetEntityItem.isAlive()) {
					if (InventoryUtils.addItemStackToInventory(inventoryEntity, targetEntityItem.item)) {
						SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.ITEM_COLLECT, 0.25F,
							((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

						targetEntityItem.remove();
					}
				}
			}
		}
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand, boolean isRandomTick) {
		validateFireUnderState(world, tilePos);
		super.updateTick(world, tilePos, rand, isRandomTick);
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		validateFireUnderState(world, tilePos);
		super.onNeighborChanged(world, tilePos, block);
	}

	@Override
	public boolean onInteracted(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Player player, @Nullable Side side, double xHit, double yHit) {
		if (!isOpenSideBlocked(world, tilePos)) {
			if (!world.isClientSide) {
				TileEntity tileEnt = world.getTileEntity(tilePos);

				if (tileEnt instanceof TileEntityCookingVessel vesselEntity) {
					displayScreen(player, vesselEntity);
				}
			}
		}

		return true;
	}

	@Override
	public boolean isEquivalent(@NotNull WorldSource world, @NotNull TilePosc tilePos, @NotNull TilePosc otherPos, @NotNull Block<?> otherBlock) {
		if (!(otherBlock.getLogic() instanceof BlockLogicCookingVessel)) {
			return super.isEquivalent(world, tilePos, otherPos, otherBlock);
		}

		return getComparatorLevel(world, tilePos) == getComparatorLevel(world, otherPos);
	}

	public static int getComparatorLevel(WorldSource world, TilePosc tilePos) {
		return world.getTileEntity(tilePos) instanceof Container container
			? InventoryUtils.calcRedstoneFromInventory(container)
			: 0;
	}

	abstract protected void validateFireUnderState(World world, TilePosc tilePos);

	abstract protected void displayScreen(@NotNull Player player, @NotNull TileEntityCookingVessel tileEntity);
}
