package xyz.trivaxy.datamancer.mixin;

import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.ServerFunctionManager;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.trivaxy.datamancer.command.entry.PopFunctionWatchEntry;
import xyz.trivaxy.datamancer.profile.FunctionWatcher;

import java.util.Deque;

@Debug(export = true)
@Mixin(ServerFunctionManager.ExecutionContext.class)
public class ExecutionContextMixin {

    @Shadow @Final public Deque<ServerFunctionManager.QueuedCommand> commandQueue;

    @Inject(method = "runTopCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/CommandFunction;getEntries()[Lnet/minecraft/commands/CommandFunction$Entry;", shift = At.Shift.AFTER))
    private void beforeRootFunctionExecute(CommandFunction function, CommandSourceStack source, CallbackInfoReturnable<Integer> cir) {
        FunctionWatcher watcher = FunctionWatcher.getInstance();
        if (!watcher.isEnabled())
            return;

        watcher.pushWatch(function);
        commandQueue.push(new ServerFunctionManager.QueuedCommand(source, -1, new PopFunctionWatchEntry(watcher)));
    }
}
