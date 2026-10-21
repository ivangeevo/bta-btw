package ivangeevo.btx.block.blocks.pottery;

import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.entity.TileEntityVase;
import ivangeevo.btx.sound.BTXSoundTypes;
import ivangeevo.btx.util.utils.MiscUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.IPaintable;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.support.FullSupport;
import net.minecraft.core.block.support.ISupport;
import net.minecraft.core.block.support.PartialSupport;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemPaintBrush;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.LevelListener;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

public class BlockLogicVase extends BlockLogic implements IPaintable {
	protected BlockLogicVase(@NotNull Block<?> block) {
		super(block, Materials.GLASS);
	}

	public static BlockLogicSupplier<BlockLogicVase> properties() {
		return block -> new BlockLogicVase(applyCommon(block));
	}

	protected static Block<?> applyCommon(Block<?> block) {
		return block
			.withEntity(TileEntityVase::new)
			.withSound(BlockSounds.GLASS)
			.withHardness(0.0F);
	}

	@Override public boolean isSolidRender() {
		return false;
	}

	@Override public boolean isCubeShaped() {
		return false;
	}

	@Override
	public @NotNull AABBdc getBoundsFromState(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return new AABBd(0.1875, 0, 0.1875, 0.8125, 1, 0.8125);
	}

	@Override
	public @NotNull ISupport getSupport(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side) {
		return side == Side.TOP ? PartialSupport.INSTANCE : FullSupport.INSTANCE;
	}

	@Override
	public boolean onInteracted(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Player player, @NotNull Side side, double xHit, double yHit) {
		ItemStack held = player.getHeldItem();
		if (held == null || held.stackSize <= 0) return false;

		// the paint brush paints the vase instead of going inside it
		if (held.getItem() instanceof ItemPaintBrush) return false;

		if (world.isClientSide) return true;

		if (!(world.getTileEntity(tilePos) instanceof TileEntityVase vase) || !vase.isEmpty()) return false;

		ItemStack single = held.copy();

		single.stackSize = 1;
		vase.setContents(single);
		held.consumeItem(player);

		SoundUtils.playAtTileCenter(world, tilePos, SoundCategory.WORLD_SOUNDS, BTXSoundTypes.VASE_FILL, 0.25F,
			((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
		return true;
	}

	@Override
	public ItemStack[] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data, @Nullable TileEntity tileEntity) {
		return switch (dropCause) {
			case SILK_TOUCH, PICK_BLOCK -> new ItemStack[]{new ItemStack(this.block, 1, data)};
			default -> null;
		};
	}

	@Override
	public void dropWithCause(@NotNull World world, @NotNull EnumDropCause dropCause, @NotNull TilePosc tilePos, int data, @Nullable TileEntity tileEntity, @Nullable Player player) {
		if (dropCause != EnumDropCause.SILK_TOUCH && dropCause != EnumDropCause.PICK_BLOCK && tileEntity instanceof TileEntityVase vase) {
			checkForExplosion(world, tilePos, vase);
		}

		super.dropWithCause(world, dropCause, tilePos, data, tileEntity, player);
	}

	public void breakVase(@NotNull World world, @NotNull TilePosc tilePos) {
		world.playBlockEvent(null, tilePos, LevelListener.EVENT_BLOCK_BREAK, this.block.id());

		if (world.getTileEntity(tilePos) instanceof TileEntityVase vase) {
			checkForExplosion(world, tilePos, vase);
		}

		world.setBlockTypeNotify(tilePos, Blocks.AIR);
	}

	/**
	 * CE: a vase holding blasting oil explodes when it shatters (clear the contents, then
	 * createExplosion at the center with strength 1.5 and fire).
	 * TODO: hook up once blasting oil is ported. Returns true if an explosion happened.
	 */
	protected boolean checkForExplosion(@NotNull World world, @NotNull TilePosc tilePos, @NotNull TileEntityVase vase) {
		return false;
	}

	@Override
	public void setColor(@NotNull World world, @NotNull TilePosc tilePos, @NotNull DyeColor color) {
		if (color == DyeColor.WHITE) return;

		swapKeepingContents(world, tilePos, BTXBlocks.VASE_PAINTED, BTXBlocks.VASE_PAINTED.getLogic().toMetadata(color));
	}

	protected static void swapKeepingContents(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> newBlock, int newData) {
		ItemStack contents = world.getTileEntity(tilePos) instanceof TileEntityVase vase ? vase.takeContents() : null;

		world.setBlockTypeDataRaw(tilePos, newBlock, newData);
		world.notifyBlockChange(tilePos, newBlock);

		if (contents == null) return;

		if (world.getTileEntity(tilePos) instanceof TileEntityVase vase) {
			vase.setContents(contents);
		} else {
			MiscUtils.ejectStackWithRandomOffset(world, tilePos, contents);
		}
	}
}
