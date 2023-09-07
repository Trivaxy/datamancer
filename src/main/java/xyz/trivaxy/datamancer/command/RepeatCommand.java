package xyz.trivaxy.datamancer.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.FunctionArgument;
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
                            Collection<CommandFunction> functions = FunctionArgument.getFunctions(context, "function");

                            return repeatFunctions(context.getSource(), count, functions);
                            })
                    )
                )
        );
    }

    private static int repeatFunctions(CommandSourceStack source, int count, Collection<CommandFunction> functions) {
        int sum = 0;

        for (int i = 0; i < count; i++)
            for (CommandFunction function : functions)
                sum += source.getServer().getFunctions().execute(function, source.withSuppressedOutput().withMaximumPermission(2));

        return sum;
    }
}
