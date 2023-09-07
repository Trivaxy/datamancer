package xyz.trivaxy.datamancer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.trivaxy.datamancer.command.DatamancerCommand;
import xyz.trivaxy.datamancer.profile.FunctionWatcher;

public class Datamancer implements ModInitializer {

    private static final String MOD_ID = "datamancer";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> DatamancerCommand.registerCommands(dispatcher, environment));
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register(((server, resourceManager) -> FunctionWatcher.getInstance().restart()));
        ServerTickEvents.END_SERVER_TICK.register(MarkerInfoHandler::sendMarkerInfoToPlayers);
    }

    public static ResourceLocation in(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static void logWarn(String message) {
        LOGGER.warn(message);
    }

    public static void logError(String message) {
        LOGGER.error(message);
    }

    public static void logError(String message, Throwable throwable) {
        LOGGER.error(message + ": ", throwable);
    }

    public static void logError(Throwable throwable) {
        LOGGER.error(throwable.toString());
    }
}
