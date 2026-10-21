package ivangeevo.btx.util.rendering;

import ivangeevo.btx.item.items.AreaMiningTool;
import ivangeevo.btx.util.AreaMiningHelper;
import ivangeevo.btx.util.SoulforgedToolHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.joml.primitives.AABBd;

public final class AreaMiningOutline {
	private static final double GROW = 0.002D;

	private AreaMiningOutline() {}

	public static void draw(Minecraft mc, World world, RenderGlobal rg, TilePosc targetPos, double ox, double oy, double oz) {
		if (!(mc.objectMouseOver instanceof HitResult.Tile hit) || !hit.tilePos.equals(targetPos)) return;

		ItemStack stack = mc.thePlayer.getHeldItem();
		if (stack == null || !(stack.getItem() instanceof AreaMiningTool tool) || !AreaMiningHelper.isAreaMode(stack)) return;
		if (SoulforgedToolHelper.isBrokenTool(stack)) return;

		float centerHardness = world.getBlockType(targetPos).getHardness();
		if (centerHardness < 0.0F) return;

		for (TilePos pos : AreaMiningHelper.getExtraTargets(tool, stack, world, mc.thePlayer, centerHardness, targetPos, hit.side)) {
			AABBd box = new AABBd();
			MathHelper.aabbGrow(world.getBlockType(pos).getSelectionAABB(world, pos), GROW, GROW, GROW, box);
			box.translate(-ox, -oy, -oz);
			rg.drawOutlinedBoundingBox(box);
		}
	}
}