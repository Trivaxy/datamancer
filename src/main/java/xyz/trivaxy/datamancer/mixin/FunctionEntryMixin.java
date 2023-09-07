package xyz.trivaxy.datamancer.mixin;

import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.ServerFunctionManager;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.trivaxy.datamancer.command.entry.PopFunctionWatchEntry;
import xyz.trivaxy.datamancer.profile.FunctionWatcher;

import java.util.Deque;

@Debug(export = true)
@Mixin(CommandFunction.FunctionEntry.class)
public class FunctionEntryMixin {

    @Inject(method = "method_17914", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", shift = At.Shift.AFTER))
    private static void beforeFunctionProcessed(ServerFunctionManager.TraceCallbacks traceCallbacks, int i, int j, Deque<ServerFunctionManager.QueuedCommand> deque, CommandSourceStack commandSourceStack, CommandFunction commandFunction, CallbackInfo ci) {
        FunctionWatcher watcher = FunctionWatcher.getInstance();
        if (!watcher.isEnabled())
            return;

        deque.addFirst(new ServerFunctionManager.QueuedCommand(commandSourceStack, j, new PopFunctionWatchEntry(watcher)));
        watcher.pushWatch(commandFunction);
    }
}
