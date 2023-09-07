package xyz.trivaxy.datamancer.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import static net.minecraft.commands.Commands.*;

public abstract class DatamancerCommand {

    public static final Component PREFIX = Component
            .literal("[Datamancer] ")
            .withStyle(style -> style.withColor(TextColor.fromRgb(2804458)));

    public abstract void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandSelection environment);

    protected final void replySuccess(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> Component.empty().append(PREFIX).append(message), false);
    }

    protected final void replyFailure(CommandSourceStack source, Component message) {
        source.sendFailure(Component.empty().append(PREFIX).append(message));
    }

    private static final DatamancerCommand[] COMMANDS = new DatamancerCommand[] {
        new FunctionProfileCommand(),
        new RepeatCommand(),
        new MarkerGogglesCommand()
    };

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandSelection environment) {
        for (DatamancerCommand command : COMMANDS) {
            command.register(dispatcher, environment);
        }
    }
}
