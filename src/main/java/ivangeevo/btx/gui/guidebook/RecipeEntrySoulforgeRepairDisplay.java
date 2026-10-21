package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.recipe.soulforge.RecipeEntrySoulforgeRepair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.data.registry.recipe.RecipeEntryBase;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;

// guidebook-only view of the repair recipe for one item group; it is never registered as a recipe
@Environment(EnvType.CLIENT)
public class RecipeEntrySoulforgeRepairDisplay extends RecipeEntryBase<RecipeSymbol, RecipeSymbol, Void> {
	private static final RecipeSymbol INGOT = new RecipeSymbol(new ItemStack(BTXItems.SOULFORGED_STEEL_INGOT));

	public RecipeEntrySoulforgeRepairDisplay(RecipeEntrySoulforgeRepair source, String itemGroup) {
		super(new RecipeSymbol(itemGroup), new RecipeSymbol(itemGroup), null);
		this.parent = source.parent;
	}

	public RecipeSymbol getIngot() {
		return INGOT;
	}
}