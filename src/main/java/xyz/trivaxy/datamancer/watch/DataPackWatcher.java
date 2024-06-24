package xyz.trivaxy.datamancer.watch;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sun.nio.file.ExtendedWatchEventModifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.io.FilenameUtils;
import xyz.trivaxy.datamancer.Datamancer;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataPackWatcher {

    private ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private State watcherState;

    private static final Set<String> watchedFileExtensions = ImmutableSet.of(
            "mcfunction",
            "json",
            "nbt",
            "mcmeta"
    );

    public DataPackWatcher(State state) {
        watcherState = state;
    }

    public void watchPack(String id) {
        watcherState.watchedPackIds.add(id);
    }

    public void unwatchPack(String id) {
        watcherState.watchedPackIds.remove(id);
    }

    public boolean isWatching(String id) {
        return watcherState.watchedPackIds.contains(id);
    }

    public void start(MinecraftServer server) {
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

                        Thread.sleep(50);

                        List<WatchEvent<?>> events = key.pollEvents();

                        if (events.isEmpty() || watcherState.watchedPackIds.isEmpty()) {
                            key.reset();
                            continue;
                        }

                        WatchEvent<?> event = events.get(0);

                        if (!shouldAcceptWatchEvent(event)) {
                            key.reset();
                            continue;
                        }

                        Path path = (Path) event.context();
                        String packId = "file/" + path.getName(0);

                        repo.reload();

                        // if a pack is on the watchlist but not enabled, remove it
                        if (repo.isAvailable(packId) && !repo.getSelectedIds().contains(packId)) {
                            unwatchPack(packId);
                            key.reset();
                            continue;
                        }

                        if (!isWatching(packId)) {
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
                    Datamancer.logError("Error while watching datapacks folder", e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        } catch (Exception e) {
            Datamancer.logError("Failed to start DataPackWatcher", e);
        }

        watcherState.active = true;
    }

    public void stop() {
        EXECUTOR_SERVICE.shutdownNow();
    }

    public void shutdown() {
        stop();
        watcherState.active = false;
    }

    private boolean shouldAcceptWatchEvent(WatchEvent<?> event) {
        Path path = (Path) event.context();
        File file = new File(String.valueOf(path));

        // a directory getting deleted or changed should trigger a reload
        if (file.isDirectory()) {
            return event.kind() == StandardWatchEventKinds.ENTRY_DELETE || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY;
        }

        return watchedFileExtensions.contains(FilenameUtils.getExtension(file.getName()));
    }

    public boolean isActive() {
        return watcherState.active;
    }

    public Collection<String> getWatchList() {
        return watcherState.watchedPackIds;
    }

    public static class State {
        private final Set<String> watchedPackIds;
        private boolean active;

        public static final Codec<State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf).fieldOf("watched_pack_ids").forGetter(State::getWatchedPacks),
                Codec.BOOL.fieldOf("active").forGetter(State::isActive)
        ).apply(instance, State::new));

        public State(Set<String> watchedPackIds, boolean active) {
            this.watchedPackIds = watchedPackIds;
            this.active = active;
        }

        public static State empty() {
            return new State(new HashSet<>(), false);
        }

        // for codec

        private Set<String> getWatchedPacks() {
            return watchedPackIds;
        }

        private boolean isActive() {
            return active;
        }
    }
}
