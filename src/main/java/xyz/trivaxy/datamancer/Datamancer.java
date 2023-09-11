package xyz.trivaxy.datamancer;

import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.trivaxy.datamancer.command.DatamancerCommand;
import xyz.trivaxy.datamancer.profile.FunctionProfiler;
import xyz.trivaxy.datamancer.watch.DataPackWatcher;
import xyz.trivaxy.datamancer.watch.WatcherStateComponent;

public class Datamancer implements ModInitializer, LevelComponentInitializer {

    public static final String MOD_ID = "datamancer";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> DatamancerCommand.registerCommands(dispatcher, environment));
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register(((server, resourceManager) -> FunctionProfiler.getInstance().restart()));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            WatcherStateComponent watcher = DataPackWatcher.KEY.get(server.getLevel(Level.OVERWORLD).getLevelData());
            if (watcher.isActive())
                watcher.start(server);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> DataPackWatcher.KEY.get(server.getLevel(Level.OVERWORLD).getLevelData()).stop());
        ServerTickEvents.END_SERVER_TICK.register(MarkerInfoHandler::sendMarkerInfoToPlayers);
    }

    @Override
    public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
        registry.register(DataPackWatcher.KEY, level -> new DataPackWatcher());
    }

    public static ResourceLocation in(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static String inRaw(String path) {
        return in(path).toString();
    }

    public static void log(String message) {
        LOGGER.info(message);
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
