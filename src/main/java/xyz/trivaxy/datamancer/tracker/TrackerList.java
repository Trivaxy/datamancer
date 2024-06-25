package xyz.trivaxy.datamancer.tracker;

import java.util.*;

public class TrackerList {

    public static final int MAX_DATA_LENGTH = 40;
    private final List<Trackable> trackers = new ArrayList<>();

    public void addTracker(Trackable tracker) {
        trackers.add(tracker);
    }

    public Collection<Trackable> getTrackers() {
        return trackers;
    }

    public int size() {
        return trackers.size();
    }
}
