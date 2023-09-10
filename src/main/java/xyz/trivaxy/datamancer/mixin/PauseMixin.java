package xyz.trivaxy.datamancer.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.trivaxy.datamancer.profile.FunctionProfiler;

@Mixin(Minecraft.class)
public class PauseMixin {

    @Inject(method = "pauseGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;pause()V", shift = At.Shift.AFTER))
    private void onPause(boolean pauseOnly, CallbackInfo ci) {
        FunctionProfiler.getInstance().pause();
    }
}
