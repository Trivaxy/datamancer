package xyz.trivaxy.datamancer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.trivaxy.datamancer.command.DatamancerCommand;
import xyz.trivaxy.datamancer.networking.packet.DatamancerPackets;
import xyz.trivaxy.datamancer.profile.FunctionProfiler;
import xyz.trivaxy.datamancer.tracker.ServerTracker;
import xyz.trivaxy.datamancer.watch.DataPackWatcher;

public class Datamancer implements ModInitializer {

    public static final String MOD_ID = "datamancer";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static DataPackWatcher watcher;
    private static ServerTracker tracker;

    @Override
    public void onInitialize() {
        DatamancerPackets.registerPacketTypes();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> DatamancerCommand.registerCommands(dispatcher, environment));
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register(((server, resourceManager) -> FunctionProfiler.getInstance().restart()));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            watcher = new DataPackWatcher(server.getLevel(Level.OVERWORLD).getAttachedOrCreate(Attachments.WATCHER_STATE_ATTACHMENT, DataPackWatcher.State::empty));

            if (watcher.isActive())
                watcher.start(server);

            tracker = new ServerTracker(server);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> watcher.stop());
        ServerTickEvents.END_SERVER_TICK.register(MarkerInfoHandler::sendMarkerInfoToPlayers);
        ServerTickEvents.END_SERVER_TICK.register(server -> tracker.sendAllTrackedInfo());
    }

    public static DataPackWatcher getWatcher() {
        return watcher;
    }

    public static ServerTracker getTracker() {
        return tracker;
    }

    public static ResourceLocation in(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
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
