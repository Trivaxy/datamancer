package xyz.trivaxy.datamancer.networking.packet;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import xyz.trivaxy.datamancer.networking.packet.marker.MarkerGogglesInfoPacket;
import xyz.trivaxy.datamancer.networking.packet.marker.MarkerGogglesOffPacket;

public class DatamancerPackets {

    public static void registerPacketTypes() {
        PayloadTypeRegistry.playS2C().register(MarkerGogglesOffPacket.PACKET_ID, MarkerGogglesOffPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(MarkerGogglesInfoPacket.PACKET_ID, MarkerGogglesInfoPacket.PACKET_CODEC);
    }
}
