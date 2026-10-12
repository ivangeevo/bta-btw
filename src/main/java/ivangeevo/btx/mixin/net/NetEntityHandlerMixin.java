package ivangeevo.btx.mixin.net;

import com.llamalad7.mixinextras.sugar.Local;
import ivangeevo.btx.networking.IOversizedEntry;
import net.minecraft.core.net.entity.INetworkEntry;
import net.minecraft.core.net.entity.ITrackedEntry;
import net.minecraft.core.net.entity.NetEntityHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(NetEntityHandler.class)
public abstract class NetEntityHandlerMixin {

	@Shadow @Final private static List<ITrackedEntry<?>> trackedEntries;

	@Inject(method = "addEntryRaw",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/core/net/entity/NetEntityHandler;isListDirty:Z",
			opcode = Opcodes.PUTSTATIC
		)
	)
	private static <T> void addOversizedEntryCheck(INetworkEntry<T> entry, int type, CallbackInfo ci, @Local Class<? extends T> entityClass) {
		if (entry instanceof IOversizedEntry<T> entry1) {
			trackedEntries.add(entry1);
		}
	}

}
