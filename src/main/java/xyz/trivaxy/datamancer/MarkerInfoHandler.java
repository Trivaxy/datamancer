package xyz.trivaxy.datamancer;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Marker;
import xyz.trivaxy.datamancer.access.MarkerListenerAccess;

import java.util.List;

public class MarkerInfoHandler {

    public static final ResourceLocation MARKER_INFO_PACKET_ID = Datamancer.in("marker_info");
    public static final ResourceLocation MARKER_GOGGLES_OFF = Datamancer.in("marker_goggles_off");

    public static void sendMarkerInfoToPlayers(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(player -> {
            if (!player.hasPermissions(2) || !((MarkerListenerAccess) player).isListeningForMarkers())
                return;

            FriendlyByteBuf markerInfo = createMarkerInfoByteBuf(player.serverLevel().getEntitiesOfClass(Marker.class, player.getBoundingBox().inflate(20)));
            ServerPlayNetworking.send(player, MARKER_INFO_PACKET_ID, markerInfo);
        });
    }

    private static FriendlyByteBuf createMarkerInfoByteBuf(List<Marker> markers) {
        FriendlyByteBuf buf = PacketByteBufs.create();

        buf.writeInt(markers.size());

        for (Marker marker : markers) {
            buf.writeUUID(marker.getUUID());
            buf.writeVec3(marker.position());
        }

        return buf;
    }
}
