package xyz.trivaxy.datamancer;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import xyz.trivaxy.datamancer.tracker.TrackerList;
import xyz.trivaxy.datamancer.watch.DataPackWatcher;

public class Attachments {

    public static final AttachmentType<DataPackWatcher.State> WATCHER_STATE_ATTACHMENT = AttachmentRegistry.createPersistent(
        Datamancer.in("watcher_state"),
        DataPackWatcher.State.CODEC
    );

    public static final AttachmentType<TrackerList> PLAYER_TRACKER_LIST_ATTACHMENT = AttachmentRegistry.<TrackerList>builder()
        .persistent(Codec.STRING.listOf().xmap(TrackerList::new, TrackerList::getTemplates))
        .copyOnDeath()
        .initializer(() -> new TrackerList())
        .buildAndRegister(Datamancer.in("player_tracker_list"));
}
