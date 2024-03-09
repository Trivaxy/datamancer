package xyz.trivaxy.datamancer.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

// Named as OurComponentUtils to avoid conflict with Minecraft's ComponentUtils class
public class OurComponentUtils {
    public static Component getPrettyPrintedTag(Tag tag, NbtPathArgument.NbtPath path) {
        if (path == null)
            return NbtUtils.toPrettyComponent(tag);

        List<Tag> nbtInPath;

        try {
            nbtInPath = path.get(tag);
        } catch (CommandSyntaxException e) {
            return Component.literal("None").withStyle(ChatFormatting.RED);
        }

        return nbtInPath
            .stream()
            .map(NbtUtils::toPrettyComponent)
            .reduce(Component.empty(), (acc, c) -> ((MutableComponent)acc).append(" ").append(c));
    }

    public static Component joinComponents(List<Component> components, String separator) {
        MutableComponent result = Component.empty();

        for (int i = 0; i < components.size(); i++) {
            result.append(components.get(i));

            if (i < components.size() - 1)
                result.append(separator);
        }

        return result;
    }

    public static Component prettyPrintedCoordinates(float x, float y, float z) {
        return Component
            .literal(String.valueOf(x)).withStyle(ChatFormatting.GOLD)
            .append(Component.literal(", ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(String.valueOf(y)).withStyle(ChatFormatting.GOLD))
            .append(Component.literal(", ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(String.valueOf(z)).withStyle(ChatFormatting.GOLD));

    }

    public static Component prettyPrintedCoordinates(BlockPos pos) {
        return prettyPrintedCoordinates(pos.getX(), pos.getY(), pos.getZ());
    }
}
