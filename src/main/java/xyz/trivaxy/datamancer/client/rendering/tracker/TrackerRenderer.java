package xyz.trivaxy.datamancer.client.rendering.tracker;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import xyz.trivaxy.datamancer.networking.packet.tracker.ClearTrackerPacket;
import xyz.trivaxy.datamancer.networking.packet.tracker.TrackerInfoPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrackerRenderer {

    private static List<Component> entries = new ArrayList<>();
    private static final int LINE_PADDING = 2;
    private static final int MAX_VALUE_LENGTH = 8;

    public static void renderTracker(GuiGraphics guiGraphics, DeltaTracker delta) {
        if (entries.isEmpty())
            return;

        Font font = Minecraft.getInstance().font;
        int trackerHeight = entries.size() * (font.lineHeight + LINE_PADDING);
        int maxExpandedWidth = entries
            .stream()
            .map(font::width)
            .max(Integer::compareTo)
            .get();

        guiGraphics.drawManaged(() -> {
            guiGraphics.fill(0, guiGraphics.guiHeight() / 2 - trackerHeight / 2, maxExpandedWidth + font.width("00") + 2, guiGraphics.guiHeight() / 2 + trackerHeight / 2 + 1, Minecraft.getInstance().options.getBackgroundColor(0.7f));

            int y = guiGraphics.guiHeight() / 2 - trackerHeight / 2 + LINE_PADDING;
            int i = 0;
            for (var entry : entries) {
                guiGraphics.drawString(font, String.valueOf(i), 2, y, ChatFormatting.RED.getColor());
                guiGraphics.drawString(font, entry, font.width("00") + 1, y, -1);
                y += font.lineHeight + LINE_PADDING;
                i++;
            }
        });
    }

    public static void handleInfoPacket(TrackerInfoPacket trackerInfoPacket, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            entries = trackerInfoPacket
                .entries()
                .stream()
                .map(entry -> (Component) Component.Serializer.fromJson(entry, context.player().registryAccess()))
                .collect(Collectors.toList());
        });
    }

    public static void handleClearPacket(ClearTrackerPacket clearTrackerPacket, ClientPlayNetworking.Context context) {
        context.client().execute(() -> entries.clear());
    }
}
