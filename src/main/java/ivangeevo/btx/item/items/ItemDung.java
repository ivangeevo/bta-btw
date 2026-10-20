package ivangeevo.btx.item.items;

import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemDye;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemDung extends ItemDye {
	public ItemDung(@NotNull String name, @NotNull String namespaceId, int id) {
		super(name, namespaceId, id);
	}

	// no ".xxx"(color) suffix
	@Override
	public @NotNull String getLanguageKey(@NotNull ItemStack selfStack) {
		return getKey();
	}

	@Override
	public boolean useOnEntity(@NotNull ItemStack selfStack, @NotNull Player player, @NotNull Mob mob) {
		ItemStack brown = brownCopy();
		boolean used = super.useOnEntity(brown, player, mob);
		if (used && brown.stackSize < 1) selfStack.consumeItem(player);
		return used;
	}

	@Override
	public boolean onUseOnBlock(@NotNull ItemStack selfStack, @NotNull World world, @Nullable Player player,
								@NotNull TilePosc blockPos, @NotNull Side side, double xPlaced, double yPlaced) {
		ItemStack brown = brownCopy();

		boolean used = super.onUseOnBlock(brown, world, player, blockPos, side, xPlaced, yPlaced);

		if (used && brown.stackSize < 1) {
			selfStack.stackSize--;
		}

		return used;
	}

	private ItemStack brownCopy() {
		return new ItemStack(this, 1, DyeColor.BROWN.itemMeta);
	}
}
