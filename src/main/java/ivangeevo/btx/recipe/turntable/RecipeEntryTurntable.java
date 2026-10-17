package ivangeevo.btx.recipe.turntable;

import com.google.gson.*;
import ivangeevo.btx.recipe.impl.RecipeEntryBTX;
import net.minecraft.core.block.Block;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.HasJsonAdapter;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.adapter.RecipeJsonAdapter;
import net.minecraft.core.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** input = block above the turntable, output = what it becomes, after `rotations` full turns. */
public class RecipeEntryTurntable extends RecipeEntryBTX implements HasJsonAdapter {
	public static final int DEFAULT_ROTATIONS = 8;

	private int rotations = DEFAULT_ROTATIONS;
	private List<ItemStack> extraOutputs = new ArrayList<>();

	private List<Integer> inputData = new ArrayList<>(); // empty = any data (CE: IGNORE_METADATA)
	private String effectSound;            // optional, played on each rotation of progress
	private String completionSound;        // optional, falls back to effectSound

	public RecipeEntryTurntable(RecipeSymbol input, ItemStack output, int rotations, List<ItemStack> extraOutputs) {
		super(input, output);
		this.rotations = Math.max(1, rotations);
		this.extraOutputs = extraOutputs;
	}

	public RecipeEntryTurntable() { super(); }

	/** CE: matchesInputs(block, metadata). */
	public boolean matchesBlock(Block<?> block, int data) {
		boolean sameBlock = false;
		List<ItemStack> stacks = getInput().resolve();
		if (stacks != null) {
			for (ItemStack s : stacks) {
				if (s != null && s.getItem().equals(block.asItem())) {
					sameBlock = true; break;
				}
			}
		}
		return sameBlock && (inputData.isEmpty() || inputData.contains(data));
	}

	public int getRotations() { return rotations; }

	public List<ItemStack> getExtraOutputs() { return extraOutputs; }

	public String getCompletionSound() { return completionSound != null ? completionSound : effectSound; }
	public String getEffectSound() { return effectSound; }

	@Override
	public RecipeJsonAdapter<?> getAdapter() { return new JsonAdapter(); }

	public static class JsonAdapter implements RecipeJsonAdapter<RecipeEntryTurntable> {
		@Override
		public RecipeEntryTurntable deserialize(@NotNull JsonElement json, Type type, @NotNull JsonDeserializationContext ctx) throws JsonParseException {
			JsonObject o = json.getAsJsonObject();

			List<Integer> data = new ArrayList<>();
			if (o.has("input_data")) for (JsonElement e : o.getAsJsonArray("input_data")) data.add(e.getAsInt());
			String sound = o.has("effect_sound") ? o.get("effect_sound").getAsString() : null;
			String doneSound = o.has("completion_sound") ? o.get("completion_sound").getAsString() : null;

			RecipeSymbol input = ctx.deserialize(o.get("input"), RecipeSymbol.class);
			ItemStack output = ctx.deserialize(o.get("output"), ItemStack.class);
			int rotations = o.has("rotations") ? o.get("rotations").getAsInt() : DEFAULT_ROTATIONS;
			List<ItemStack> extras = new ArrayList<>();
			if (o.has("extra_outputs")) {
				for (JsonElement e : o.getAsJsonArray("extra_outputs")) {
					extras.add(ctx.deserialize(e, ItemStack.class));
				}
			}
			return new RecipeEntryTurntable(input, output, rotations, extras);
		}

		@Override
		public JsonElement serialize(@NotNull RecipeEntryTurntable r, Type type, @NotNull JsonSerializationContext ctx) {
			JsonObject o = new JsonObject();
			o.addProperty("type", Registries.RECIPE_TYPES.getKey(r.getClass()));
			o.addProperty("name", r.toString());
			o.add("input", ctx.serialize(r.getInput(), RecipeSymbol.class));
			o.add("output", ctx.serialize(r.getOutput(), ItemStack.class));
			o.addProperty("rotations", r.getRotations());

			if (!r.inputData.isEmpty()) { JsonArray a = new JsonArray(); r.inputData.forEach(a::add); o.add("input_data", a); }
			if (r.effectSound != null) o.addProperty("effect_sound", r.effectSound);
			if (r.completionSound != null) o.addProperty("completion_sound", r.completionSound);

			if (!r.getExtraOutputs().isEmpty()) {
				JsonArray arr = new JsonArray();
				for (ItemStack s : r.getExtraOutputs()) arr.add(ctx.serialize(s, ItemStack.class));
				o.add("extra_outputs", arr);
			}
			return o;
		}
	}
}
