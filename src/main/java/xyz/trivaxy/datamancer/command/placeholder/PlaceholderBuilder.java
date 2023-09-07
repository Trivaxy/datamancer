package xyz.trivaxy.datamancer.command.placeholder;

import com.mojang.brigadier.arguments.ArgumentType;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderBuilder {

    private final List<ArgumentType<?>> argumentTypes = new ArrayList<>();
    private int optionals = 0;

    public PlaceholderBuilder argument(ArgumentType<?> argumentType) {
        if (optionals > 0)
            return optional(argumentType);

        argumentTypes.add(argumentType);
        return this;
    }

    public PlaceholderBuilder optional(ArgumentType<?> argumentType) {
        optionals++;
        argumentTypes.add(argumentType);
        return this;
    }

    public Placeholder process(PlaceholderProcessor processor) {
        return new Placeholder(argumentTypes, optionals, processor);
    }
}
