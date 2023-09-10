package xyz.trivaxy.datamancer.profile;

import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.commands.CommandFunction;

import java.util.ArrayDeque;
import java.util.Deque;

public class FunctionProfiler {

    private Object2ObjectMap<CommandFunction, PerformanceEntry> performances = new Object2ObjectOpenHashMap<>();
    private Object2ObjectMap<CommandFunction, Stopwatch> stopwatches = new Object2ObjectOpenHashMap<>();
    private Deque<CommandFunction> watchStack = new ArrayDeque<>();
    private Object2IntMap<CommandFunction> callsInChain = new Object2IntOpenHashMap<>();
    private boolean enabled = false;
    private static final FunctionProfiler INSTANCE = new FunctionProfiler();

    public void restart() {
        performances.clear();
        stopwatches.clear();
        watchStack.clear();
        callsInChain.clear();
    }

    public void pushWatch(CommandFunction function) {
        performances.putIfAbsent(function, PerformanceEntry.empty(function));
        watchStack.push(function);
        callsInChain.put(function, callsInChain.getOrDefault(function, 0) + 1);
        stopwatches.putIfAbsent(function, Stopwatch.createStarted());
    }

    public void popWatch() {
        CommandFunction function = watchStack.pop();
        Stopwatch stopwatch = stopwatches.get(function);

        long time = stopwatch.elapsed().toMillis();
        PerformanceEntry entry = performances.get(function);
        int callCount = callsInChain.getInt(function);

        if (callCount > 1)
            entry.accountForRecursive(time);
        else {
            entry.accountFor(time);
            stopwatches.remove(function);
        }

        callsInChain.put(function, callCount - 1);
    }

    public int watchCount() {
        return performances.size();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        enabled = true;
        resume();
    }

    public void disable() {
        enabled = false;
        pause();
    }

    public void pause() {
        for (Stopwatch stopwatch : stopwatches.values())
            stopwatch.stop();
    }

    public void resume() {
        if (!enabled)
            return;

        for (Stopwatch stopwatch : stopwatches.values())
            stopwatch.start();
    }

    public FunctionReport getReport() {
        return new FunctionReport(performances.values());
    }

    public static FunctionProfiler getInstance() {
        return INSTANCE;
    }
}
