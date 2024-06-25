package xyz.trivaxy.datamancer.networking.packet.tracker;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import xyz.trivaxy.datamancer.Datamancer;

public record ClearTrackerPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClearTrackerPacket> PACKET_ID = new CustomPacketPayload.Type<>(Datamancer.in("clear_tracker"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearTrackerPacket> PACKET_CODEC = StreamCodec.of((encoder, obj) -> {}, buf -> new ClearTrackerPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
