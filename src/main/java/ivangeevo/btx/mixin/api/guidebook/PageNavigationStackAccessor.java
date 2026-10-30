package ivangeevo.btx.mixin.api.guidebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.guidebook.PageNavigationStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Stack;

@Environment(EnvType.CLIENT)
@Mixin(PageNavigationStack.class)
public interface PageNavigationStackAccessor {
	@Accessor("stack") Stack<Integer> btx$getStack();
	@Accessor("stackPointer") int btx$getStackPointer();
	@Accessor("stackPointer") void btx$setStackPointer(int pointer);
}
