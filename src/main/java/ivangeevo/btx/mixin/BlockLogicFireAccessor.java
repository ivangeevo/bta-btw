package ivangeevo.btx.mixin;

import net.minecraft.core.block.BlockLogicFire;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockLogicFire.class)
public interface BlockLogicFireAccessor {
    @Accessor("burnChance")
    static int[] btx$getBurnChance() { throw new AssertionError(); }
}
