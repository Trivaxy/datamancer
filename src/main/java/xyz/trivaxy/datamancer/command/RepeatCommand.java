package xyz.trivaxy.datamancer.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.server.commands.FunctionCommand;

import java.util.Collection;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class RepeatCommand extends DatamancerCommand {

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
        dispatcher.register(literal("repeat")
                .requires(source -> source.hasPermission(2))
                .then(argument("count", integer(1))
                    .then(argument("function", FunctionArgument.functions()).suggests(FunctionCommand.SUGGEST_FUNCTION)
                        .executes(context -> {
                            int count = getInteger(context, "count");
                            Collection<CommandFunction<CommandSourceStack>> functions = FunctionArgument.getFunctions(context, "function");

                            repeatFunctions(context.getSource(), count, functions);
                            return 0;
                            })
                    )
                )
        );
    }

    private static void repeatFunctions(CommandSourceStack source, int count, Collection<CommandFunction<CommandSourceStack>> functions) {
        for (int i = 0; i < count; i++)
            for (CommandFunction<CommandSourceStack> function : functions)
                source.getServer().getFunctions().execute(function, source.withSuppressedOutput().withMaximumPermission(2));
    }
}
