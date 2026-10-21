package ivangeevo.btx.recipe.cooking;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import ivangeevo.btx.util.utils.InventoryUtils;
import net.minecraft.core.data.registry.recipe.RecipeEntryBase;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.Container;

import java.util.ArrayList;
import java.util.List;

public abstract class RecipeEntryBulk extends RecipeEntryBase<List<RecipeSymbol>, List<ItemStack>, Void> {
	protected RecipeEntryBulk(List<RecipeSymbol> inputs, List<ItemStack> outputs) {
		super(inputs, outputs, null);
	}

	protected RecipeEntryBulk() {
		super();
	}

	public boolean doesInventoryContainIngredients(Container inventory) {
		for (RecipeSymbol input : getInput()) {
			if (!isSymbolSatisfied(input, inventory)) {
				return false;
			}
		}

		return true;
	}

	public boolean consumeInventoryIngredients(Container inventory) {
		boolean successful = true;

		for (RecipeSymbol input : getInput()) {
			if (!consumeSymbol(input, inventory)) {
				successful = false;
			}
		}

		return successful;
	}

	public List<ItemStack> getCraftingOutputList() {
		List<ItemStack> copies = new ArrayList<>();

		for (ItemStack output : getOutput()) {
			copies.add(output.copy());
		}

		return copies;
	}

	// a group symbol is satisfied if any one of its resolved variants alone meets the required amount
	private boolean isSymbolSatisfied(RecipeSymbol symbol, Container inventory) {
		for (ItemStack resolved : symbol.resolve()) {
			int count = InventoryUtils.countItemsInInventory(inventory, resolved.itemID, resolvedMetadata(resolved));

			if (count >= requiredAmount(symbol)) {
				return true;
			}
		}

		return false;
	}

	private boolean consumeSymbol(RecipeSymbol symbol, Container inventory) {
		for (ItemStack resolved : symbol.resolve()) {
			int metadata = resolvedMetadata(resolved);
			int count = InventoryUtils.countItemsInInventory(inventory, resolved.itemID, metadata);

			if (count >= requiredAmount(symbol)) {
				return InventoryUtils.consumeItemsInInventory(inventory, resolved.itemID, metadata, requiredAmount(symbol));
			}
		}

		return false;
	}

	private static int requiredAmount(RecipeSymbol symbol) {
		ItemStack stack = symbol.getStack();
		int stackSize = stack != null && symbol.getItemGroup() == null ? Math.max(1, stack.stackSize) : 1;
		return stackSize * symbol.getAmount();
	}

	private static int resolvedMetadata(ItemStack resolved) {
		return resolved.getMetadata() == -1 ? InventoryUtils.IGNORE_METADATA : resolved.getMetadata();
	}

	public static List<RecipeSymbol> parseInputs(JsonObject object, JsonDeserializationContext context) {
		List<RecipeSymbol> inputs = new ArrayList<>();

		for (var element : object.getAsJsonArray("inputs")) {
			inputs.add(context.deserialize(element, RecipeSymbol.class));
		}

		return inputs;
	}

	public static List<ItemStack> parseOutputs(JsonObject object, JsonDeserializationContext context) {
		List<ItemStack> outputs = new ArrayList<>();

		for (var element : object.getAsJsonArray("outputs")) {
			outputs.add(context.deserialize(element, ItemStack.class));
		}

		return outputs;
	}

	public static JsonArray serializeInputs(List<RecipeSymbol> inputs, JsonSerializationContext context) {
		JsonArray array = new JsonArray();

		for (RecipeSymbol input : inputs) {
			array.add(context.serialize(input, RecipeSymbol.class));
		}

		return array;
	}

	public static JsonArray serializeOutputs(List<ItemStack> outputs, JsonSerializationContext context) {
		JsonArray array = new JsonArray();

		for (ItemStack output : outputs) {
			array.add(context.serialize(output, ItemStack.class));
		}

		return array;
	}
}
