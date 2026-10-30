package ivangeevo.btx.item.render;

import ivangeevo.btx.entity.entities.WaterWheelEntity;
import ivangeevo.btx.item.items.ItemWaterWheel;
import ivangeevo.btx.util.PaintTints;
import net.minecraft.client.render.item.model.ItemModelStandard;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemModelWaterWheel extends ItemModelStandard {
	private final IconCoordinate paintableIcon;

	public ItemModelWaterWheel(@NotNull Item item, @NotNull String icon, @NotNull String paintableIcon) {
		super(item);
		setIcon(icon);
		this.paintableIcon = TextureRegistry.getTexture(paintableIcon);
	}

	@NotNull
	@Override
	public IconCoordinate getIcon(@Nullable Entity entity, @NotNull ItemStack itemStack) {
		return isPainted(itemStack) ? this.paintableIcon : super.getIcon(entity, itemStack);
	}

	@Override
	public int getColor(ItemStack stack) {
		return isPainted(stack) ? PaintTints.getRGB(ItemWaterWheel.colorFromMeta(stack.getMetadata())) : super.getColor(stack);
	}

	private static boolean isPainted(ItemStack stack) {
		return ItemWaterWheel.colorFromMeta(stack.getMetadata()) != WaterWheelEntity.UNPAINTED;
	}
}