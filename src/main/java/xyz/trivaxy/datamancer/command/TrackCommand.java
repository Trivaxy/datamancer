package xyz.trivaxy.datamancer.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.*;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import xyz.trivaxy.datamancer.Datamancer;
import xyz.trivaxy.datamancer.tracker.TrackedBlockEntity;
import xyz.trivaxy.datamancer.tracker.TrackedEntity;
import xyz.trivaxy.datamancer.tracker.TrackedScore;
import xyz.trivaxy.datamancer.tracker.TrackedStorageEntry;

import java.util.Collection;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class TrackCommand extends DatamancerCommand {

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
        dispatcher.register(literal("tracker")
            .requires(source -> source.hasPermission(2) && source.isPlayer())
            .then(literal("add")
                .then(literal("block")
                    .then(argument("block_pos", BlockPosArgument.blockPos())
                        .then(argument("path", NbtPathArgument.nbtPath())
                            .executes(context -> trackBlockEntityForPlayer(context, BlockPosArgument.getBlockPos(context, "block_pos"), NbtPathArgument.getPath(context, "path")))
                        )
                        .executes(context -> trackBlockEntityForPlayer(context, BlockPosArgument.getBlockPos(context, "block_pos"), null))
                    )
                )
                .then(literal("entity")
                    .then(argument("entities", EntityArgument.entities())
                        .then(argument("path", NbtPathArgument.nbtPath())
                            .executes(context -> trackEntitiesForPlayer(context, EntityArgument.getEntities(context, "entities"), NbtPathArgument.getPath(context, "path")))
                        )
                        .executes(context -> trackEntitiesForPlayer(context, EntityArgument.getEntities(context, "entities"), null))
                    )
                )
                .then(literal("score")
                    .then(argument("targets", ScoreHolderArgument.scoreHolders())
                        .then(argument("objective", ObjectiveArgument.objective())
                            .executes(context -> trackScoresForPlayer(context, ScoreHolderArgument.getNames(context, "targets"), ObjectiveArgument.getObjective(context, "objective")))
                        )
                    )
                )
                .then(literal("storage")
                    .then(argument("target", ResourceLocationArgument.id())
                        .then(argument("path", NbtPathArgument.nbtPath())
                            .executes(context -> trackStorageEntryForPlayer(context, ResourceLocationArgument.getId(context, "target"), NbtPathArgument.getPath(context, "path")))
                        )
                        .executes(context -> trackStorageEntryForPlayer(context, ResourceLocationArgument.getId(context, "target"), null))
                    )
                )
            )
        );
    }

    private int trackBlockEntityForPlayer(CommandContext<CommandSourceStack> context, BlockPos pos, NbtPathArgument.NbtPath path) {
        Datamancer.getTracker().addTrackableForPlayer(context.getSource().getPlayer(), new TrackedBlockEntity(pos, context.getSource().getLevel().dimension(), path));
        return 1;
    }

    private int trackEntitiesForPlayer(CommandContext<CommandSourceStack> context, Collection<? extends Entity> entities, NbtPathArgument.NbtPath path) {
        for (Entity entity : entities)
            Datamancer.getTracker().addTrackableForPlayer(context.getSource().getPlayer(), new TrackedEntity(entity, path));

        return 1;
    }

    private int trackScoresForPlayer(CommandContext<CommandSourceStack> context, Collection<ScoreHolder> scoreHolders, Objective objective) {
        for (ScoreHolder scoreHolder : scoreHolders)
            Datamancer.getTracker().addTrackableForPlayer(context.getSource().getPlayer(), new TrackedScore(objective.getName(), scoreHolder.getScoreboardName()));

        return 1;
    }

    private int trackStorageEntryForPlayer(CommandContext<CommandSourceStack> context, ResourceLocation storage, NbtPathArgument.NbtPath path) {
        Datamancer.getTracker().addTrackableForPlayer(context.getSource().getPlayer(), new TrackedStorageEntry(storage, path));
        return 1;
    }
}
