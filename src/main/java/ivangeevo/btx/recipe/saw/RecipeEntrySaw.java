package ivangeevo.btx.recipe.saw;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import ivangeevo.btx.util.utils.MiscUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.data.registry.recipe.HasJsonAdapter;
import net.minecraft.core.data.registry.recipe.RecipeSymbol;
import net.minecraft.core.data.registry.recipe.adapter.RecipeJsonAdapter;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.block.ItemBlock;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class RecipeEntrySaw extends RecipeEntrySawBase<RecipeSymbol, List<ItemStack>> implements HasJsonAdapter {
	public enum SawFacing {
		ANY, VERTICAL, HORIZONTAL;

		public boolean accepts(Direction sawFacing) {
			boolean vertical = sawFacing == Direction.UP || sawFacing == Direction.DOWN;
			return switch (this) {
				case ANY -> true;
				case VERTICAL -> vertical;
				case HORIZONTAL -> !vertical;
			};
		}
	}

	private static final int ALL_BITS = 0xFF;

	private @Nullable Integer data = null;
	private int dataMask = ALL_BITS;
	private SawFacing facing = SawFacing.ANY;
	private boolean drops = false;
	private @Nullable ItemStack leaves = null;
	private @Nullable String sound = null;
	private float soundVolume = 1.0F;
	private boolean blood = false;
	private int displayMeta = 0;
	private @Nullable String note = null;
	private @Nullable Set<Block<?>> inputBlocks = null;

	public RecipeEntrySaw(RecipeSymbol input, List<ItemStack> outputs) {
		super(input, outputs);
	}

	public RecipeEntrySaw() {
		super();
	}

	public SawFacing getFacing() {
		return this.facing;
	}

	public boolean dropsBlockItems() {
		return this.drops;
	}

	public int getDisplayMeta() {
		return this.displayMeta;
	}

	public @Nullable String getNote() {
		return this.note;
	}

	@Override
	public boolean matches(Block<?> block, int blockData, Direction sawFacing) {
		if (this.inputBlocks == null) {
			this.inputBlocks = resolveBlocks(List.of(getInput()));
		}

		return this.inputBlocks.contains(block)
			&& (this.data == null || (blockData & this.dataMask) == this.data)
			&& this.facing.accepts(sawFacing);
	}

	@Override
	public int getSpecificity() {
		int dataBits = this.data != null ? 1 + Integer.bitCount(this.dataMask) : 0;
		return dataBits * 2 + (this.facing != SawFacing.ANY ? 1 : 0);
	}

	public void apply(World world, TilePosc pos, Random rand) {
		Block<?> block = world.getBlockType(pos);
		int blockData = world.getBlockData(pos);
		List<ItemStack> results = new ArrayList<>();

		if (this.drops) {
			ItemStack[] dropped = block.getLogic().getBreakResult(world, EnumDropCause.WORLD, pos, blockData, world.getTileEntity(pos));
			if (dropped != null) {
				for (ItemStack stack : dropped) {
					if (stack != null) {
						results.add(stack.copy());
					}
				}
			}
		}

		for (ItemStack output : getOutput()) {
			results.add(output.copy());
		}

		if (this.leaves != null && this.leaves.getItem() instanceof ItemBlock<?> leftBlock) {
			world.setBlockTypeDataNotify(pos, leftBlock.getBlock(), this.leaves.getMetadata());
		} else {
			world.setBlockTypeNotify(pos, Blocks.AIR);
		}

		for (ItemStack stack : results) {
			MiscUtils.ejectStackWithRandomOffset(world, pos, stack);
		}

		if (this.sound != null) {
			SoundUtils.playAtTileCenter(world, pos, SoundCategory.WORLD_SOUNDS, this.sound, this.soundVolume,
				(rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
		}

		if (this.blood) {
			for (int i = 0; i < 10; i++) {
				world.spawnParticle("reddust", pos.x() + rand.nextFloat(), pos.y() + rand.nextFloat(), pos.z() + rand.nextFloat(),
					0.0, 0.0, 0.0, 0, false);
			}
		}
	}

	@Override
	public RecipeJsonAdapter<?> getAdapter() {
		return new JsonAdapter();
	}

	public static class JsonAdapter implements RecipeJsonAdapter<RecipeEntrySaw> {
		@Override
		public RecipeEntrySaw deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();

			RecipeSymbol input = context.deserialize(object.get("input"), RecipeSymbol.class);
			List<ItemStack> outputs = new ArrayList<>();
			if (object.has("outputs")) {
				for (JsonElement element : object.getAsJsonArray("outputs")) {
					outputs.add(context.deserialize(element, ItemStack.class));
				}
			}

			RecipeEntrySaw recipe = new RecipeEntrySaw(input, outputs);
			if (object.has("data")) {
				recipe.data = object.get("data").getAsInt();
			}
			if (object.has("data_mask")) {
				recipe.dataMask = object.get("data_mask").getAsInt();
			}
			if (object.has("facing")) {
				recipe.facing = SawFacing.valueOf(object.get("facing").getAsString().toUpperCase(Locale.ROOT));
			}
			if (object.has("drops")) {
				recipe.drops = object.get("drops").getAsBoolean();
			}
			if (object.has("leaves")) {
				recipe.leaves = context.deserialize(object.get("leaves"), ItemStack.class);
			}
			if (object.has("sound")) {
				recipe.sound = object.get("sound").getAsString();
			}
			if (object.has("sound_volume")) {
				recipe.soundVolume = object.get("sound_volume").getAsFloat();
			}
			if (object.has("blood")) {
				recipe.blood = object.get("blood").getAsBoolean();
			}
			if (object.has("display_meta")) {
				recipe.displayMeta = object.get("display_meta").getAsInt();
			}
			if (object.has("note")) {
				recipe.note = object.get("note").getAsString();
			}

			return recipe;
		}

		@Override
		public JsonElement serialize(@NotNull RecipeEntrySaw recipe, Type typeOfSrc, @NotNull JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			object.addProperty("type", Registries.RECIPE_TYPES.getKey(recipe.getClass()));
			object.addProperty("name", recipe.toString());
			object.add("input", context.serialize(recipe.getInput(), RecipeSymbol.class));

			JsonArray outputs = new JsonArray();
			for (ItemStack output : recipe.getOutput()) {
				outputs.add(context.serialize(output, ItemStack.class));
			}
			object.add("outputs", outputs);

			if (recipe.data != null) {
				object.addProperty("data", recipe.data);
				object.addProperty("data_mask", recipe.dataMask);
			}
			if (recipe.facing != SawFacing.ANY) {
				object.addProperty("facing", recipe.facing.name().toLowerCase(Locale.ROOT));
			}
			if (recipe.drops) {
				object.addProperty("drops", true);
			}
			if (recipe.leaves != null) {
				object.add("leaves", context.serialize(recipe.leaves, ItemStack.class));
			}
			if (recipe.sound != null) {
				object.addProperty("sound", recipe.sound);
				object.addProperty("sound_volume", recipe.soundVolume);
			}
			if (recipe.blood) {
				object.addProperty("blood", true);
			}
			if (recipe.displayMeta != 0) {
				object.addProperty("display_meta", recipe.displayMeta);
			}
			if (recipe.note != null) {
				object.addProperty("note", recipe.note);
			}

			return object;
		}
	}
}
