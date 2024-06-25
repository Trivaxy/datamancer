package xyz.trivaxy.datamancer.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import xyz.trivaxy.datamancer.client.rendering.marker.MarkerRenderer;
import xyz.trivaxy.datamancer.client.rendering.tracker.TrackerRenderer;
import xyz.trivaxy.datamancer.networking.packet.marker.MarkerGogglesInfoPacket;
import xyz.trivaxy.datamancer.networking.packet.marker.MarkerGogglesOffPacket;
import xyz.trivaxy.datamancer.networking.packet.tracker.ClearTrackerPacket;
import xyz.trivaxy.datamancer.networking.packet.tracker.TrackerInfoPacket;

@Environment(net.fabricmc.api.EnvType.CLIENT)
public class DatamancerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(MarkerGogglesInfoPacket.PACKET_ID, MarkerRenderer::handleMarkerInfoPacket);
        ClientPlayNetworking.registerGlobalReceiver(MarkerGogglesOffPacket.PACKET_ID, MarkerRenderer::handleMarkerGogglesOffPacket);
        ClientPlayNetworking.registerGlobalReceiver(TrackerInfoPacket.PACKET_ID, TrackerRenderer::handleInfoPacket);
        ClientPlayNetworking.registerGlobalReceiver(ClearTrackerPacket.PACKET_ID, TrackerRenderer::handleClearPacket);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(MarkerRenderer::renderMarkers);
        HudRenderCallback.EVENT.register(TrackerRenderer::renderTracker);
    }
}
