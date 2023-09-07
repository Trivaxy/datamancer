package xyz.trivaxy.datamancer.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import xyz.trivaxy.datamancer.MarkerInfoHandler;
import xyz.trivaxy.datamancer.client.rendering.marker.MarkerRenderer;

@Environment(net.fabricmc.api.EnvType.CLIENT)
public class DatamancerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(MarkerInfoHandler.MARKER_INFO_PACKET_ID, MarkerRenderer::handleMarkerInfoPacket);
        ClientPlayNetworking.registerGlobalReceiver(MarkerInfoHandler.MARKER_GOGGLES_OFF, MarkerRenderer::handleMarkerGogglesOffPacket);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(MarkerRenderer::renderMarkers);
    }
}
