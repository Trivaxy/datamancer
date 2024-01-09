package xyz.trivaxy.datamancer.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.DetectedVersion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.LevelResource;
import xyz.trivaxy.datamancer.Datamancer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class MakeCommand extends DatamancerCommand {

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
        dispatcher.register(literal("make")
            .requires(source -> source.hasPermission(3))
            .then(argument("name", StringArgumentType.word())
                .executes(context -> {
                    String name = StringArgumentType.getString(context, "name");
                    return createPack(context, name, null);
                })
                .then(argument("description", StringArgumentType.greedyString())
                    .executes(context -> {
                        String name = StringArgumentType.getString(context, "name");
                        String description = StringArgumentType.getString(context, "description");
                        return createPack(context, name, description);
                    }
                )
            ))
        );
    }

    private int createPack(CommandContext<CommandSourceStack> context, String name, String description) {
        Path datapacksFolder = context.getSource().getServer().getWorldPath(LevelResource.DATAPACK_DIR);
        Path packFolder = datapacksFolder.resolve(name);
        int datapackVersion = DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA);

        JsonObject packMeta = new JsonObject();
        packMeta.addProperty("pack_format", datapackVersion);
        packMeta.addProperty("description", description == null ? "" : description);

        JsonObject root = new JsonObject();
        root.add("pack", packMeta);

        if (packFolder.toFile().exists()) {
            replyFailure(context.getSource(), Component.literal("Pack already exists"));
            return 0;
        }

        if (!packFolder.toFile().mkdirs()) {
            errorAndCleanup(context.getSource(), name, "unable to create pack folder");
            return 0;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(packFolder.resolve("pack.mcmeta").toFile())) {
            writer.write(gson.toJson(root));
        } catch (IOException e) {
            errorAndCleanup(context.getSource(), name, "unable to write pack.mcmeta");
            return 0;
        }

        if (!packFolder.resolve("data").resolve(name).toFile().mkdirs()) {
            errorAndCleanup(context.getSource(), name, "unable to create data folder");
            return 0;
        }

        if (!context.getSource().isPlayer() || context.getSource().getServer().isDedicatedServer()) {
            replySuccess(context.getSource(), Component.literal("Created pack successfully."));
            return 1;
        }

        Component fileLink = Component
                .literal("Created pack successfully.")
                .withStyle(ChatFormatting.UNDERLINE)
                .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, packFolder.toAbsolutePath().toString())));

        replySuccess(context.getSource(), fileLink);
        
        return 1;
    }

    private void errorAndCleanup(CommandSourceStack source, String packName, String reason) {
        Datamancer.logError("Failed to create datapack \"" + packName + "\": " + reason);
        replyFailure(source, Component.literal("Failed to create datapack"));

        Path packFolder = source.getServer().getWorldPath(LevelResource.DATAPACK_DIR).resolve(packName);

        if (packFolder.toFile().exists()) {
            if (!packFolder.toFile().delete()) {
                Datamancer.logError("Failed to delete pack folder during cleanup: " + packFolder.toAbsolutePath().toString());
            }
        }
    }
}
