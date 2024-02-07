package xyz.trivaxy.datamancer;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Marker;
import xyz.trivaxy.datamancer.access.MarkerListenerAccess;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
            buf.writeLong(calculateMarkerColor(marker));
        }

        return buf;
    }

    private static int calculateTagsColor(Collection<String> tags) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            for (String tag : tags) {
                digest.update(tag.getBytes(StandardCharsets.UTF_8));
            }

            byte[] hash = digest.digest();
            return (hash[0] & 0xFF) << 16 | (hash[1] & 0xFF) << 8 | (hash[2] & 0xFF);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found. What kind of system are you running on???", e);
        }
    }

    private static int calculateUUIDColor(UUID uuid) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(uuid.toString().getBytes(StandardCharsets.UTF_8));

            byte[] hash = digest.digest();
            return (hash[0] & 0xFF) << 16 | (hash[1] & 0xFF) << 8 | (hash[2] & 0xFF);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found. What kind of system are you running on???", e);
        }
    }

    private static int calculateMarkerColor(Marker marker) {
        int color = !marker.getTags().isEmpty() ? calculateTagsColor(marker.getTags()) : calculateUUIDColor(marker.getUUID());

        int red = applyGammaCorrection((color >> 16) & 0xFF);
        int green = applyGammaCorrection((color >> 8) & 0xFF);
        int blue = applyGammaCorrection(color & 0xFF);

        return red << 16 | green << 8 | blue;
    }

    private static int applyGammaCorrection(int component) {
        double gamma = 1.2;
        double correctedComponent = 255.0 * Math.pow(component / 255.0, gamma);
        return (int) Math.min(255, correctedComponent);
    }
}
