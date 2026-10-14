package ivangeevo.btx.entity.entities;

import com.mojang.nbt.tags.CompoundTag;
import ivangeevo.btx.BTXGlobals;
import ivangeevo.btx.block.BTXBlocks;
import ivangeevo.btx.block.blocks.BlockLogicAxle;
import ivangeevo.btx.item.BTXItems;
import ivangeevo.btx.util.MechPowerUtils;
import net.minecraft.core.block.Block;
import net.minecraft.core.data.gamerule.GameRules;
import net.minecraft.core.entity.EntityLightning;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biomes;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import net.minecraft.core.world.weather.Weathers;
import org.jetbrains.annotations.NotNull;

public class WindMillEntity extends MechanicalPowerSourceEntityHorizonal {

	public static final float HEIGHT = 12.8F;
	public static final float WIDTH = 12.8F;
	public static final float DEPTH = 0.8F;

	private static final int MAX_DAMAGE = 160;

	private static final float ROTATION_PER_TICK = -0.12F;
	private static final float ROTATION_PER_TICK_IN_STORM = -2.0F;
	private static final float ROTATION_PER_TICK_IN_HELL = -0.07F;

	private static final int TICKS_PER_FULL_UPDATE = 20;
	private static final int UPDATES_TO_OVERPOWER = 30; // 30 seconds in a storm will overpower the mill

	private static final int DATA_BLADE_COLOR_0_ID = 23;
	private static final int DATA_BLADE_COLOR_1_ID = 24;
	private static final int DATA_BLADE_COLOR_2_ID = 25;
	private static final int DATA_BLADE_COLOR_3_ID = 26;

	private int currentBladeColoringIndex;

	protected int overpowerTimer;

	public WindMillEntity(@NotNull World world) {
		super(world);
		currentBladeColoringIndex = 0;
	}

	public WindMillEntity(World world, double x, double y, double z, boolean alignedToX) {
		super(world, x, y, z, alignedToX);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(DATA_BLADE_COLOR_0_ID,0, Integer.class);
		entityData.define(DATA_BLADE_COLOR_1_ID, 0, Integer.class);
		entityData.define(DATA_BLADE_COLOR_2_ID, 0, Integer.class);
		entityData.define(DATA_BLADE_COLOR_3_ID, 0, Integer.class);
	}

	public int getBladeColor(int bladeIndex) {
		return entityData.getInt(DATA_BLADE_COLOR_0_ID + bladeIndex);
	}

	public void setBladeColor(int bladeIndex, int color) {
		entityData.set(DATA_BLADE_COLOR_0_ID + bladeIndex, color);
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag tag) {
		alignedToX = tag.getBoolean("AlignedToX");
		rotation = tag.getFloat("MillRotation");
		providingPower = tag.getBoolean("ProvidingPower");
		overpowerTimer = tag.getInteger("OverpowerTimer");
		setBladeColor(0, tag.getInteger("BladeColor0"));
		setBladeColor(1, tag.getInteger("BladeColor1"));
		setBladeColor(2, tag.getInteger("BladeColor2"));
		setBladeColor(3, tag.getInteger("BladeColor3"));
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag tag) {
		tag.putBoolean("AlignedToX", alignedToX);
		tag.putFloat("MillRotation", rotation);
		tag.putBoolean("ProvidingPower", providingPower);
		tag.putInt("OverpowerTimer", overpowerTimer);
		for (int i = 0; i < 4; i++) {
			tag.putInt("BladeColor" + i, getBladeColor(i));
		}
	}

	@Override
	public boolean interact(@NotNull Player player) {
		ItemStack heldStack = player.getHeldItem();

		if (heldStack == null ) return super.interact(player);

		boolean isDye = heldStack.itemID == Items.DYE.id;
		boolean isDung = heldStack.itemID == BTXItems.DUNG.id ;

		if (!isDung && !isDye) return super.interact(player);

		if (!world.isClientSide) {
			// brown smear for dung
			int color = isDung ? 12 : DyeColor.colorFromItemMeta(heldStack.getMetadata()).ordinal();
			setBladeColor(currentBladeColoringIndex, color);
			currentBladeColoringIndex = (currentBladeColoringIndex + 1) % 4;
		}

		heldStack.consumeItem(player);

		return true;
	}

	@Override
	public void thunderHit(EntityLightning bolt) {
		if (this.world.getGameRuleValue(GameRules.DO_FIRE_BURN)) {
			this.remove();
		}
	}

	@Override
	public float getWidth() {
		return WIDTH;
	}

	@Override
	public float getHeight() {
		return HEIGHT;
	}

	@Override
	public float getDepth() {
		return DEPTH;
	}

	@Override
	public int getMaxDamage() {
		return MAX_DAMAGE;
	}

	@Override
	public int getTicksPerFullUpdate() {
		return TICKS_PER_FULL_UPDATE;
	}

	@Override
	protected void onFullUpdateServer() {
		super.onFullUpdateServer();

		// update overpower
		if (overpowerTimer >= 0) {
			if (overpowerTimer > 0) {
				overpowerTimer--;
			}

			if (overpowerTimer <= 0) {
				int centerX = MathHelper.floor(x);
				int centerY = MathHelper.floor(y);
				int centerZ = MathHelper.floor(z);

				TilePos tilePos = getCenterPos();

				// destroy any gearboxes attached to the windmill by overpowering the axle
				if (BTXBlocks.AXLE.getLogic() instanceof BlockLogicAxle axle) {
					axle.overpower(world, tilePos);
				}
			}
		}

	}

	@Override
	public void destroyWithDrop() {
		if (!world.isClientSide && !removed) {
			this.dropItem(BTXItems.WINDMILL.id, 1, 0.0F);
			this.remove();
		}
	}

	@Override
	public boolean validateAreaAroundDevice() {
		int centerX = MathHelper.floor(this.x);
		int centerY = MathHelper.floor(this.y);
		int centerZ = MathHelper.floor(this.z);

		return windMillValidateAreaAroundBlock(world, centerX, centerY, centerZ, alignedToX);
	}

	@Override
	public float computeRotation() {
		int centerX = MathHelper.floor(this.x);
		int centerY = MathHelper.floor(this.y);
		int centerZ = MathHelper.floor(this.z);

		float rotationAmount = 0.0F;

		TilePos centerTilePos = new TilePos(centerX, centerY, centerZ);

		if (world.canBlockSeeSky(centerTilePos)) {
			if (world.getCurrentWeather() == Weathers.OVERWORLD_STORM && !overpowerSafeBiome(world, centerX, centerY, centerZ)) {
				rotationAmount = ROTATION_PER_TICK_IN_STORM;
				if (overpowerTimer < 0) {
					overpowerTimer = UPDATES_TO_OVERPOWER;
				}
			} else {
				rotationAmount = ROTATION_PER_TICK;
				overpowerTimer = -1;
			}
		} else {
			overpowerTimer = -1;
		}

		return rotationAmount;
	}

	private boolean overpowerSafeBiome(World world, int x, int y, int z) {
		return world.getBiomeProvider().getBiome(x, y, z) == Biomes.OVERWORLD_DESERT;
	}

	@Override
	protected void onClientRotationOctantChange() {
		//float speed = getRotationSpeed();
		float speed = getDisplayedRotationSpeed();

		if (speed < ROTATION_PER_TICK) {
			TilePos targetPos = new TilePos(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));

			Block<?> targetBlock = world.getBlockType(targetPos);

			if (targetBlock == BTXBlocks.AXLE_POWER_SOURCE && targetBlock.getLogic() instanceof BlockLogicAxle axle) {
				int axleAlignment = axle.getAxisAlignment(world, targetPos);

				clientNotifyGearboxOfOverpoweredOctantChangeInDirection(targetPos, axleAlignment << 1);
				clientNotifyGearboxOfOverpoweredOctantChangeInDirection(targetPos, (axleAlignment << 1 ) + 1);
			}
		}
	}

	protected void clientNotifyGearboxOfOverpoweredOctantChangeInDirection(TilePosc tilePos, int facing) {
		TilePos tempPos = new TilePos(tilePos);

		// just to prevent running on indefinitely
		for (int tempCount = 0; tempCount < 10; tempCount++) {
			tempPos.add(Direction.fromId(facing));

			Block<?> block = world.getBlockType(tempPos);

			if (MechPowerUtils.isBlockAxle(block) && block.getLogic() instanceof BlockLogicAxle axle) {
				if (axle.isAxleOrientedTowardsFacing(world, tempPos, facing)) {
					continue;
				}
			} else if (MechPowerUtils.isPoweredGearBox(world, tempPos)) {
				// TODO:  play sound
			}

			break;
		}
	}

	public static boolean windMillValidateAreaAroundBlock(World world, int x, int y, int z, boolean alignedX) {
		int xOffset;
		int zOffset;

		if (alignedX) {
			xOffset = 0;
			zOffset = 1;
		} else {
			xOffset = 1;
			zOffset = 0;
		}

		for (int heightOffset = -6; heightOffset <= 6; heightOffset++) {
			for (int widthOffset = -6; widthOffset <= 6; widthOffset++) {
				if (heightOffset != 0 || widthOffset != 0) {
					int tempX = x + (xOffset * widthOffset);
					int tempY = y + heightOffset;
					int tempZ = z + (zOffset * widthOffset);

					if (!isValidBlockForWindMillToOccupy(world, tempX, tempY, tempZ)) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public static boolean isValidBlockForWindMillToOccupy(World world, int x, int y, int z) {
		return world.isAirBlock(new TilePos(x, y, z));
	}

}
