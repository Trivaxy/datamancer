package xyz.trivaxy.datamancer.command.placeholder;

@SuppressWarnings("unchecked")
public class Arguments {

    private final Object[] arguments;

    public Arguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public <T> T get(int index) {
        return (T) arguments[index];
    }
}
