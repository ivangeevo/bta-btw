package ivangeevo.btx.block.blocks;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicFenceThin;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.sound.BlockSound;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

import java.util.List;

public class BlockLogicPaneBTX extends BlockLogicFenceThin {

	public BlockLogicPaneBTX(@NotNull Block<?> block, @NotNull Material material) {
		super(block, material);
	}

	public static BlockLogicSupplier<BlockLogicPaneBTX> properties(@NotNull Material material, @NotNull BlockSound sound, int lightBlock) {
		return block -> new BlockLogicPaneBTX(
			block
				.withSound(sound)
				.withHardness(0.5F)
				.withLightBlock(lightBlock)
				.withLitInteriorSurface(true)
				.withTags(BlockTags.MINEABLE_BY_AXE),
			material
		);
	}

	public static BlockLogicSupplier<BlockLogicPaneBTX> woodProperties(int lightBlock) {
		return properties(Materials.WOOD, BlockSounds.WOOD, lightBlock);
	}

	public static BlockLogicSupplier<BlockLogicPaneBTX> wickerProperties() {
		return properties(Materials.WOOD, BlockSounds.GRASS, 4);
	}

	@Override
	public boolean canConnectTo(@NotNull WorldSource source, @NotNull TilePosc tilePos, @NotNull Side side) {
		Block<?> neighbor = source.getBlockType(tilePos.add(side.direction(), new TilePos()));

		return neighbor != null && neighbor != net.minecraft.core.block.Blocks.AIR && !neighbor.getMaterial().isLiquid();
	}

	@Override
	public @NotNull AABBdc getBoundsFromState(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		boolean north = canConnectTo(source, tilePos, Side.NORTH);
		boolean south = canConnectTo(source, tilePos, Side.SOUTH);
		boolean west = canConnectTo(source, tilePos, Side.WEST);
		boolean east = canConnectTo(source, tilePos, Side.EAST);

		if (!north && !south && !west && !east) {
			return new AABBd(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
		}

		return new AABBd(
			west ? 0.0 : 0.4375, 0.0, north ? 0.0 : 0.4375,
			east ? 1.0 : 0.5625, 1.0, south ? 1.0 : 0.5625);
	}

	@Override
	public AABBdc getCollisionAABB(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return getBoundsFromState(source, tilePos);
	}

	@Override
	public void getCollisionAABBs(@NotNull World world, @NotNull TilePosc tilePos, @NotNull AABBdc aabb, @NotNull List<AABBdc> aabbList) {
		boolean north = canConnectTo(world, tilePos, Side.NORTH);
		boolean south = canConnectTo(world, tilePos, Side.SOUTH);
		boolean west = canConnectTo(world, tilePos, Side.WEST);
		boolean east = canConnectTo(world, tilePos, Side.EAST);

		double x = tilePos.x();
		double y = tilePos.y();
		double z = tilePos.z();

		if (west) addIntersectingBoundingBox(aabb, new AABBd(0.0, 0.0, 0.4375, 0.5, 1.0, 0.5625).translate(x, y, z), aabbList);
		if (east) addIntersectingBoundingBox(aabb, new AABBd(0.5, 0.0, 0.4375, 1.0, 1.0, 0.5625).translate(x, y, z), aabbList);
		if (north) addIntersectingBoundingBox(aabb, new AABBd(0.4375, 0.0, 0.0, 0.5625, 1.0, 0.5).translate(x, y, z), aabbList);
		if (south) addIntersectingBoundingBox(aabb, new AABBd(0.4375, 0.0, 0.5, 0.5625, 1.0, 1.0).translate(x, y, z), aabbList);

		if (!north && !south && !west && !east) {
			// the cross
			addIntersectingBoundingBox(aabb, new AABBd(0.0, 0.0, 0.4375, 1.0, 1.0, 0.5625).translate(x, y, z), aabbList);
			addIntersectingBoundingBox(aabb, new AABBd(0.4375, 0.0, 0.0, 0.5625, 1.0, 1.0).translate(x, y, z), aabbList);
		}
	}
}
