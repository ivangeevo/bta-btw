package ivangeevo.btx.item;

import ivangeevo.btx.BTXMod;
import net.minecraft.core.item.material.ArmorMaterial;
import net.minecraft.core.item.material.ToolMaterial;
import net.minecraft.core.util.collection.NamespaceID;
import net.minecraft.core.util.helper.DamageType;

public final class BTXMaterials {
	public static final ToolMaterial SOULFORGED_STEEL_TOOL = new ToolMaterial()
		.setDurability(ToolMaterial.steel.getDurability())
		.setEfficiency(12.0F, 24.0F)
		.setMiningLevel(4)
		.setDamage(4);

	public static final ArmorMaterial SOULFORGED_STEEL_ARMOR = new ArmorMaterial(NamespaceID.fromPool(BTXMod.MOD_ID, "soulforged_steel"), ToolMaterial.steel.getDurability())
		.withProtectionPercentage(DamageType.COMBAT, 60.0F)
		.withProtectionPercentage(DamageType.BLAST, 60.0F)
		.withProtectionPercentage(DamageType.FIRE, 60.0F)
		.withProtectionPercentage(DamageType.FALL, 60.0F);

	private BTXMaterials() {}
}
