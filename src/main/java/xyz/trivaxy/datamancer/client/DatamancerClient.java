package xyz.trivaxy.datamancer.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import xyz.trivaxy.datamancer.client.rendering.marker.MarkerRenderer;
import xyz.trivaxy.datamancer.networking.packet.marker.MarkerGogglesInfoPacket;
import xyz.trivaxy.datamancer.networking.packet.marker.MarkerGogglesOffPacket;

@Environment(net.fabricmc.api.EnvType.CLIENT)
public class DatamancerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(MarkerGogglesInfoPacket.PACKET_ID, MarkerRenderer::handleMarkerInfoPacket);
        ClientPlayNetworking.registerGlobalReceiver(MarkerGogglesOffPacket.PACKET_ID, MarkerRenderer::handleMarkerGogglesOffPacket);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(MarkerRenderer::renderMarkers);
    }
}
