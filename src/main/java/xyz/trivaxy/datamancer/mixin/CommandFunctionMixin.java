package xyz.trivaxy.datamancer.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.StringReader;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.FunctionBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.trivaxy.datamancer.command.entry.DebugEntry;

@Mixin(CommandFunction.class)
public interface CommandFunctionMixin {
    @WrapOperation(method = "fromLines", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;peek()C", ordinal = 0))
    private static char detectLineIfItsDebug(StringReader reader, Operation<Character> original, @Local FunctionBuilder<?> builder) {
        char peeked = original.call(reader);

        if (peeked == '#' && reader.canRead(2) && reader.peek(1) == '!')
            builder.plainEntries.add(new DebugEntry<>(reader.getString().substring(2)));

        return peeked;
    }
}
