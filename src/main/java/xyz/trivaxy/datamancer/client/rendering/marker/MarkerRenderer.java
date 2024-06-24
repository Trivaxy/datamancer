package xyz.trivaxy.datamancer.client.rendering.marker;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import xyz.trivaxy.datamancer.networking.packet.marker.MarkerGogglesInfoPacket;
import xyz.trivaxy.datamancer.networking.packet.marker.MarkerGogglesOffPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MarkerRenderer {

    private static Map<UUID, Pair<Vec3, Integer>> markers = new HashMap<>();

    public static void renderMarkers(WorldRenderContext context) {
        for (Pair<Vec3, Integer> pair : markers.values()) {
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
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f posMatrix = poseStack.last().pose();

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        buffer.addVertex(posMatrix, -0.5f, 0.5f, 0).setColor(r, g, b, 1f);
        buffer.addVertex(posMatrix, -0.5f, -0.5f, 0).setColor(r, g, b, 1f);
        buffer.addVertex(posMatrix, 0.5f, -0.5f, 0).setColor(r, g, b, 1f);
        buffer.addVertex(posMatrix, 0.5f, 0.5f, 0).setColor(r, g, b, 1f);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        poseStack.popPose();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableCull();
    }

    public static void handleMarkerInfoPacket(MarkerGogglesInfoPacket packet, ClientPlayNetworking.Context context) {
        context.client().execute(() -> markers = packet.markers());
    }

    public static void handleMarkerGogglesOffPacket(MarkerGogglesOffPacket packet, ClientPlayNetworking.Context context) {
        context.client().execute(() -> markers.clear());
    }
}
