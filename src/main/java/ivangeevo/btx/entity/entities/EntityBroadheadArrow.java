package ivangeevo.btx.entity.entities;

import ivangeevo.btx.item.BTXItems;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.projectile.ProjectileArrow;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;

public class EntityBroadheadArrow extends ProjectileArrow {
	public static final int TYPE_BROADHEAD = 4;
	private static final float DAMAGE_MULTIPLIER = 1.5F;

	public EntityBroadheadArrow(@NotNull World world) {
		super(world, TYPE_BROADHEAD);
		this.stack = new ItemStack(BTXItems.BROADHEAD_ARROW);
	}

	public EntityBroadheadArrow(@NotNull World world, double x, double y, double z) {
		super(world, x, y, z, TYPE_BROADHEAD);
		this.stack = new ItemStack(BTXItems.BROADHEAD_ARROW);
	}

	public EntityBroadheadArrow(@NotNull World world, @NotNull Mob owner, boolean doesArrowBelongToPlayer) {
		super(world, owner, doesArrowBelongToPlayer, TYPE_BROADHEAD);
		this.stack = new ItemStack(BTXItems.BROADHEAD_ARROW);
	}

	@Override
	protected void initProjectile() {
		super.initProjectile();
		this.damage = Math.round(this.damage * DAMAGE_MULTIPLIER);
	}
}