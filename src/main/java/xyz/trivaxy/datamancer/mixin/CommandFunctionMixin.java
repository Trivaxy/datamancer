package xyz.trivaxy.datamancer.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.brigadier.StringReader;
import net.minecraft.commands.CommandFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.trivaxy.datamancer.command.entry.DebugEntry;

import java.util.ArrayList;
import java.util.List;

@Mixin(CommandFunction.class)
public class CommandFunctionMixin {

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "fromLines", at = @At(value = "STORE"), ordinal = 1)
    private static List<CommandFunction.Entry> shareCommandFunctionEntries(List<CommandFunction.Entry> entries, @Share("entries") LocalRef<List<CommandFunction.Entry>> entriesRef) {
        entriesRef.set(entries);
        return entries;
    }

    @WrapOperation(method = "fromLines", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;peek()C", ordinal = 0))
    private static char detectLineIfItsDebug(StringReader reader, Operation<Character> original, @Share("entries") LocalRef<ArrayList<CommandFunction.Entry>> entriesRef) {
        char peeked = original.call(reader);

        if (peeked == '#' && reader.canRead(2) && reader.peek(1) == '!')
            entriesRef.get().add(new DebugEntry(reader.getString().substring(2)));

        return peeked;
    }
}
