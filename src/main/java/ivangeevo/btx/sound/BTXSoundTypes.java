package ivangeevo.btx.sound;

import ivangeevo.btx.BTXMod;
import net.minecraft.core.sound.SoundTypes;

public class BTXSoundTypes {
	public static final String BELLOWS_BLOW = create("bellows.blow");
	public static final String BELLOWS_RETRACT = create("bellows.retract");
	public static final String MECHANICAL_BREAK = create("mechanical.break");
	public static final String GEARBOX_ENGAGE = create("gearbox.engage");
	public static final String GEARBOX_TOGGLE = create("gearbox.toggle");
	public static final String GEARBOX_OVERPOWERED = create("gearbox.overpowered");
	public static final String AXLE_CREAK = create("axle.creak");
	public static final String HAND_CRANK_CRANK = create("hand_crank.crank");
	public static final String MILLSTONE_GRIND = create("millstone.grind");
	public static final String MILLSTONE_IDLE = create("millstone.idle");
	public static final String MILLSTONE_NETHERRACK = create("millstone.netherrack");
	public static final String TURNTABLE_ROTATE = create("turntable.rotate");
	public static final String TURNTABLE_ADJUST = create("turntable.adjust");
	public static final String TURNTABLE_SHAPE = create("turntable.shape");
	public static final String VESSEL_TIP = create("vessel.tip");
	public static final String ITEM_COLLECT = create("item.collect");
	public static final String VASE_FILL = create("vase.fill");
	public static final String HIBACHI_IGNITE = create("hibachi.ignite");
	public static final String HIBACHI_EXTINGUISH = create("hibachi.extinguish");
	public static final String HOPPER_SOUL_CAPTURE = create("hopper.soul_capture");
	public static final String HOPPER_SOUL_OVERLOAD = create("hopper.soul_overload");
	public static final String COMPANION_CUBE_WHINE = create("companion_cube.whine");
	public static final String COMPANION_CUBE_HURT = create("companion_cube.hurt");
	public static final String WOLF_DUNG = create("wolf.dung");
	public static final String WOLF_DUNG_GROWL = create("wolf.dung.growl");
	public static final String WOLF_TURN_HOSTILE = create("wolf.turn_hostile");
	public static final String WOLF_EAT = create("wolf.eat");
	public static final String GUIDEBOOK_SWITCH_BOOK = create("guidebook.switch_book");
	public static final String SOULFORGE_RETURN_ITEMS = create("soulforge.return_items");
	public static final String SOULFORGE_CRAFT = create("soulforge.craft");
	public static final String SAW_POWER_ON = create("saw.power_on");
	public static final String SAW_POWER_OFF = create("saw.power_off");
	public static final String SAW_CUT = create("saw.cut");
	public static final String SAW_HURT = create("saw.hurt");
	public static final String SOULFORGED_ACTIVATE = create("soulforged.activate");
	public static final String SOULFORGED_ACTIVATE_IGNITE = create("soulforged.activate.ignite");
	public static final String SOULFORGED_ACTIVATE_HUM = create("soulforged.activate.hum");
	public static final String SOULFORGED_ACTIVATE_MOAN = create("soulforged.activate.moan");
	public static final String SOULFORGED_DEACTIVATE = create("soulforged.deactivate");
	public static final String SOULFORGED_DEACTIVATE_FIZZ = create("soulforged.deactivate.fizz");
	public static final String SOULFORGED_POWER_DOWN = create("soulforged.power_down");
	public static final String SOULFORGED_POWER_DOWN_FIZZ = create("soulforged.power_down.fizz");
	public static final String SOULFORGED_POWER_DOWN_MOAN = create("soulforged.power_down.moan");

	public static void init() {
		SoundTypes.loadSoundsJson("btx");
	}

	private static String create(String key) {
		return BTXMod.MOD_ID + ":" + key; // "btx:bellows.blow"
	}
}
