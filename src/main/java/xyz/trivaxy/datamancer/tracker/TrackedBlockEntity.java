package xyz.trivaxy.datamancer.tracker;

import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import xyz.trivaxy.datamancer.util.OurComponentUtils;

public class TrackedBlockEntity implements Trackable {

    private final BlockPos pos;
    private final ResourceKey<Level> level;
    private final NbtPathArgument.NbtPath path;

    public TrackedBlockEntity(BlockPos pos, ResourceKey<Level> level, NbtPathArgument.NbtPath path) {
        this.pos = pos;
        this.level = level;
        this.path = path;
    }

    @Override
    public boolean canTrack(MinecraftServer server) {
        return getBlockEntity(server) != null;
    }

    @Override
    public Component getTitle(MinecraftServer server) {
        return OurComponentUtils.prettyPrintedCoordinates(pos);
    }

    @Override
    public Component getValue(MinecraftServer server) {
        return OurComponentUtils.getPrettyPrintedTag(new BlockDataAccessor(getBlockEntity(server), pos).getData(), path);
    }

    private BlockEntity getBlockEntity(MinecraftServer server) {
        return server.getLevel(level).getBlockEntity(pos);
    }
}
