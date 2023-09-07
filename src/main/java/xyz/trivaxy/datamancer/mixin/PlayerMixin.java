package xyz.trivaxy.datamancer.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.trivaxy.datamancer.access.MarkerListenerAccess;

@Mixin(Player.class)
public class PlayerMixin implements MarkerListenerAccess {

    @Unique
    private boolean listeningForMarkers = false;

    @Unique
    public boolean isListeningForMarkers() {
        return listeningForMarkers;
    }

    @Unique
    public void setListeningForMarkers(boolean listeningForMarkers) {
        this.listeningForMarkers = listeningForMarkers;
    }
}
