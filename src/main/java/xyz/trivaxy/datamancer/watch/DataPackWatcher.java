package xyz.trivaxy.datamancer.watch;

import com.sun.nio.file.ExtendedWatchEventModifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelResource;
import xyz.trivaxy.datamancer.Datamancer;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DataPackWatcher {

    private ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private final Set<String> watchedPackIds = new HashSet<>();
    private boolean started = false;
    private static final DataPackWatcher INSTANCE = new DataPackWatcher();

    public void watchPack(String id) {
        watchedPackIds.add(id);
    }

    public void unwatchPack(String id) {
        watchedPackIds.remove(id);
    }

    public boolean isStarted() {
        return started;
    }

    // will take in the datapacks folder
    public void start(MinecraftServer server) {
        if (started) {
            Datamancer.logWarn("Attempted to start DataPackWatcher twice. This should not happen");
            return;
        }

        PackRepository repo = server.getPackRepository();
        Path datapacksFolder = server.getWorldPath(LevelResource.DATAPACK_DIR);

        try {
            EXECUTOR_SERVICE.execute(() -> {
                try {
                    WatchService watchService = FileSystems.getDefault().newWatchService();
                    datapacksFolder.register(
                            watchService,
                            new WatchEvent.Kind[] {
                                StandardWatchEventKinds.ENTRY_MODIFY,
                                StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_DELETE
                            },
                            ExtendedWatchEventModifier.FILE_TREE
                    );

                    while (true) {
                        WatchKey key;

                        try {
                            key = watchService.take();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }

                        List<WatchEvent<?>> events = key.pollEvents();

                        if (events.isEmpty() || watchedPackIds.isEmpty()) {
                            key.reset();
                            continue;
                        }

                        // doesn't matter what the event is, we just need to reload the needed packs
                        WatchEvent<?> event = events.get(0);
                        Path path = (Path) event.context();
                        String packId = "file/" + path.getName(0);

                        repo.reload();

                        // if a pack is on the watchlist but not enabled, remove it
                        if (repo.isAvailable(packId) && !repo.getSelectedIds().contains(packId)) {
                            watchedPackIds.remove(packId);
                            key.reset();
                            continue;
                        }

                        if (!watchedPackIds.contains(packId)) {
                            key.reset();
                            continue;
                        }

                        Datamancer.log("Auto reloading...");

                        List<String> packs = new ArrayList<>(repo.getSelectedIds());

                        server.reloadResources(packs).exceptionally(e -> {
                            Datamancer.logError("DataPackWatcher failed to reload packs", e);
                            return null;
                        });

                        key.reset();
                    }

                    watchService.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            Datamancer.logError("Failed to start DataPackWatcher", e);
        }

        started = true;
    }

    public void stop() {
        EXECUTOR_SERVICE.shutdownNow();
        EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
        started = false;
    }

    public static DataPackWatcher getInstance() {
        return INSTANCE;
    }
}
