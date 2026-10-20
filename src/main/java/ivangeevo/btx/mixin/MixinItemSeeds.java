package ivangeevo.btx.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ivangeevo.btx.block.BTXBlocks;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.ItemSeeds;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** Seeds only check for farmland below; let wheat, pumpkin and hemp seeds go into a soil planter too. */
@Mixin(ItemSeeds.class)
public abstract class MixinItemSeeds {
	@Shadow @Final private Block<?> cropsBlock;

	@WrapOperation(
		method = "onUseOnBlock",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/core/world/World;getBlockType(Lnet/minecraft/core/world/pos/TilePosc;)Lnet/minecraft/core/block/Block;")
	)
	private Block<?> btx$planterIsFarmland(World world, TilePosc pos, Operation<Block<?>> original) {
		Block<?> block = original.call(world, pos);
		boolean growsInPlanter = this.cropsBlock == Blocks.CROPS_WHEAT || this.cropsBlock == Blocks.CROPS_PUMPKIN || this.cropsBlock == BTXBlocks.HEMP;
		return block == BTXBlocks.PLANTER_SOIL && growsInPlanter ? Blocks.FARMLAND_DIRT : block;
	}
}
