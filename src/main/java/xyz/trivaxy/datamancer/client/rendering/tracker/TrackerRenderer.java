package xyz.trivaxy.datamancer.client.rendering.tracker;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import xyz.trivaxy.datamancer.networking.packet.tracker.TrackerInfoPacket;

import java.util.ArrayList;
import java.util.List;

public class TrackerRenderer {

    private static List<Pair<Component, Component>> entries = new ArrayList<>();
    private static final int LINE_PADDING = 2;

    public static void renderTracker(GuiGraphics guiGraphics, DeltaTracker delta) {
        if (entries.isEmpty())
            return;

        Font font = Minecraft.getInstance().font;
        int trackerHeight = entries.size() * (font.lineHeight + LINE_PADDING);
        int trackerWidth = entries
            .stream()
            .map(entry -> font.width(entry.getFirst()) + font.width(": ") + font.width(entry.getSecond()))
            .max(Integer::compareTo)
            .get();

        guiGraphics.drawManaged(() -> {
            guiGraphics.fill(0, guiGraphics.guiHeight() / 2 - trackerHeight / 2, trackerWidth, guiGraphics.guiHeight() / 2 + trackerHeight / 2, Minecraft.getInstance().options.getBackgroundColor(0.7f));

            int y = guiGraphics.guiHeight() / 2 - trackerHeight / 2 + LINE_PADDING;
            for (var entry : entries) {
                guiGraphics.drawString(font, entry.getFirst(), 2, y, -1);
                guiGraphics.drawString(font, entry.getSecond(), trackerWidth - font.width(entry.getSecond() ) - 2, y, -1);
                y += font.lineHeight + LINE_PADDING;
            }
        });
    }

    public static void handleInfoPacket(TrackerInfoPacket trackerInfoPacket, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            entries = trackerInfoPacket
                .entries()
                .stream()
                .map(entry -> Pair.of(
                    (Component) Component.Serializer.fromJson(entry.getFirst(), context.player().registryAccess()),
                    (Component) Component.Serializer.fromJson(entry.getSecond(), context.player().registryAccess()))
                )
                .toList();
        });
    }
}
