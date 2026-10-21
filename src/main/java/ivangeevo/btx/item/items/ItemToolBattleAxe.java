package ivangeevo.btx.item.items;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.material.ToolMaterial;
import net.minecraft.core.item.tool.ItemToolAxe;
import org.jetbrains.annotations.NotNull;

public class ItemToolBattleAxe extends ItemToolAxe {
	private static final int EXTRA_DAMAGE = 3;

	public ItemToolBattleAxe(@NotNull String name, @NotNull String namespaceId, int id, ToolMaterial material) {
		super(name, namespaceId, id, material);
	}

	@Override
	public int getDamageVsEntity(ItemStack stack, Entity entity) {
		return super.getDamageVsEntity(stack, entity) + EXTRA_DAMAGE;
	}
}