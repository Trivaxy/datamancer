package xyz.trivaxy.datamancer.tracker;

import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import xyz.trivaxy.datamancer.util.OurComponentUtils;

public class TrackedStorageEntry implements Trackable {

    private final ResourceLocation storageId;
    private final NbtPathArgument.NbtPath path;

    public TrackedStorageEntry(ResourceLocation storageId, NbtPathArgument.NbtPath path) {
        this.storageId = storageId;
        this.path = path;
    }

    @Override
    public boolean canTrack(MinecraftServer server) {
        return true;
    }

    @Override
    public Component getTitle(MinecraftServer server) {
        String title = storageId.toString();

        if (path != null)
            title += "." + path;

        return Component.literal(title);
    }

    @Override
    public Component getValue(MinecraftServer server) {
        return OurComponentUtils.getPrettyPrintedTag(server.getCommandStorage().get(storageId), path);
    }
}
