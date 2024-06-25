package xyz.trivaxy.datamancer.tracker;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.trivaxy.datamancer.networking.packet.tracker.TrackerInfoPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerTracker {

    private final MinecraftServer server;
    private final Map<ServerPlayer, TrackerList> perPlayerTracker = new HashMap<>();

    public ServerTracker(MinecraftServer server) {
        this.server = server;
    }

    public void addTrackableForPlayer(ServerPlayer player, Trackable trackable) {
        getOrCreateTrackerForPlayer(player).addTracker(trackable);
    }

    public void sendAllTrackedInfo() {
        for (ServerPlayer player : perPlayerTracker.keySet())
            sendInfoToPlayer(player);
    }

    private void sendInfoToPlayer(ServerPlayer player) {
        TrackerList trackerList = getOrCreateTrackerForPlayer(player);
        List<Pair<String, String>> payload = new ArrayList<>();

        for (var entry : trackerList.getTrackers()) {
            Component title = entry.getTitle(server);
            Component value = entry.canTrack(server) ? entry.getValue(server) : Component.literal("?").withStyle(ChatFormatting.RED);

            // TODO: Should we handle the case where the title/value components are large to avoid huge packets?
            payload.add(Pair.of(Component.Serializer.toJson(title, server.registryAccess()), Component.Serializer.toJson(value, server.registryAccess())));
        }

        ServerPlayNetworking.send(player, new TrackerInfoPacket(payload));
    }

    private TrackerList getOrCreateTrackerForPlayer(ServerPlayer player) {
        return perPlayerTracker.computeIfAbsent(player, k -> new TrackerList());
    }
}
