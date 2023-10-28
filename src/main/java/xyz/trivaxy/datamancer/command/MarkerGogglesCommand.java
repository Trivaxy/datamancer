package xyz.trivaxy.datamancer.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import xyz.trivaxy.datamancer.access.MarkerListenerAccess;
import xyz.trivaxy.datamancer.MarkerInfoHandler;

import static net.minecraft.commands.Commands.literal;

public class MarkerGogglesCommand extends DatamancerCommand {

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
       dispatcher.register(literal("markergoggles")
                .requires(source -> source.hasPermission(2))
                .executes(this::execute)
       );

       // we love brigadier bugs... can't use redirect here
       dispatcher.register(literal("mg")
                .requires(source -> source.hasPermission(2))
                .executes(this::execute)
       );
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            replyFailure(context.getSource(), Component.literal("This command can only be executed by a player"));
            return 0;
        }

        ServerPlayer player = context.getSource().getPlayer();
        MarkerListenerAccess listener = (MarkerListenerAccess) player;

        if (listener.isListeningForMarkers()) {
            listener.setListeningForMarkers(false);
            ServerPlayNetworking.send(player, MarkerInfoHandler.MARKER_GOGGLES_OFF, PacketByteBufs.empty());
            replySuccess(context.getSource(), Component.literal("Disabled marker goggles"));
        } else {
            listener.setListeningForMarkers(true);
            replySuccess(context.getSource(), Component.literal("Enabled marker goggles"));
        }

        return 0;
    }
}
