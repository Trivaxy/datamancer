package xyz.trivaxy.datamancer.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import xyz.trivaxy.datamancer.watch.DataPackWatcher;
import xyz.trivaxy.datamancer.watch.WatcherStateComponent;

import java.util.Collection;

public class WatchCommand extends DatamancerCommand {

    private static final SuggestionProvider<CommandSourceStack> SELECTED_PACKS = (commandContext, suggestionsBuilder) ->
            SharedSuggestionProvider.suggest(
                    commandContext.getSource().getServer().getPackRepository().getAvailablePacks()
                                  .stream()
                                  .filter(pack -> pack.getPackSource() == PackSource.WORLD)
                                  .map(Pack::getId)
                                  .map(StringArgumentType::escapeIfRequired), suggestionsBuilder);

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("watch")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("add")
                        .then(Commands.argument("pack", StringArgumentType.string())
                                .suggests(SELECTED_PACKS)
                                .executes(context -> {
                                    String packName = StringArgumentType.getString(context, "pack");
                                    WatcherStateComponent watcher = getWatcher(context.getSource());
                                    Pack pack = context.getSource().getServer().getPackRepository().getPack(packName);

                                    if (pack == null) {
                                        replyFailure(context.getSource(), Component.literal("Unknown pack: ").append(Component.literal(packName)));
                                        return 0;
                                    }

                                    Component packLink = pack.getChatLink(false);

                                    if (watcher.isWatching(packName)) {
                                        replyFailure(context.getSource(), Component.literal("Already watching pack ").append(packLink));
                                        return 0;
                                    }

                                    watcher.watchPack(packName);
                                    replySuccess(context.getSource(), Component.literal("Started watching pack ").append(packLink));

                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("pack", StringArgumentType.string())
                                .suggests(SELECTED_PACKS)
                                .executes(context -> {
                                    String packName = StringArgumentType.getString(context, "pack");
                                    WatcherStateComponent watcher = getWatcher(context.getSource());
                                    Pack pack = context.getSource().getServer().getPackRepository().getPack(packName);

                                    if (pack == null) {
                                        replyFailure(context.getSource(), Component.literal("Unknown pack: ").append(Component.literal(packName)));
                                        return 0;
                                    }

                                    Component packLink = pack.getChatLink(false);

                                    if (!watcher.isWatching(packName)) {
                                        replyFailure(context.getSource(), Component.literal("Not watching pack ").append(packLink));
                                        return 0;
                                    }

                                    watcher.unwatchPack(packName);
                                    replySuccess(context.getSource(), Component.literal("Stopped watching pack ").append(packLink));

                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("start")
                        .executes(context -> {
                            WatcherStateComponent watcher = getWatcher(context.getSource());

                            if (watcher.isActive()) {
                                replyFailure(context.getSource(), Component.literal("Watcher already active"));
                                return 0;
                            }

                            watcher.start(context.getSource().getServer());
                            replySuccess(context.getSource(), Component.literal("Started watcher"));

                            return 1;
                        })
                )
                .then(Commands.literal("stop")
                        .executes(context -> {
                            WatcherStateComponent watcher = getWatcher(context.getSource());

                            if (!watcher.isActive()) {
                                replyFailure(context.getSource(), Component.literal("Watcher not active"));
                                return 0;
                            }

                            watcher.shutdown();
                            replySuccess(context.getSource(), Component.literal("Stopped watcher"));
                            return 1;
                        })
                )
                .then(Commands.literal("list")
                        .executes(context -> {
                            WatcherStateComponent watcher = getWatcher(context.getSource());
                            Collection<String> watchList = watcher.getWatchList();

                            if (watchList.isEmpty()) {
                                replySuccess(context.getSource(), Component.literal("Not watching any packs"));
                                return 1;
                            }

                            replySuccess(context.getSource(),
                                    Component.literal("Currently watching ")
                                             .append(String.valueOf(watchList.size()))
                                             .append(" packs: ")
                                             .append(
                                                     ComponentUtils.formatList(watchList, packId ->
                                                             getPackLink(context.getSource().getServer().getPackRepository(), packId)
                                                     )
                                             )
                            );
                            return 1;
                        })
                )
        );
    }

    private static Component getPackLink(PackRepository repository, String packId) {
        Pack pack = repository.getPack(packId);

        if (pack == null)
            return null;

        return pack.getChatLink(true);
    }

    private static WatcherStateComponent getWatcher(CommandSourceStack source) {
        return DataPackWatcher.KEY.get(source.getLevel().getLevelData());
    }


}
