package xyz.trivaxy.datamancer.tracker;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.trivaxy.datamancer.Attachments;
import xyz.trivaxy.datamancer.command.entry.DebugEntry;
import xyz.trivaxy.datamancer.networking.packet.tracker.ClearTrackerPacket;
import xyz.trivaxy.datamancer.networking.packet.tracker.TrackerInfoPacket;

import java.util.ArrayList;
import java.util.List;

public class ServerTracker {

    private final MinecraftServer server;

    public ServerTracker(MinecraftServer server) {
        this.server = server;
    }

    public void addTrackableForPlayer(ServerPlayer player, String template) {
        player.getAttachedOrCreate(Attachments.PLAYER_TRACKER_LIST_ATTACHMENT).addTemplate(template);
    }

    public void sendAllTrackedInfo() {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.hasPermissions(2) || !player.hasAttached(Attachments.PLAYER_TRACKER_LIST_ATTACHMENT))
                continue;

            sendInfoToPlayer(player);
        }
    }

    private void sendInfoToPlayer(ServerPlayer player) {
        TrackerList trackerList = player.getAttachedOrCreate(Attachments.PLAYER_TRACKER_LIST_ATTACHMENT);
        List<String> payload = new ArrayList<>();

        for (String template : trackerList.getTemplates()) {
            Component expandedResult = Component.literal("X").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD);

            try {
                expandedResult = DebugEntry.processTemplate(player.createCommandSourceStack(), template);
            } catch (Exception e) {
                // TODO: Currently we ignore the exception, ideally we don't reprocess the template as long as it's invalid
            }

            // TODO: Should we handle the case where the components are large to avoid huge packets?
            payload.add(Component.Serializer.toJson(expandedResult, server.registryAccess()));
        }

        ServerPlayNetworking.send(player, new TrackerInfoPacket(payload));
    }

    public void clearTrackablesForPlayer(ServerPlayer player) {
        player.getAttachedOrCreate(Attachments.PLAYER_TRACKER_LIST_ATTACHMENT).clear();
        ServerPlayNetworking.send(player, new ClearTrackerPacket());
    }

    public void removeTrackableAtIndexForPlayer(ServerPlayer player, int index) {
        player.getAttachedOrCreate(Attachments.PLAYER_TRACKER_LIST_ATTACHMENT).removeAt(index);
    }
}
