package xyz.trivaxy.datamancer.watch;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.server.MinecraftServer;

import java.util.Collection;

public interface WatcherStateComponent extends Component {

    void watchPack(String packId);

    void unwatchPack(String packId);

    boolean isActive();

    boolean isWatching(String packId);

    void start(MinecraftServer server);

    void shutdown();

    void stop();

    Collection<String> getWatchList();
}
