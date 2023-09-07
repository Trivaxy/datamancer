package xyz.trivaxy.datamancer.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.trivaxy.datamancer.profile.FunctionWatcher;

@Mixin(PauseScreen.class)
public class PauseScreenMixin {

    @Inject(method = "method_19845", at = @At("TAIL"))
    private void onUnpause(Button button, CallbackInfo ci) {
        FunctionWatcher.getInstance().resume();
    }
}
