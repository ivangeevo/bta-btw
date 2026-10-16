package ivangeevo.btx.util.rendering;

import ivangeevo.btx.util.IExactHitPlacement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.renderer.DrawMode;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.item.IPlaceable;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Axis;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

public final class PlacementOutline {
	public static void draw(Minecraft mc, World world, RenderGlobal rg,
							TilePosc targetPos, double ox, double oy, double oz) {
		if (!(mc.objectMouseOver instanceof HitResult.Tile hit) || !hit.tilePos.equals(targetPos)) return;

		ItemStack stack = mc.thePlayer.getHeldItem();
		if (stack == null || !(stack.getItem() instanceof IPlaceable.PlaceableBlock<?> placeable)) return;
		if (!(placeable.getBlock().getLogic() instanceof PlacementBounds bounds)) return;

		Side side = hit.side;
		TilePosc pos = hit.tilePos;

		double xHit, yHit;
		if (placeable.getBlock().getLogic() instanceof IExactHitPlacement) {
			double[] p = IExactHitPlacement.encodeFromHit(hit);
			xHit = p[0];
			yHit = p[1];
		} else {
			// same formula as Minecraft.clickMouse
			yHit = hit.location.y() - pos.y();
			xHit = side.axis() == Axis.Z
				? hit.location.z() - pos.z()
				: hit.location.x() - pos.x();
		}

		// an unshifted placement replaces the target block, so nothing touches in that case
		if (placeable.shouldShiftOutOf(stack, world, mc.thePlayer, pos, side, xHit, yHit)) {
			pos = pos.add(side.direction(), new TilePos());
		}

		boolean valid = placeable.canPlaceDirectlyAtPosition(stack, world, mc.thePlayer, pos, side, xHit, yHit);
		int data = placeable.getPlacedData(stack, world, mc.thePlayer, pos, side, xHit, yHit);

		// world-space boxes, used only to compare edges
		AABBd placedWorld = bounds.placementBounds(data).translate(pos.x(), pos.y(), pos.z(), new AABBd());
		AABBdc targetWorld = world.getBlockType(targetPos).getSelectionAABB(world, targetPos);

		// camera-relative box, used for drawing
		AABBd box = new AABBd(placedWorld);
		box.translate(-ox, -oy, -oz);
		MathHelper.aabbGrow(box, 0.002, 0.002, 0.002, box);

		GLRenderer.setColor4f(1f, valid ? 1f : 0.25f, valid ? 1f : 0.25f, 1f);
		drawEdges(box, placedWorld, targetWorld);
	}

	private static void drawEdges(AABBdc drawBox, AABBdc placedWorld, AABBdc targetWorld) {
		double[] dlo = {drawBox.minX(), drawBox.minY(), drawBox.minZ()};
		double[] dhi = {drawBox.maxX(), drawBox.maxY(), drawBox.maxZ()};
		double[] plo = {placedWorld.minX(), placedWorld.minY(), placedWorld.minZ()};
		double[] phi = {placedWorld.maxX(), placedWorld.maxY(), placedWorld.maxZ()};
		double[] tlo = {targetWorld.minX(), targetWorld.minY(), targetWorld.minZ()};
		double[] thi = {targetWorld.maxX(), targetWorld.maxY(), targetWorld.maxZ()};

		TessellatorGeneral t = GLRenderer.getTessellator();
		t.startDrawing(DrawMode.LINES);

		for (int e = 0; e < 3; e++) {              // axis the edge runs along
			int a = (e + 1) % 3, c = (e + 2) % 3;  // the two other axes
			for (int ia = 0; ia < 2; ia++) {
				for (int ic = 0; ic < 2; ic++) {
					double va = ia == 0 ? plo[a] : phi[a];
					double vc = ic == 0 ? plo[c] : phi[c];

					boolean onTargetEdge =
						(near(va, tlo[a]) || near(va, thi[a])) &&
							(near(vc, tlo[c]) || near(vc, thi[c])) &&
							plo[e] >= tlo[e] - EPS && phi[e] <= thi[e] + EPS;
					if (onTargetEdge) continue;

					double[] p0 = new double[3];
					double[] p1 = new double[3];
					p0[a] = p1[a] = ia == 0 ? dlo[a] : dhi[a];
					p0[c] = p1[c] = ic == 0 ? dlo[c] : dhi[c];
					p0[e] = dlo[e];
					p1[e] = dhi[e];

					t.addVertex(p0[0], p0[1], p0[2]);
					t.addVertex(p1[0], p1[1], p1[2]);
				}
			}
		}
		t.draw();
	}

	private static final double EPS = 0.01;

	private static boolean near(double x, double y) {
		return Math.abs(x - y) < EPS;
	}
}
