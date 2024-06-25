package xyz.trivaxy.datamancer.tracker;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public interface Trackable {

    boolean canTrack(MinecraftServer server);

    Component getTitle(MinecraftServer server);

    Component getValue(MinecraftServer server);
}
