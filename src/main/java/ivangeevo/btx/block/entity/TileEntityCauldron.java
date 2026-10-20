package ivangeevo.btx.block.entity;

import com.mojang.nbt.tags.CompoundTag;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.cooking.RecipeGroupBulk;
import ivangeevo.btx.util.utils.InventoryUtils;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemFood;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityCauldron extends TileEntityCookingVessel {
	@Override
	public void readAdditionalData(@NotNull CompoundTag compoundTag) {
		super.readAdditionalData(compoundTag);

		if (compoundTag.containsKey("m_iCauldronCookCounter")) {
			cookCounter = compoundTag.getInteger("m_iCauldronCookCounter");
		}

		if (compoundTag.containsKey("m_iRenderCooldownCounter")) {
			stokedCooldownCounter = compoundTag.getInteger("m_iRenderCooldownCounter");
		}

		if (compoundTag.containsKey("m_bContainsValidIngrediantsForState")) {
			containsValidIngredientsForState = compoundTag.getBoolean("m_bContainsValidIngrediantsForState");
		}
	}

	@Override
	public void writeAdditionalData(@NotNull CompoundTag compoundTag) {
		super.writeAdditionalData(compoundTag);

		compoundTag.putInt("m_iCauldronCookCounter", cookCounter);
		compoundTag.putInt("m_iRenderCooldownCounter", stokedCooldownCounter);
	}

	@Override
	public @NotNull String getNameTranslationKey() {
		return "container.cauldron.name";
	}

	@Override
	public void validateContentsForState() {
		containsValidIngredientsForState = false;

		if (fireUnderType == 1) {
			// regular fire

			if (BTXRecipes.CAULDRON != null && BTXRecipes.CAULDRON.getCraftingResult(this) != null) {
				containsValidIngredientsForState = true;
			} else if (containsDung() && containsTaintableFood()) {
				containsValidIngredientsForState = true;
			}
		} else if (fireUnderType == 2) {
			// stoked fire

			if (doesContainExplosives()) {
				containsValidIngredientsForState = true;
			} else if (BTXRecipes.CAULDRON_STOKED != null && BTXRecipes.CAULDRON_STOKED.getCraftingResult(this) != null) {
				containsValidIngredientsForState = true;
			}
		}
	}

	@Override
	protected RecipeGroupBulk getCraftingManager(int fireType) {
		if (fireType == 1) {
			return BTXRecipes.CAULDRON;
		} else if (fireType == 2) {
			return BTXRecipes.CAULDRON_STOKED;
		}

		return null;
	}

	@Override
	protected boolean attemptToCookNormal() {
		// dung spoils the food before anything else cooks
		if (containsDung() && taintAllFood()) {
			return true;
		}

		return super.attemptToCookNormal();
	}

	private boolean containsDung() {
		return InventoryUtils.getFirstOccupiedStackOfItem(this, BTXItems.DUNG.id) >= 0;
	}

	private boolean containsTaintableFood() {
		for (ItemStack stack : contents) {
			if (isTaintableFood(stack)) {
				return true;
			}
		}

		return false;
	}

	private boolean taintAllFood() {
		boolean tainted = false;

		for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
			ItemStack stack = contents[slot];

			if (isTaintableFood(stack)) {
				setItem(slot, new ItemStack(BTXItems.FOUL_FOOD, stack.stackSize));
				tainted = true;
			}
		}

		return tainted;
	}

	private static boolean isTaintableFood(@Nullable ItemStack stack) {
		if (stack == null) return false;

		Item item = stack.getItem();
		return item instanceof ItemFood && item != BTXItems.FOUL_FOOD && item != Items.FOOD_STEW_MUSHROOM;
	}
}
