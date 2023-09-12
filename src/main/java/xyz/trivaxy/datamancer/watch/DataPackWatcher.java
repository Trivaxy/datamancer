package xyz.trivaxy.datamancer.watch;

import com.sun.nio.file.ExtendedWatchEventModifier;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelResource;
import xyz.trivaxy.datamancer.Datamancer;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataPackWatcher implements WatcherStateComponent {

    private ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    public Set<String> watchedPackIds = new HashSet<>();
    public boolean active = false;

    public static final ComponentKey<WatcherStateComponent> KEY = ComponentRegistry.getOrCreate(Datamancer.in("watcher"), WatcherStateComponent.class);

    @Override
    public void watchPack(String id) {
        watchedPackIds.add(id);
    }

    @Override
    public void unwatchPack(String id) {
        watchedPackIds.remove(id);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isWatching(String id) {
        return watchedPackIds.contains(id);
    }

    @Override
    public Collection<String> getWatchList() {
        return Collections.unmodifiableSet(watchedPackIds);
    }

    @Override
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
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            Datamancer.logError("Failed to start DataPackWatcher", e);
        }

        active = true;
    }

    @Override
    public void stop() {
        EXECUTOR_SERVICE.shutdownNow();
        EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    }

    @Override
    public void shutdown() {
        stop();
        active = false;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        ListTag watched = tag.getList("watched_packs", Tag.TAG_STRING);

        for (Tag t : watched) {
            watchedPackIds.add(t.getAsString());
        }

        active = tag.getBoolean("watcher_started");
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        ListTag watched = new ListTag();

        for (String id : watchedPackIds) {
            watched.add(StringTag.valueOf(id));
        }

        tag.put("watched_packs", watched);
        tag.putBoolean("watcher_started", active);
    }
}
