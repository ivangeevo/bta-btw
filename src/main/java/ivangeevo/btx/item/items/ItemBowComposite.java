package ivangeevo.btx.item.items;

import ivangeevo.btx.entity.entities.EntityBroadheadArrow;
import ivangeevo.btx.item.BTXItems;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.entity.projectile.Projectile;
import net.minecraft.core.entity.projectile.ProjectileArrow;
import net.minecraft.core.entity.projectile.ProjectileArrowFlaming;
import net.minecraft.core.entity.projectile.ProjectileArrowGolden;
import net.minecraft.core.entity.projectile.ProjectileArrowPurple;
import net.minecraft.core.enums.HumanArmorShape;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ItemBowComposite extends Item {
	public static final int MAX_DAMAGE = 576;
	private static final double VELOCITY_MULTIPLIER = 1.5D;

	public ItemBowComposite(@NotNull String name, @NotNull String namespaceId, int id) {
		super(name, namespaceId, id);
		this.maxStackSize = 1;
		this.setMaxDamage(MAX_DAMAGE);
	}

	@Override
	public @Nullable ItemStack onUse(@NotNull ItemStack selfStack, @NotNull World world, @NotNull Player player) {
		ItemStack quiverSlot = player.getItemInArmorSlot(HumanArmorShape.CHEST);

		if (quiverSlot != null && quiverSlot.itemID == Items.ARMOR_QUIVER.id && quiverSlot.getMetadata() < quiverSlot.getMaxDamage()) {
			quiverSlot.damageItem(1, player);
			shoot(selfStack, world, player, () -> new ProjectileArrow(world, player, true, 0));
		} else if (quiverSlot != null && quiverSlot.itemID == Items.ARMOR_QUIVER_GOLD.id) {
			shoot(selfStack, world, player, () -> new ProjectileArrowPurple(world, player, false));
		} else if (player.inventory.consumeInventoryItem(BTXItems.BROADHEAD_ARROW.id)) {
			shoot(selfStack, world, player, () -> new EntityBroadheadArrow(world, player, true));
		} else if (player.inventory.consumeInventoryItem(Items.AMMO_ARROW_FLAMING.id)) {
			shoot(selfStack, world, player, () -> new ProjectileArrowFlaming(world, player, true));
		} else if (player.inventory.consumeInventoryItem(Items.AMMO_ARROW_GOLD.id)) {
			shoot(selfStack, world, player, () -> new ProjectileArrowGolden(world, player, true));
		} else if (player.inventory.consumeInventoryItem(Items.AMMO_ARROW.id)) {
			shoot(selfStack, world, player, () -> new ProjectileArrow(world, player, true, 0));
		}

		return selfStack;
	}

	private void shoot(ItemStack selfStack, World world, Player player, Supplier<Projectile> arrowFactory) {
		selfStack.damageItem(1, player);
		world.playSoundAtEntity(player, player, "random.bow", 0.3F, 1.0F / (itemRand.nextFloat() * 0.4F + 0.8F));

		if (!world.isClientSide) {
			Projectile arrow = arrowFactory.get();
			arrow.xd *= VELOCITY_MULTIPLIER;
			arrow.yd *= VELOCITY_MULTIPLIER;
			arrow.zd *= VELOCITY_MULTIPLIER;
			world.entityJoinedWorld(arrow);
		}
	}
}