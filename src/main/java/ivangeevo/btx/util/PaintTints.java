package ivangeevo.btx.util;

public final class PaintTints {
	// calibrated so a paintable texture averaging 231 grey matches the painted planks texture, indexed by DyeColor ordinal (== blockMeta)
	private static final float[][] TINTS = {
		{0.869F, 0.851F, 0.759F},
		{0.931F, 0.647F, 0.311F},
		{0.821F, 0.497F, 0.700F},
		{0.615F, 0.812F, 0.955F},
		{0.992F, 0.912F, 0.470F},
		{0.667F, 0.888F, 0.485F},
		{1.000F, 0.811F, 0.776F},
		{0.492F, 0.492F, 0.492F},
		{0.665F, 0.665F, 0.665F},
		{0.392F, 0.593F, 0.593F},
		{0.605F, 0.430F, 0.723F},
		{0.385F, 0.453F, 0.688F},
		{0.542F, 0.401F, 0.242F},
		{0.475F, 0.655F, 0.352F},
		{0.866F, 0.353F, 0.290F},
		{0.301F, 0.294F, 0.276F}
	};

	public static final int COUNT = TINTS.length;

	private PaintTints() {}

	public static float[] get(int color) {
		return TINTS[color];
	}

	public static int getRGB(int color) {
		float[] tint = TINTS[color];
		return Math.round(tint[0] * 255.0F) << 16 | Math.round(tint[1] * 255.0F) << 8 | Math.round(tint[2] * 255.0F);
	}
}