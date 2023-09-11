package xyz.trivaxy.datamancer.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import xyz.trivaxy.datamancer.watch.DataPackWatcher;

public class WatchCommand extends DatamancerCommand {

    private static final SuggestionProvider<CommandSourceStack> SELECTED_PACKS = (commandContext, suggestionsBuilder) ->
            SharedSuggestionProvider.suggest(commandContext.getSource().getServer().getPackRepository().getAvailableIds().stream().filter(s -> !s.equals("vanilla") && !s.equals("fabric")).map(StringArgumentType::escapeIfRequired), suggestionsBuilder);

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("watch")
                .then(Commands.literal("add")
                        .then(Commands.argument("pack", StringArgumentType.string())
                                .suggests(SELECTED_PACKS)
                                .executes(context -> {
                                    String packName = StringArgumentType.getString(context, "pack");
                                    DataPackWatcher.getInstance().watchPack(packName);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("pack", StringArgumentType.string())
                                .suggests(SELECTED_PACKS)
                                .executes(context -> {
                                    String packName = StringArgumentType.getString(context, "pack");
                                    DataPackWatcher.getInstance().unwatchPack(packName);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("start")
                        .executes(context -> {
                            if (DataPackWatcher.getInstance().isStarted()) {
                                replyFailure(context.getSource(), Component.literal("Watcher already active"));
                                return 0;
                            }

                            DataPackWatcher.getInstance().start(context.getSource().getServer());
                            replySuccess(context.getSource(), Component.literal("Started watcher"));

                            return 1;
                        })
                )
                .then(Commands.literal("stop")
                        .executes(context -> {
                            if (!DataPackWatcher.getInstance().isStarted()) {
                                replyFailure(context.getSource(), Component.literal("Watcher not active"));
                                return 0;
                            }

                            DataPackWatcher.getInstance().stop();
                            replySuccess(context.getSource(), Component.literal("Stopped watcher"));
                            return 1;
                        })
                )
        );
    }
}
