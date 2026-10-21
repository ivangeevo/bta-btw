package ivangeevo.btx.gui.guidebook;

import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.saw.RecipeEntrySaw;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.GuidebookPage;
import net.minecraft.core.data.registry.recipe.SearchQuery;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.DyeColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class GuidebookSectionSaw extends GuidebookSectionBTX<RecipeEntrySawDisplay> {
	private static final Pattern COLOR_TOKEN = buildColorPattern();

	private int cachedRecipeCount = -1;
	private List<RecipeEntrySawDisplay> displays = new ArrayList<>();

	public GuidebookSectionSaw(String translationKey, ItemStack tabIcon, int bgColor, int fgColor) {
		super(translationKey, tabIcon, bgColor, fgColor);
	}

	private static Pattern buildColorPattern() {
		StringBuilder colors = new StringBuilder();
		for (DyeColor color : DyeColor.values()) {
			if (!colors.isEmpty()) {
				colors.append('|');
			}
			colors.append(Pattern.quote(color.colorID));
		}

		return Pattern.compile("_(" + colors + ")_");
	}

	@Override
	protected List<RecipeEntrySawDisplay> getRecipes() {
		if (BTXRecipes.SAW == null) {
			return new ArrayList<>();
		}

		List<RecipeEntrySaw> cuts = BTXRecipes.SAW.getCutRecipes();
		if (cuts.size() != this.cachedRecipeCount) {
			this.cachedRecipeCount = cuts.size();

			Map<String, List<RecipeEntrySaw>> families = new LinkedHashMap<>();
			for (RecipeEntrySaw cut : cuts) {
				String family = COLOR_TOKEN.matcher(String.valueOf(cut)).replaceFirst("_*_");
				families.computeIfAbsent(family, key -> new ArrayList<>()).add(cut);
			}

			List<RecipeEntrySawDisplay> built = new ArrayList<>();
			for (List<RecipeEntrySaw> family : families.values()) {
				RecipeEntrySawDisplay display = new RecipeEntrySawDisplay(family);
				if (!display.getFrames().isEmpty()) {
					built.add(display);
				}
			}

			this.displays = built;
		}

		return this.displays;
	}

	@Override
	protected boolean matchesQuery(RecipeEntrySawDisplay recipe, SearchQuery query) {
		boolean matches = switch (query.mode) {
			case ALL -> matchesRecipe(recipe, query) || matchesUsage(recipe, query);
			case RECIPE -> matchesRecipe(recipe, query);
			case USAGE -> matchesUsage(recipe, query);
		};

		return matches && matchesScope(recipe.getRecipe(), query);
	}

	private static boolean matchesRecipe(RecipeEntrySawDisplay recipe, SearchQuery query) {
		for (RecipeEntrySawDisplay.Frame frame : recipe.getFrames()) {
			for (ItemStack output : frame.outputs()) {
				if (matchesStack(output, query)) {
					return true;
				}
			}
		}

		return false;
	}

	private static boolean matchesUsage(RecipeEntrySawDisplay recipe, SearchQuery query) {
		for (RecipeEntrySawDisplay.Frame frame : recipe.getFrames()) {
			if (matchesStack(frame.input(), query)) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected int getRecipesPerPage() {
		return RecipePageSaw.RECIPES_PER_PAGE;
	}

	@Override
	protected GuidebookPage createPage(List<RecipeEntrySawDisplay> recipes) {
		return new RecipePageSaw(this, recipes);
	}
}
