package ivangeevo.btx.util;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.entity.FeedableWolf;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.sound.BTXSoundTypes;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.animal.MobWolf;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;

import java.util.ArrayList;
import java.util.List;

public class WolfDungHelper {
	private static final int CHANCE_OF_DUNG = 24000;
	private static final int DARKNESS_LIGHT_LEVEL = 5;

	public static void rollForDung(MobWolf wolf) {
		int chanceOfDung = isDarkEnoughToAffectDung(wolf) ? 2 : 1;
		if (wolf.world.rand.nextInt(CHANCE_OF_DUNG) < chanceOfDung) {
			attemptToDung(wolf);
		}
	}

	public static boolean isDung(Object entity) {
		return entity instanceof EntityItem entityItem && entityItem.item != null && entityItem.item.itemID == BTXItems.DUNG.id;
	}

	public static <T> List<T> withoutDung(List<T> entities) {
		List<T> filtered = new ArrayList<>(entities.size());

		for (T entity : entities) {
			if (!isDung(entity)) {
				filtered.add(entity);
			}
		}

		return filtered;
	}

	private static void attemptToDung(MobWolf wolf) {
		World world = wolf.world;

		float dungVectorX = MathHelper.sin(wolf.yRot / 180.0F * (float) Math.PI);
		float dungVectorZ = -MathHelper.cos(wolf.yRot / 180.0F * (float) Math.PI);

		double dungX = wolf.x + dungVectorX;
		double dungY = wolf.y + 0.25D;
		double dungZ = wolf.z + dungVectorZ;

		if (!isPathOpenToDung(world, wolf, MathHelper.floor(dungX), MathHelper.floor(dungY), MathHelper.floor(dungZ))) {
			return;
		}

		EntityItem entityItem = new EntityItem(world, dungX, dungY, dungZ, new ItemStack(BTXItems.DUNG));

		float velocityFactor = 0.05F;
		entityItem.xd = dungVectorX * 10.0F * velocityFactor;
		entityItem.zd = dungVectorZ * 10.0F * velocityFactor;
		entityItem.yd = (float) world.rand.nextGaussian() * velocityFactor + 0.2F;
		entityItem.pickupDelay = 10;

		world.entityJoinedWorld(entityItem);
		world.playSoundAtEntity(null, wolf, BTXSoundTypes.WOLF_DUNG, 0.2F, 1.25F);
		world.playSoundAtEntity(null, wolf, BTXSoundTypes.WOLF_DUNG_GROWL, ((FeedableWolf) wolf).btx$getSoundVolume(),
			(world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);

		for (int counter = 0; counter < 5; counter++) {
			double smokeX = wolf.x + dungVectorX * 0.5F + world.rand.nextDouble() * 0.25D;
			double smokeY = wolf.y + world.rand.nextDouble() * 0.5D + 0.25D;
			double smokeZ = wolf.z + dungVectorZ * 0.5F + world.rand.nextDouble() * 0.25D;

			world.spawnParticle("smoke", smokeX, smokeY, smokeZ, 0.0D, 0.0D, 0.0D, 0, true);
		}
	}

	private static boolean isPathOpenToDung(World world, MobWolf wolf, int x, int y, int z) {
		if (!isBlockOpenToDung(world, x, y, z)) {
			return false;
		}

		int wolfX = MathHelper.floor(wolf.x);
		int wolfZ = MathHelper.floor(wolf.z);

		if (x != wolfX && z != wolfZ) {
			return isBlockOpenToDung(world, wolfX, y, z) || isBlockOpenToDung(world, x, y, wolfZ);
		}

		return true;
	}

	private static boolean isBlockOpenToDung(World world, int x, int y, int z) {
		Block<?> block = world.getBlockType(new TilePos(x, y, z));

		if (block == Blocks.AIR || block == Blocks.FIRE || block == BTXBlocks.STOKED_FIRE) {
			return true;
		}

		Material material = block.getMaterial();

		return material.isLiquid() || material.isReplaceable();
	}

	private static boolean isDarkEnoughToAffectDung(MobWolf wolf) {
		TilePos pos = new TilePos(MathHelper.floor(wolf.x), MathHelper.floor(wolf.y), MathHelper.floor(wolf.z));
		return wolf.world.getBlockLightValue(pos) <= DARKNESS_LIGHT_LEVEL;
	}
}
