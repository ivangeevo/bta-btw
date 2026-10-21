package ivangeevo.btx.recipe.soulforge;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.item.items.ItemArmorSoulforged;
import ivangeevo.btx.util.SoulforgedToolHelper;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.HasJsonAdapter;
import net.minecraft.core.data.registry.recipe.adapter.RecipeJsonAdapter;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerCrafting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class RecipeEntrySoulforgeRepair extends RecipeEntrySoulforgeBase<String, String> implements HasJsonAdapter {
	public static final int MAX_INGOTS = 4;

	public RecipeEntrySoulforgeRepair() {
		super("soulforged_gear", "soulforged_gear");
	}

	@Override
	public boolean matches(ContainerCrafting grid) {
		return findRepairable(grid) != null;
	}

	@Override
	public @Nullable ItemStack getCraftingResult(ContainerCrafting grid) {
		ItemStack target = findRepairable(grid);

		if (target == null) {
			return null;
		}

		int ingots = countIngots(grid);
		int restorePerIngot = (target.getMaxDamage() + MAX_INGOTS - 1) / MAX_INGOTS;
		ItemStack repaired = target.copy();

		repaired.stackSize = 1;
		repaired.setMetadata(Math.max(0, target.getMetadata() - restorePerIngot * ingots));

		return repaired;
	}

	private static @Nullable ItemStack findRepairable(ContainerCrafting grid) {
		ItemStack target = null;
		int ingots = 0;

		for (int i = 0; i < grid.getContainerSize(); i++) {
			ItemStack stack = grid.getItem(i);
			if (stack == null) {
				continue;
			}

			if (isRepairable(stack)) {
				if (target != null) {
					return null;
				}
				target = stack;
			} else if (stack.itemID == BTXItems.SOULFORGED_STEEL_INGOT.id) {
				ingots++;
			} else {
				return null;
			}
		}

		if (target == null || !target.isItemDamaged() || ingots < 1 || ingots > MAX_INGOTS) {
			return null;
		}

		return target;
	}

	private static boolean isRepairable(ItemStack stack) {
		return ItemArmorSoulforged.isSoulforged(stack) || SoulforgedToolHelper.isSoulforgedTool(stack.getItem());
	}

	private static int countIngots(ContainerCrafting grid) {
		int ingots = 0;
		for (int i = 0; i < grid.getContainerSize(); i++) {
			ItemStack stack = grid.getItem(i);
			if (stack != null && stack.itemID == BTXItems.SOULFORGED_STEEL_INGOT.id) {
				ingots++;
			}
		}

		return ingots;
	}

	@Override
	public RecipeJsonAdapter<?> getAdapter() {
		return new JsonAdapter();
	}

	public static class JsonAdapter implements RecipeJsonAdapter<RecipeEntrySoulforgeRepair> {
		@Override
		public RecipeEntrySoulforgeRepair deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
			return new RecipeEntrySoulforgeRepair();
		}

		@Override
		public JsonElement serialize(@NotNull RecipeEntrySoulforgeRepair recipe, Type typeOfSrc, @NotNull JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			object.addProperty("type", Registries.RECIPE_TYPES.getKey(recipe.getClass()));
			object.addProperty("name", recipe.toString());
			return object;
		}
	}
}
