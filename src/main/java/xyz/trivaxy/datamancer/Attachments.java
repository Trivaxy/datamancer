package xyz.trivaxy.datamancer;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import xyz.trivaxy.datamancer.watch.DataPackWatcher;

public class Attachments {

    public static final AttachmentType<DataPackWatcher.State> WATCHER_STATE_ATTACHMENT = AttachmentRegistry.createPersistent(
            Datamancer.in("watcher_state"),
            DataPackWatcher.State.CODEC
    );
}
