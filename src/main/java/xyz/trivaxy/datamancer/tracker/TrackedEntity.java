package xyz.trivaxy.datamancer.tracker;

import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import xyz.trivaxy.datamancer.util.OurComponentUtils;

import java.util.UUID;

public class TrackedEntity implements Trackable {

    private final UUID entityId;
    private final NbtPathArgument.NbtPath path;

    public TrackedEntity(Entity entity, NbtPathArgument.NbtPath path) {
        this.entityId = entity.getUUID();
        this.path = path;
    }

    @Override
    public boolean canTrack(MinecraftServer server) {
        return getEntity(server) != null;
    }

    @Override
    public Component getTitle(MinecraftServer server) {
        return getEntity(server).getDisplayName();
    }

    @Override
    public Component getValue(MinecraftServer server) {
        return OurComponentUtils.getPrettyPrintedTag(NbtPredicate.getEntityTagToCompare(getEntity(server)), path);
    }

    @Override
    public String getId() {
        return "entity:" + entityId;
    }

    private Entity getEntity(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(entityId);
            if (entity != null)
                return entity;
        }

        return null;
    }
}
