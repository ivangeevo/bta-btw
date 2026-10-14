package ivangeevo.btx.util.utils;

import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public class MiscUtils {

	public static void ejectStackWithRandomOffset(World world, TilePosc tilePos, ItemStack stack) {
		if (world.isClientSide || stack == null) return;

		double x = tilePos.x() + world.rand.nextFloat() * 0.7F + 0.15F;
		double y = tilePos.y() + world.rand.nextFloat() * 0.2F + 0.1F;
		double z = tilePos.z() + world.rand.nextFloat() * 0.7F + 0.15F;

		EntityItem entity = new EntityItem(world, x, y, z, stack);

		float spread = 0.05F;

		entity.xd = (float) world.rand.nextGaussian() * spread;
		entity.yd = (float) world.rand.nextGaussian() * spread + 0.2F;
		entity.zd = (float) world.rand.nextGaussian() * spread;
		entity.pickupDelay = 10;

		world.entityJoinedWorld(entity);
	}

	public static void ejectSingleItemWithRandomOffset(World world, TilePosc tilePos, int itemId, int iDamage) {
		ItemStack itemStack = new ItemStack(itemId, 1, iDamage);
		ejectStackWithRandomOffset(world, tilePos, itemStack);
	}
}
