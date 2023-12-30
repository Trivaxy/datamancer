package xyz.trivaxy.datamancer.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.trivaxy.datamancer.profile.FunctionProfiler;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @WrapOperation(
       method = "runTick",
       at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;pause:Z", opcode = Opcodes.PUTFIELD)
    )
    private void onPauseStatusChange(Minecraft instance, boolean paused, Operation<Void> original) {
        original.call(instance, paused);

        FunctionProfiler profiler = FunctionProfiler.getInstance();

        if (paused && profiler.isEnabled())
            profiler.pause();
        else if (!paused && profiler.isEnabled())
            profiler.resume();
    }
}
