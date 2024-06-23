package xyz.trivaxy.datamancer.tracker;

import net.minecraft.server.MinecraftServer;

import java.util.*;

public class TrackerList {

    public static final int MAX_DATA_LENGTH = 40;
    private Map<String, Trackable> trackers = new HashMap<>();

    public void addTracker(Trackable tracker) {
        trackers.put(tracker.getId(), tracker);
    }

    public void removeTrackerById(String id) {
        trackers.remove(id);
    }

    public void update(MinecraftServer server) {
        trackers.entrySet().removeIf(entry -> !entry.getValue().canTrack(server));
    }

    public Iterator<Trackable> getTrackers() {
        return trackers.values().iterator();
    }
}
