package xyz.trivaxy.datamancer.client.rendering.marker;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class MarkerRenderer {

    private static Map<UUID, Pair<Vec3, Long>> markers = new HashMap<>();

    public static void renderMarkers(WorldRenderContext context) {
        for (Pair<Vec3, Long> pair : markers.values()) {
            renderMarker(context, pair.getFirst(), pair.getSecond());
        }
    }

    private static void renderMarker(WorldRenderContext context, Vec3 position, long color) {
        PoseStack poseStack = context.matrixStack();
        Vec3 cameraPos = context.camera().getPosition();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        poseStack.translate(position.x, position.y, position.z);
        poseStack.scale(0.1f, 0.1f, 0.1f);
        poseStack.mulPose(context.camera().rotation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f posMatrix = poseStack.last().pose();

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(posMatrix, -0.5f, 0.5f, 0).color(r, g, b, 1f).endVertex();
        buffer.vertex(posMatrix, -0.5f, -0.5f, 0).color(r, g, b, 1f).endVertex();
        buffer.vertex(posMatrix, 0.5f, -0.5f, 0).color(r, g, b, 1f).endVertex();
        buffer.vertex(posMatrix, 0.5f, 0.5f, 0).color(r, g, b, 1f).endVertex();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        tesselator.end();

        poseStack.popPose();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableCull();
    }

    public static void handleMarkerInfoPacket(Minecraft minecraft, ClientPacketListener listener, FriendlyByteBuf buf, PacketSender responseSender) {
        int markerCount = buf.readInt();

        HashMap<UUID, Pair<Vec3, Long>> incomingMarkers = new HashMap<>();

        for (int i = 0; i < markerCount; i++) {
            UUID markerUUID = buf.readUUID();
            Vec3 markerPos = buf.readVec3();
            long markerColor = buf.readLong();

            incomingMarkers.put(markerUUID, Pair.of(markerPos, markerColor));
        }

        minecraft.execute(() -> markers = incomingMarkers);
    }

    public static void handleMarkerGogglesOffPacket(Minecraft minecraft, ClientPacketListener listener, FriendlyByteBuf buf, PacketSender responseSender) {
        minecraft.execute(() -> markers.clear());
    }
}
