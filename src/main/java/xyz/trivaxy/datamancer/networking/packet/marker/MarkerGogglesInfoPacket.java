package xyz.trivaxy.datamancer.networking.packet.marker;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import xyz.trivaxy.datamancer.Datamancer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record MarkerGogglesInfoPacket(Map<UUID, Pair<Vec3, Integer>> markers) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MarkerGogglesInfoPacket> PACKET_ID = new CustomPacketPayload.Type<>(Datamancer.in("marker_goggles_info"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MarkerGogglesInfoPacket> PACKET_CODEC = StreamCodec.of((buf, obj) -> obj.write(buf), MarkerGogglesInfoPacket::new);

    public MarkerGogglesInfoPacket(RegistryFriendlyByteBuf buf) {
        this(read(buf));
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(markers.size());

        for (var entry : markers.entrySet()) {
            buf.writeUUID(entry.getKey()); // uuid
            buf.writeVec3(entry.getValue().getFirst()); // vec3
            buf.writeInt(entry.getValue().getSecond()); // color code
        }
    }

    private static Map<UUID, Pair<Vec3, Integer>> read(RegistryFriendlyByteBuf buf) {
        int markerCount = buf.readInt();

        HashMap<UUID, Pair<Vec3, Integer>> incomingMarkers = new HashMap<>();

        for (int i = 0; i < markerCount; i++) {
            UUID markerUUID = buf.readUUID();
            Vec3 markerPos = buf.readVec3();
            int markerColor = buf.readInt();

            incomingMarkers.put(markerUUID, Pair.of(markerPos, markerColor));
        }

        return incomingMarkers;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
