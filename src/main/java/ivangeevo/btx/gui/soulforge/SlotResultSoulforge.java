package ivangeevo.btx.gui.soulforge;

import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.soulforge.RecipeEntrySoulforgeBase;
import ivangeevo.btx.sound.BTXSoundTypes;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.player.inventory.container.ContainerCrafting;
import net.minecraft.core.player.inventory.slot.SlotResult;
import net.minecraft.core.world.World;

public class SlotResultSoulforge extends SlotResult {
	private final Player player;
	private final ContainerCrafting grid;
	private long lastCraftSoundTick = -1L;

	public SlotResultSoulforge(Player player, ContainerCrafting grid, Container result, int index, int x, int y) {
		super(player, grid, result, index, x, y);
		this.player = player;
		this.grid = grid;
	}

	@Override
	public void onTake(ItemStack itemStack) {
		itemStack.onCrafting(this.player.world, this.player);
		RecipeEntrySoulforgeBase<?, ?> recipe = BTXRecipes.SOULFORGE.findRecipe(this.grid);

		if (recipe != null) {
			recipe.onCraftResult(this.grid);
		}

		playCraftSound();
	}

	private void playCraftSound() {
		World world = this.player.world;

		if (world.isClientSide || world.getWorldTime() == this.lastCraftSoundTick) {
			return;
		}

		this.lastCraftSoundTick = world.getWorldTime();
		world.playSoundAtEntity(null, this.player, BTXSoundTypes.SOULFORGE_CRAFT, 0.5F, world.rand.nextFloat() * 0.25F + 1.25F);
	}
}
