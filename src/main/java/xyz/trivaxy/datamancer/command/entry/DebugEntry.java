package xyz.trivaxy.datamancer.command.entry;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;
import xyz.trivaxy.datamancer.Datamancer;
import xyz.trivaxy.datamancer.command.placeholder.PlaceholderException;
import xyz.trivaxy.datamancer.command.placeholder.Placeholder;

import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DebugEntry implements CommandFunction.Entry {

    private final String template;
    private static final Pattern PLACEHOLDER_REGEX_PATTERN = Pattern.compile("\\{([A-Za-z_]+)(\\h+([^{}]+))*}");

    public DebugEntry(String template) {
        this.template = template;
    }

    @Override
    public void execute(ServerFunctionManager serverFunctionManager, CommandSourceStack commandSourceStack, Deque<ServerFunctionManager.QueuedCommand> deque, int i, int j, @Nullable ServerFunctionManager.TraceCallbacks traceCallbacks) throws CommandSyntaxException {
        try {
            Component result = processTemplate(commandSourceStack, template);
            PlayerList players = commandSourceStack.getServer().getPlayerList();

            for (ServerPlayer player : players.getPlayers()) {
                if (player.hasPermissions(2))
                    player.sendSystemMessage(result);
            }
        } catch (Exception e) {
            Datamancer.logError("Could not expand placeholder: " + e.getMessage());
        }
    }

    private static Component processTemplate(CommandSourceStack source, String template) throws PlaceholderException, CommandSyntaxException {
        Matcher matcher = PLACEHOLDER_REGEX_PATTERN.matcher(template);
        MutableComponent processedTemplate = Component.empty();

        int lastEnd = 0;

        while (matcher.find()) {
            processedTemplate.append(template.substring(lastEnd, matcher.start()));
            lastEnd = matcher.end();

            String processorType = matcher.group(1);
            String arguments = matcher.group(3);

            if (!Placeholder.PLACEHOLDERS.containsKey(processorType))
                throw new PlaceholderException("Unknown placeholder type: " + processorType);

            Component replacement = Placeholder.PLACEHOLDERS.get(processorType).expand(source, arguments);
            processedTemplate.append(replacement);
        }

        if (lastEnd < template.length())
            processedTemplate.append(template.substring(lastEnd));

        return processedTemplate;
    }
}
