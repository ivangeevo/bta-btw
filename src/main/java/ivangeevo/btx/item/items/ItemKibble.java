package ivangeevo.btx.item.items;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemFood;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemKibble extends ItemFood {
	public ItemKibble(@NotNull String name, @NotNull String namespaceId, int id) {
		super(name, namespaceId, id, 3, 16, true, 16);
	}

	@Override
	public @Nullable ItemStack onUse(@NotNull ItemStack selfStack, @NotNull World world, @NotNull Player player) {
		return selfStack;
	}
}