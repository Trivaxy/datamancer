package xyz.trivaxy.datamancer.command.placeholder;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface PlaceholderProcessor {

    Component process(CommandSourceStack source, Placeholder.Arguments arguments) throws PlaceholderException, CommandSyntaxException;
}
