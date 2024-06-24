package xyz.trivaxy.datamancer.networking.packet.marker;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import xyz.trivaxy.datamancer.Datamancer;

public record MarkerGogglesOffPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MarkerGogglesOffPacket> PACKET_ID = new CustomPacketPayload.Type<>(Datamancer.in("marker_goggles_off"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MarkerGogglesOffPacket> PACKET_CODEC = StreamCodec.of((encoder, obj) -> {}, buf -> new MarkerGogglesOffPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}