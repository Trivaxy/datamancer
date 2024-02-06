package xyz.trivaxy.datamancer.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.level.storage.LevelResource;
import xyz.trivaxy.datamancer.Datamancer;

import java.io.IOException;
import java.nio.file.Path;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class OpenCommand extends DatamancerCommand {

    private static final SuggestionProvider<CommandSourceStack> ALL_PACKS = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
        commandContext.getSource().getServer().getPackRepository().getAvailablePacks()
            .stream()
            .map(Pack::getId)
            .filter(s -> s.startsWith("file/"))
            .map(s -> s.substring(5))
            .map(StringArgumentType::escapeIfRequired), suggestionsBuilder
    );

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
        dispatcher.register(literal("open")
            .requires(source -> source.hasPermission(3))
            .then(argument("name", StringArgumentType.string())
                .suggests(ALL_PACKS)
                .executes(context -> {
                    if (!context.getSource().isPlayer() || context.getSource().getServer().isDedicatedServer()) {
                        replyFailure(context.getSource(), Component.literal("Command is only executable by players in singleplayer"));
                        return 0;
                    }

                    Path datapacksFolder = context.getSource().getServer().getWorldPath(LevelResource.DATAPACK_DIR);
                    Path packFolder = datapacksFolder.resolve(StringArgumentType.getString(context, "name"));

                    if (!packFolder.toFile().exists()) {
                        replyFailure(context.getSource(), Component.literal("Pack does not exist"));
                        return 0;
                    }

                    try {
                        openFolder(packFolder);

                        Component fileLink = Component
                            .literal("Opened pack successfully.")
                            .withStyle(ChatFormatting.UNDERLINE)
                            .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, packFolder.toAbsolutePath().toString())));

                        replySuccess(context.getSource(), fileLink);
                        return 1;
                    } catch (IOException e) {
                        replyFailure(context.getSource(), Component.literal("Failed to open pack folder"));
                        Datamancer.logError("Failed to open datapack \"" + packFolder + "\":" + e.getMessage());
                        return 0;
                    }
                })
            )
        );
    }

    // Because we don't want to use AWT
    private static void openFolder(Path folder) throws IOException {
        Runtime rt = Runtime.getRuntime();
        String os = System.getProperty("os.name").toLowerCase();
        String path = "\"" + folder.toAbsolutePath() + "\"";

        if (os.contains("win")) {
            rt.exec("explorer.exe " + path);
        } else if (os.contains("mac")) {
            rt.exec("open " + path);
        } else if (os.contains("nix") || os.contains("nux")) {
            rt.exec("xdg-open " + path);
        }
    }
}
