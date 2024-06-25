package xyz.trivaxy.datamancer.networking.packet.tracker;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import xyz.trivaxy.datamancer.Datamancer;

import java.util.ArrayList;
import java.util.List;

public record TrackerInfoPacket(List<Pair<String, String>> entries) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TrackerInfoPacket> PACKET_ID = new CustomPacketPayload.Type<>(Datamancer.in("tracker_info"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TrackerInfoPacket> PACKET_CODEC = StreamCodec.of((buf, obj) -> obj.write(buf), TrackerInfoPacket::new);

    public TrackerInfoPacket(RegistryFriendlyByteBuf buf) {
        this(read(buf));
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(entries.size());

        for (Pair<String, String> entry : entries) {
            buf.writeUtf(entry.getFirst());
            buf.writeUtf(entry.getSecond());
        }
    }

    public static List<Pair<String, String>> read(RegistryFriendlyByteBuf buf) {
        int size = buf.readInt();
        List<Pair<String, String>> entries = new ArrayList<>();

        for (int i = 0; i < size; i++)
            entries.add(Pair.of(buf.readUtf(), buf.readUtf()));

        return entries;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
