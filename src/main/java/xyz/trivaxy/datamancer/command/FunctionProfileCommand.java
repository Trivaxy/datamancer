package xyz.trivaxy.datamancer.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.types.Func;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import xyz.trivaxy.datamancer.Datamancer;
import xyz.trivaxy.datamancer.profile.FunctionProfiler;
import xyz.trivaxy.datamancer.profile.FunctionReport;
import xyz.trivaxy.datamancer.profile.FunctionReportColumn;

import static net.minecraft.commands.Commands.literal;

public class FunctionProfileCommand extends DatamancerCommand {

    private static final FunctionProfiler PROFILER = FunctionProfiler.getInstance();

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
        dispatcher.register(literal("fprofile")
                .requires(source -> source.hasPermission(2))
                .then(literal("start")
                        .executes(context -> {
                            if (PROFILER.isEnabled()) {
                                replyFailure(context.getSource(), Component.literal("Already watching! Use /fprofile stop to stop profiling, or /fprofile clear to erase profiling data and continue."));
                                return 0;
                            }

                            PROFILER.enable();
                            replySuccess(context.getSource(), Component.literal("Profiler started!"));
                            return 1;
                        })
                )
                .then(literal("stop")
                        .executes(context -> {
                            if (!PROFILER.isEnabled()) {
                                replyFailure(context.getSource(), Component.literal("Not watching any functions"));
                                return 0;
                            }

                            PROFILER.disable();
                            replySuccess(context.getSource(), Component.literal("Profiler stopped!"));
                            return 1;
                        })
                )
                .then(literal("clear")
                        .executes(context -> {
                            PROFILER.restart();
                            replySuccess(context.getSource(), Component.literal("Cleared profiler data"));
                            return 0;
                        })
                )
                .then(createDumpSubcommand())
        );
    }

    private LiteralArgumentBuilder<CommandSourceStack> createDumpSubcommand() {
        LiteralArgumentBuilder<CommandSourceStack> node = literal("dump");

        for (FunctionReportColumn column : FunctionReportColumn.values()) {
            node = node.then(literal(column.name().toLowerCase())
                .then(literal("ascending").executes(context -> dumpReport(context, column, true)))
                .then(literal("descending").executes(context -> dumpReport(context, column, false)))
            );
        }

        // If sorting options aren't specified, default is descending mean
        node = node.executes(context -> dumpReport(context, FunctionReportColumn.MEAN, false));

        return node;
    }

    private int dumpReport(CommandContext<CommandSourceStack> context, FunctionReportColumn sortByColumn, boolean ascending) {
        if (!PROFILER.isEnabled() || PROFILER.watchCount() == 0) {
            replyFailure(context.getSource(), Component.literal("Not watching any functions"));
            return 0;
        }

        FunctionReport report = PROFILER.getReport(sortByColumn, ascending);

        try {
            report.writeToFile();
        } catch (Exception e) {
            replyFailure(context.getSource(), Component.literal("Failed to write report to file"));
            Datamancer.logError("Could not write function report to " + FunctionReport.OUTPUT_PATH, e);
            return -1;
        }

        if (!context.getSource().isPlayer() || context.getSource().getServer().isDedicatedServer()) {
            replySuccess(context.getSource(), Component.literal("Report saved"));
            return PROFILER.watchCount();
        }

        Component fileLink = Component
            .literal(FunctionReport.OUTPUT_PATH.toString())
            .withStyle(ChatFormatting.UNDERLINE)
            .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, FunctionReport.OUTPUT_PATH.toAbsolutePath().toString())));

        replySuccessClientside(Component.literal("Report saved to ").append(fileLink));

        return PROFILER.watchCount();
    }
}
