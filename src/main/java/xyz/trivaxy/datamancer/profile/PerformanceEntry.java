package xyz.trivaxy.datamancer.profile;

import net.minecraft.commands.CommandFunction;

public final class PerformanceEntry {

    private long totalExecutionTime;
    private long totalExecutionCount;
    private final CommandFunction function;

    private PerformanceEntry(long executionTime, long executionCount, CommandFunction function) {
        totalExecutionTime = executionTime;
        totalExecutionCount = executionCount;
        this.function = function;
    }

    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public long getTotalExecutionCount() {
        return totalExecutionCount;
    }

    public CommandFunction getFunction() {
        return function;
    }

    public void accountFor(long executionTime) {
        totalExecutionTime += executionTime;
        totalExecutionCount++;
    }

    public void accountForRecursive(long executionTime) {
        totalExecutionTime += executionTime;
    }

    public double getAverageExecutionTime() {
        return totalExecutionTime / (double) totalExecutionCount;
    }

    public void reset() {
        totalExecutionTime = 0;
        totalExecutionCount = 0;
    }

    public static PerformanceEntry empty(CommandFunction function) {
        return new PerformanceEntry(0, 0, function);
    }
}
