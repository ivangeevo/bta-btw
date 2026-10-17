package ivangeevo.btx.recipe.turntable;

import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.util.utils.MiscUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.block.ItemBlock;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public final class TurntableCrafting {
	private TurntableCrafting() {}

	public static int onRotated(World world, TilePosc blockPos, int counter) {
		RecipeEntryTurntable recipe = BTXRecipes.TURNTABLE.findRecipe(world, blockPos);

		if (recipe == null) return counter;

		playCraftSound(world, blockPos);

		counter++;
		if (counter < recipe.getRotations()) {
			return counter;
		}

		transform(world, blockPos, recipe);

		return 0;
	}

	private static void playCraftSound(World world, TilePosc pos) {
		var sound = world.getBlockType(pos).getSound();
		SoundUtils.playAtTileCenter(
			world,
			pos,
			SoundCategory.WORLD_SOUNDS,
			sound.getBreakSoundName(),
			(sound.getVolume() + 1.0F) / 2.0F,
			sound.getPitch() * 0.8F
		);
	}

	private static void transform(World world, TilePosc pos, RecipeEntryTurntable recipe) {
		ItemStack out = recipe.getOutput();

		// see note 2: turning an ItemStack back into a block + data
		if (out.getItem() instanceof ItemBlock<?> ib) {
			world.setBlockTypeDataNotify(pos, ib.getBlock(), out.getMetadata());
		} else {
			world.setBlockTypeNotify(pos, Blocks.AIR);
			MiscUtils.ejectStackWithRandomOffset(world, pos, out.copy());
		}

		for (ItemStack extra : recipe.getExtraOutputs()) {
			MiscUtils.ejectStackWithRandomOffset(world, pos, extra.copy());
		}
	}
}
