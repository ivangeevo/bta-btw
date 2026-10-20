package ivangeevo.btx.block.entity;

import com.mojang.nbt.tags.CompoundTag;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.cooking.CrucibleIncineration;
import ivangeevo.btx.recipe.cooking.RecipeGroupBulk;
import org.jetbrains.annotations.NotNull;

public class TileEntityCrucible extends TileEntityCookingVessel {

	@Override
	public void readAdditionalData(@NotNull CompoundTag compoundTag) {
		super.readAdditionalData(compoundTag);

		cookCounter = compoundTag.getInteger("m_iCrucibleCookCounter");

		if (compoundTag.containsKey("m_iStokedCooldownCounter")) {
			stokedCooldownCounter = compoundTag.getInteger("m_iStokedCooldownCounter");
		}

		if (compoundTag.containsKey("m_bContainsValidIngrediantsForState")) {
			containsValidIngredientsForState = compoundTag.getBoolean("m_bContainsValidIngrediantsForState");
		}
	}

	@Override
	public void writeAdditionalData(@NotNull CompoundTag compoundTag) {
		super.writeAdditionalData(compoundTag);
		compoundTag.putInt("m_iCrucibleCookCounter", cookCounter);
		compoundTag.putInt("m_iStokedCooldownCounter", stokedCooldownCounter);
	}

	@Override
	public @NotNull String getNameTranslationKey() {
		return "container.crucible.name";
	}

	@Override
	public void validateContentsForState() {
		containsValidIngredientsForState = false;

		if (fireUnderType == 1) {
			// regular fire

			if (BTXRecipes.CRUCIBLE != null && BTXRecipes.CRUCIBLE.getCraftingResult(this) != null) {
				containsValidIngredientsForState = true;
			}
		} else if (fireUnderType == 2) {
			// stoked fire

			if (doesContainExplosives()) {
				containsValidIngredientsForState = true;
			} else if (BTXRecipes.CRUCIBLE_STOKED != null && BTXRecipes.CRUCIBLE_STOKED.getCraftingResult(this) != null) {
				containsValidIngredientsForState = true;
			} else if (getFirstStackThatContainsItemsDestroyedByStokedFire() >= 0) {
				containsValidIngredientsForState = true;
			}
		}
	}

	@Override
	protected RecipeGroupBulk getCraftingManager(int fireType) {
		if (fireType == 1) {
			return BTXRecipes.CRUCIBLE;
		} else if (fireType == 2) {
			return BTXRecipes.CRUCIBLE_STOKED;
		}

		return null;
	}

	@Override
	protected boolean attemptToCookStoked() {
		int burnableSlot = getFirstStackThatContainsItemsDestroyedByStokedFire();

		if (burnableSlot >= 0) {
			removeItem(burnableSlot, 1);

			return true;
		}

		return super.attemptToCookStoked();
	}

	private int getFirstStackThatContainsItemsDestroyedByStokedFire() {
		for (int i = 0; i < getContainerSize(); i++) {
			if (CrucibleIncineration.isIncinerated(getItem(i))) {
				return i;
			}
		}

		return -1;
	}
}
