package xyz.trivaxy.datamancer.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import xyz.trivaxy.datamancer.Datamancer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class TrackerCommand extends DatamancerCommand {

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
        dispatcher.register(literal("tracker")
            .requires(source -> source.hasPermission(2) && source.isPlayer())
            .then(literal("add")
                .then(argument("template", StringArgumentType.greedyString())
                    .executes(context -> {
                        Datamancer.getTracker().addTrackableForPlayer(context.getSource().getPlayer(), StringArgumentType.getString(context, "template"));
                        return 1;
                    })
                )
            )
            .then(literal("clear")
                .executes(context -> {
                    Datamancer.getTracker().clearTrackablesForPlayer(context.getSource().getPlayer());
                    return 1;
                })
            )
            .then(literal("remove")
                .then(argument("index", IntegerArgumentType.integer(0))
                    .executes(context -> {
                        Datamancer.getTracker().removeTrackableAtIndexForPlayer(context.getSource().getPlayer(), IntegerArgumentType.getInteger(context, "index"));
                        return 1;
                    })
                )
            )
        );
    }
}
