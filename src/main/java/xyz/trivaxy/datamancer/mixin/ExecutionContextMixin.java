package xyz.trivaxy.datamancer.mixin;

import net.minecraft.commands.execution.ExecutionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.trivaxy.datamancer.profile.FunctionProfiler;

@Mixin(ExecutionContext.class)
public class ExecutionContextMixin {

    @Inject(method = "runCommandQueue", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V"))
    private void onCommandQuotaExceeded(CallbackInfo ci) {
        if (FunctionProfiler.getInstance().isEnabled())
            FunctionProfiler.getInstance().signalOverflow();
    }
}
