package ivangeevo.btx.block.entity;

import com.mojang.nbt.tags.CompoundTag;
import ivangeevo.btx.block.blocks.turntable.BlockLogicTurntable;
import ivangeevo.btx.block.blocks.turntable.TurntableAttachable;
import ivangeevo.btx.block.blocks.turntable.TurntableAttachments;
import ivangeevo.btx.block.blocks.turntable.TurntableInteraction;
import ivangeevo.btx.recipe.BTXRecipes;
import ivangeevo.btx.recipe.turntable.TurntableCrafting;
import ivangeevo.btx.util.utils.MiscUtils;
import ivangeevo.btx.util.utils.SoundUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public class TileEntityTurntable extends TileEntity {
	private static final int MAX_HEIGHT_OF_BLOCKS_ROTATED = 2;
	private static final int[] TICKS_TO_ROTATE = {10, 20, 40, 80, 200, 600, 1200, 2400, 6000, 12000, 24000};

	private int rotationTickCount = 0;
	private int craftingRotationCount = 0;
	private int switchOverride = -1; // legacy m_iSwitchSetting, applied to block data on the first server tick

	@Override
	public void readAdditionalData(@NotNull CompoundTag tag) {
		this.rotationTickCount = tag.getInteger("m_iRotationCount");
		if (tag.containsKey("m_iSwitchSetting")) {
			this.switchOverride = Math.min(tag.getInteger("m_iSwitchSetting"), 3);
		}
		if (tag.containsKey("m_iPotteryRotationCount")) {
			this.craftingRotationCount = tag.getInteger("m_iPotteryRotationCount");
		}
	}

	@Override
	public void writeAdditionalData(@NotNull CompoundTag tag) {
		tag.putInt("m_iRotationCount", this.rotationTickCount);
		tag.putInt("m_iPotteryRotationCount", this.craftingRotationCount);
		// m_iSwitchSetting is no longer written: it lives in the block data
	}

	@Override
	public void tick() {
		if (this.worldObj == null || this.worldObj.isClientSide) return;

		Block<?> block = this.worldObj.getBlockType(tilePos);

		if (!(block.getLogic() instanceof BlockLogicTurntable turntable)) return;

		if (switchOverride >= 0) { // legacy world data
			turntable.setSwitchSetting(worldObj, tilePos, switchOverride);
			switchOverride = -1;
		}

		if (!turntable.isBlockMechanicalOn(worldObj, tilePos)) {
			rotationTickCount = 0;
			return;
		}

		// don't rotate if the surroundings aren't loaded
		if (!worldObj.areBlocksLoaded(tilePos, 9)) return;

		if (++rotationTickCount >= getTicksToRotate(turntable)) {
			rotationTickCount = 0;
			rotateTurntable(turntable);
		}
	}

	private boolean hasCraftableBlockAbove() {
		for (int y = tilePos.y + 1; y <= tilePos.y + MAX_HEIGHT_OF_BLOCKS_ROTATED; y++) {
			if (BTXRecipes.TURNTABLE.findRecipe(worldObj, new TilePos(tilePos.x, y, tilePos.z)) != null) {
				return true;
			}
		}
		return false;
	}

	private int getTicksToRotate(BlockLogicTurntable turntable) {
		return TICKS_TO_ROTATE[turntable.getSwitchSetting(worldObj, tilePos)];
	}

	private void rotateTurntable(BlockLogicTurntable turntable) {
		SoundUtils.playAtTileCenter(worldObj, tilePos, SoundCategory.WORLD_SOUNDS, "random.click", 0.05F, 1.0F);

		boolean reverse = turntable.isBlockRedstoneOn(worldObj, tilePos);
		int craftingCounter = this.craftingRotationCount;

		for (int y = tilePos.y + 1; y <= tilePos.y + MAX_HEIGHT_OF_BLOCKS_ROTATED; y++) {
			TilePos target = new TilePos(tilePos.x, y, tilePos.z);

			TurntableInteraction ti = worldObj.getBlockType(target).getLogic() instanceof TurntableInteraction t ? t : null;
			boolean craftable = BTXRecipes.TURNTABLE.findRecipe(worldObj, target) != null;

			boolean transmitHorizontally;
			boolean transmitVertically;

			if (ti != null && ti.canRotateOnTurntable(worldObj, target)) {
				// read capacity first, as rotation may change it
				transmitHorizontally = ti.canTransmitRotationHorizontally(worldObj, target);
				transmitVertically = ti.canTransmitRotationVertically(worldObj, target);
				craftingCounter = ti.rotateOnTurntable(worldObj, target, reverse, craftingCounter);
			} else if (ti == null && worldObj.isBlockNormalCube(target)) {
				// BTW fallback: solid blocks don't rotate, but they carry the rotation up and sideways
				transmitHorizontally = true;
				transmitVertically = true;
			} else if (craftable) {
				// craftable but not rotatable (e.g. a non-cube pottery block): it can still be crafted, but carries nothing
				transmitHorizontally = false;
				transmitVertically = false;
			} else {
				break;
			}

			// crafting runs after the rotation, so the recipe sees the block's current data.
			// It can replace the block, which is why the transmit flags were read above.
			craftingCounter = TurntableCrafting.onRotated(worldObj, target, craftingCounter);

			if (transmitHorizontally) {
				rotateBlocksAttachedToBlock(target, reverse);
			}
			if (!transmitVertically) {
				break;
			}
		}

		// the counter only accumulates while something keeps counting it
		if (craftingCounter > craftingRotationCount) {
			craftingRotationCount = craftingCounter;
		}
		else {
			craftingRotationCount = 0;
		}

		this.setChanged();

		// notify neighbours so buddy-style blocks can pick up on the change
		worldObj.notifyBlockChange(tilePos, turntable.block);
	}

	private void rotateBlocksAttachedToBlock(TilePosc center, boolean reverse) {
		Block<?>[] newBlocks = new Block<?>[4];
		int[] newData = new int[4];

		// pass 1: pick up every attached block
		for (Direction side : Direction.horizontal) {
			TilePos pos = new TilePos(center);
			pos.add(side);

			Block<?> block = worldObj.getBlockType(pos);
			if (block == Blocks.AIR) continue;

			TurntableAttachable attachable = TurntableAttachments.find(block);
			if (attachable == null) continue;

			Direction toColumn = side.opposite(); // the side of the moving block that touches the column
			if (!attachable.canRotateAroundBlockOnTurntableToFacing(worldObj, pos, toColumn)) continue;
			if (!attachable.onRotatedAroundBlockOnTurntableToFacing(worldObj, pos, toColumn)) continue;

			Direction dest = TurntableAttachments.rotateFacingAroundY(side, reverse);
			int slot = TurntableAttachments.horizontalIndex(dest);

			newBlocks[slot] = block;
			newData[slot] = attachable.getNewMetadataRotatedAroundBlockOnTurntableToFacing(
				worldObj, pos, toColumn, dest.opposite());

			worldObj.setBlockTypeNotify(pos, Blocks.AIR);
		}

		// pass 2: put them down
		for (int i = 0; i < 4; i++) {
			Block<?> block = newBlocks[i];
			if (block == null) continue;

			Direction side = Direction.horizontal[i];
			TilePos target = new TilePos(center);
			target.add(side);

			if (isReplaceable(target)) {
				worldObj.setBlockTypeDataNotify(target, block, newData[i]);
			} else {
				// target occupied: eject as an item at the old location
				Direction oldSide = TurntableAttachments.rotateFacingAroundY(side, !reverse);
				TilePos oldPos = new TilePos(center);
				oldPos.add(oldSide);

				ItemStack[] drops = block.getLogic().getBreakResult(worldObj, EnumDropCause.PROPER_TOOL, newData[i], null);
				if (drops != null) {
					for (ItemStack stack : drops) {
						MiscUtils.ejectStackWithRandomOffset(worldObj, oldPos, stack);
					}
				}
			}
		}
	}

	private boolean isReplaceable(TilePosc pos) {
		// ASSUMPTION: BTA exposes replaceability on the material. Replace if your port has a helper (BTW: WorldUtils.isReplaceableBlock).
		return worldObj.isAirBlock(pos) || worldObj.getBlockType(pos).getMaterial().isReplaceable();
	}
}
