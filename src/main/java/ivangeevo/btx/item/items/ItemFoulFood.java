package ivangeevo.btx.item.items;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.IBonemealable;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public class ItemFoulFood extends Item {
	public ItemFoulFood(@NotNull String name, @NotNull String namespaceId, int id) {
		super(name, namespaceId, id);
	}

	@Override
	public boolean onUseOnBlock(@NotNull ItemStack selfStack, @NotNull World world, @NotNull Player player, @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		if (world.getBlockType(tilePos).getLogic() instanceof IBonemealable bonemealable) {
			return bonemealable.onBonemealUsed(selfStack, player, world, tilePos, side, xHit, yHit);
		}

		return false;
	}
}
