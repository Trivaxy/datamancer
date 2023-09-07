package xyz.trivaxy.datamancer.command.entry;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.ServerFunctionManager;
import org.jetbrains.annotations.Nullable;
import xyz.trivaxy.datamancer.profile.FunctionWatcher;

import java.util.Deque;

public class PopFunctionWatchEntry implements CommandFunction.Entry {

    private FunctionWatcher watcher;

    public PopFunctionWatchEntry(FunctionWatcher watcher) {
        this.watcher = watcher;
    }

    @Override
    public void execute(ServerFunctionManager functionManager, CommandSourceStack source, Deque<ServerFunctionManager.QueuedCommand> queuedCommands, int commandLimit, int depth, @Nullable ServerFunctionManager.TraceCallbacks tracer) throws CommandSyntaxException {
        watcher.popWatch();
    }
}
