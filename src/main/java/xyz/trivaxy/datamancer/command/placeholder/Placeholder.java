package xyz.trivaxy.datamancer.command.placeholder;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.*;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import xyz.trivaxy.datamancer.util.OurComponentUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

public class Placeholder {

    private final List<ArgumentType<?>> argumentTypes;
    private final PlaceholderProcessor processor;
    private final int optionals;

    public static final Map<String, Placeholder> PLACEHOLDERS = Map.of(
            "score", new PlaceholderBuilder()
                    .argument(ScoreHolderArgument.scoreHolders())
                    .argument(ObjectiveArgument.objective())
                    .process((context, arguments) -> {
                        ServerScoreboard scoreboard = context.getServer().getScoreboard();
                        Collection<ScoreHolder> scoreHolders = arguments.<ScoreHolderArgument.Result>get(0).getNames(context, Collections::emptyList);
                        Objective objective = scoreboard.getObjective(arguments.get(1));

                        if (objective == null)
                            return Component.literal("None");

                        String result = scoreHolders
                                .stream()
                                .filter(s -> scoreboard.getPlayerScoreInfo(s, objective) != null)
                                .map(s -> String.valueOf(scoreboard.getOrCreatePlayerScore(s, objective).get()))
                                .collect(Collectors.joining(", "));

                        if (result.isBlank())
                            result = "None";

                        return Component.literal(result);
                    }),

            "entity", new PlaceholderBuilder()
                    .argument(EntityArgument.entity())
                    .optional(NbtPathArgument.nbtPath())
                    .process((context, arguments) -> {
                        EntitySelector entitySelector = arguments.get(0);
                        Entity entity = null;

                        try {
                            entity = entitySelector.findSingleEntity(context);
                        } catch (CommandSyntaxException e) {
                            return Component.literal("None");
                        }

                        return OurComponentUtils.getPrettyPrintedTag(NbtPredicate.getEntityTagToCompare(entity), arguments.get(1));
                    }),
            "block", new PlaceholderBuilder()
                    .argument(BlockPosArgument.blockPos())
                    .optional(NbtPathArgument.nbtPath())
                    .process((context, arguments) -> {
                        Coordinates coords = arguments.get(0);
                        BlockPos pos = coords.getBlockPos(context);
                        ServerLevel level = context.getLevel();

                        if (!level.isLoaded(pos))
                            return Component.literal("Unloaded");

                        BlockEntity blockEntity = level.getBlockEntity(pos);

                        if (blockEntity == null)
                            return Component.literal("None");

                        return OurComponentUtils.getPrettyPrintedTag(new BlockDataAccessor(blockEntity, pos).getData(), arguments.get(1));
                    }),
            "storage", new PlaceholderBuilder()
                    .argument(ResourceLocationArgument.id())
                    .optional(NbtPathArgument.nbtPath())
                    .process((context, arguments) -> {
                        CommandStorage storage = context.getServer().getCommandStorage();

                        return OurComponentUtils.getPrettyPrintedTag(storage.get(arguments.get(0)), arguments.get(1));
                    }),
            "list", new PlaceholderBuilder()
                    .argument(EntityArgument.entities())
                    .process((context, argument) -> {
                        EntitySelector entitySelector = argument.get(0);
                        List<? extends Entity> entities = entitySelector.findEntities(context);

                        if (entities.isEmpty())
                            return Component.literal("None");

                        return OurComponentUtils.joinComponents(entities.stream().map(Entity::getDisplayName).collect(Collectors.toList()), ", ");
                    }),
            "state", new PlaceholderBuilder()
                    .argument(BlockPosArgument.blockPos())
                    .process((context, argument) -> {
                        Coordinates coords = argument.get(0);
                        BlockPos pos = coords.getBlockPos(context);
                        ServerLevel level = context.getLevel();

                        if (!level.isLoaded(pos))
                            return Component.literal("Unloaded");

                        return prettyPrintBlockState(level.getBlockState(pos));
                    }),
            "at", new PlaceholderBuilder()
                    .optional(EntityArgument.entities())
                    .process((context, argument) -> {
                        EntitySelector entitySelector = argument.get(0);

                        if (entitySelector == null)
                            return prettyPrintPositions(Collections.singletonList(context.getPosition()));

                        List<? extends Entity> entities = entitySelector.findEntities(context);

                        if (entities.isEmpty())
                            return Component.literal("None");

                        return prettyPrintPositions(entities.stream().map(Entity::position).collect(Collectors.toList()));
                    }),
            "count", new PlaceholderBuilder()
                    .argument(EntityArgument.entities())
                    .process((context, argument) -> {
                        EntitySelector entitySelector = argument.get(0);
                        List<? extends Entity> entities = entitySelector.findEntities(context);

                        return Component.literal(String.valueOf(entities.size()));
                    }),
            "time", new PlaceholderBuilder()
                    .argument(StringArgumentType.word())
                    .process((context, argument) -> switch ((String) argument.get(0)) {
                        case "daytime" -> Component.literal(String.valueOf(context.getLevel().getDayTime() % 24000L));
                        case "gametime" -> Component.literal(String.valueOf(context.getLevel().getGameTime() % 2147483647L));
                        case "day" -> Component.literal(String.valueOf(context.getLevel().getDayTime() / 24000L % 2147483647L));
                        case "realtime" -> Component.literal(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(LocalDateTime.now()));
                        default -> Component.literal("None");
                    })
    );

    public Placeholder(List<ArgumentType<?>> argumentTypes, int optionals, PlaceholderProcessor processor) {
        this.argumentTypes = argumentTypes;
        this.optionals = optionals;
        this.processor = processor;
    }

    public Component expand(CommandSourceStack source, String arguments) throws CommandSyntaxException, PlaceholderException {
        Object[] parsed = new Object[argumentTypes.size()];
        int parsedCount = 0;

        if (arguments == null || arguments.isEmpty()) {
            assertArgCount(0);
            return processor.process(source, new Arguments(parsed));
        }

        StringReader reader = new StringReader(arguments);

        while (reader.canRead() && parsedCount < argumentTypes.size()) {
            parsed[parsedCount] = argumentTypes.get(parsedCount).parse(reader);
            reader.skipWhitespace();
            parsedCount++;
        }

        if (reader.canRead())
            throw new PlaceholderException("Too many arguments, expected at most " + argumentTypes.size());

        assertArgCount(parsedCount);

        return processor.process(source, new Arguments(parsed));
    }

    private void assertArgCount(int actual) throws PlaceholderException {
        int max = argumentTypes.size();
        int min = max - optionals;

        if (actual < min)
            throw new PlaceholderException("Expected at least " + min + " arguments, got " + actual);

        if (actual > max)
            throw new PlaceholderException("Too many arguments, expected at most " + max);
    }

    private static Component prettyPrintBlockState(BlockState state) {
        MutableComponent result = state.getBlock().getName().withStyle(ChatFormatting.RED);
        List<Property<?>> properties = state.getProperties().stream().toList();

        if (properties.isEmpty())
            return result;

        result.append(Component.literal("[").withStyle(ChatFormatting.WHITE));

        for (int i = 0; i < properties.size(); i++) {
            result.append(Component.literal(properties.get(i).getName()).withStyle(ChatFormatting.GRAY));
            result.append(Component.literal("=").withStyle(ChatFormatting.WHITE));
            result.append(Component.literal(state.getValue(properties.get(i)).toString()).withStyle(ChatFormatting.AQUA));

            if (i < properties.size() - 1)
                result.append(Component.literal(", ").withStyle(ChatFormatting.WHITE));
        }

        result.append(Component.literal("]").withStyle(ChatFormatting.WHITE));

        return result;
    }

    private static Component prettyPrintPositions(List<Vec3> positions) {
        MutableComponent result = Component.empty();

        for (int i = 0; i < positions.size(); i++) {
            Vec3 pos = positions.get(i);

            result.append(Component.literal("[").withStyle(ChatFormatting.WHITE));
            result.append(Component.literal(String.format("%.4f", pos.x)).withStyle(ChatFormatting.AQUA));
            result.append(Component.literal(", ").withStyle(ChatFormatting.WHITE));
            result.append(Component.literal(String.format("%.4f", pos.y)).withStyle(ChatFormatting.AQUA));
            result.append(Component.literal(", ").withStyle(ChatFormatting.WHITE));
            result.append(Component.literal(String.format("%.4f", pos.z)).withStyle(ChatFormatting.AQUA));
            result.append(Component.literal("]").withStyle(ChatFormatting.WHITE));

            if (i < positions.size() - 1)
                result.append(Component.literal(", ").withStyle(ChatFormatting.WHITE));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static class Arguments {

        private final Object[] arguments;

        public Arguments(Object[] arguments) {
            this.arguments = arguments;
        }

        public <T> T get(int index) {
            return (T) arguments[index];
        }
    }
}
