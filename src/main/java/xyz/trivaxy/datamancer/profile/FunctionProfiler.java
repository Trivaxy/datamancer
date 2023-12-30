package xyz.trivaxy.datamancer.profile;

import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class FunctionProfiler {

    private final HashMap<ResourceLocation, PerformanceEntry> performances = new HashMap<>();
    private final Stopwatch stopwatch = Stopwatch.createStarted(); // it's fine for this to run forever (unless you live 584 years)
    private final Deque<ResourceLocation> functionStack = new ArrayDeque<>();
    private final LongArrayFIFOQueue timestampStack = new LongArrayFIFOQueue();
    private boolean enabled = false;
    private static final FunctionProfiler INSTANCE = new FunctionProfiler();

    public void restart() {
        performances.clear();
        functionStack.clear();
        timestampStack.clear();
    }

    public void pushWatch(ResourceLocation functionId) {
        performances.computeIfAbsent(functionId, PerformanceEntry::new);
        functionStack.push(functionId);
        timestampStack.enqueue(stopwatch.elapsed(TimeUnit.MICROSECONDS));
    }

    public void popWatch() {
        ResourceLocation functionId = functionStack.pop();
        long profiledTime = stopwatch.elapsed(TimeUnit.MICROSECONDS) - timestampStack.dequeueLastLong();
        performances.get(functionId).record(profiledTime);
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
        stopwatch.reset();
    }

    public void pause() {
        if (!enabled)
            return;

        if (stopwatch.isRunning())
            stopwatch.stop();
    }

    public void resume() {
        if (!enabled)
            return;

        if (!stopwatch.isRunning())
            stopwatch.start();
    }

    public FunctionReport getReport() {
        return new FunctionReport(performances.values());
    }

    public static FunctionProfiler getInstance() {
        return INSTANCE;
    }
}
