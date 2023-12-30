package xyz.trivaxy.datamancer.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.execution.tasks.ContinuationTask;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.trivaxy.datamancer.profile.FunctionProfiler;

import java.util.List;

@Mixin(CallFunction.class)
public abstract class CallFunctionMixin<T extends ExecutionCommandSource<T>> implements UnboundEntryAction<T> {

    @WrapOperation(method = "execute(Lnet/minecraft/commands/ExecutionCommandSource;Lnet/minecraft/commands/execution/ExecutionContext;Lnet/minecraft/commands/execution/Frame;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/execution/tasks/ContinuationTask;schedule(Lnet/minecraft/commands/execution/ExecutionContext;Lnet/minecraft/commands/execution/Frame;Ljava/util/List;Lnet/minecraft/commands/execution/tasks/ContinuationTask$TaskProvider;)V"))
    private void wrapFunctionCall(ExecutionContext<T> executionContext, Frame innerFrame, List<UnboundEntryAction<T>> list, ContinuationTask.TaskProvider<Frame, UnboundEntryAction<T>> taskProvider, Operation<Void> original, @Local(argsOnly = true) Frame outerFrame) {
        ResourceLocation functionId = ((CallFunction<T>) (Object) this).function.id();
        FunctionProfiler profiler = FunctionProfiler.getInstance();

        if (profiler.isEnabled()) {
            executionContext.queueNext(new CommandQueueEntry<>(outerFrame, (frame, context) -> profiler.pushWatch(functionId)));
            original.call(executionContext, innerFrame, list, taskProvider);
            executionContext.queueNext(new CommandQueueEntry<>(outerFrame, (frame, context) -> profiler.popWatch()));
        } else {
            original.call(executionContext, innerFrame, list, taskProvider);
        }
    }
}
