package xyz.trivaxy.datamancer.profile;

import net.minecraft.resources.ResourceLocation;
import xyz.trivaxy.datamancer.util.LongRingBuffer;

public final class PerformanceEntry {

    private final ResourceLocation functionId;
    private final LongRingBuffer data = new LongRingBuffer(50);
    private long totalExecutionCount = 0;

    public PerformanceEntry(ResourceLocation functionId) {
        this.functionId = functionId;
    }

    public ResourceLocation getFunctionId() {
        return functionId;
    }

    public void record(long executionTime) {
        data.add(executionTime);
        totalExecutionCount++;
    }

    public void reset() {
        data.clear();
    }

    public long getTotalExecutionCount() {
        return totalExecutionCount;
    }

    public double calculateMean() {
        double sum = 0;

        for (long value : data) {
            sum += value;
        }

        return sum / data.size();
    }

    public double calculateStandardDeviation() {
        double mean = calculateMean();
        double sum = 0;

        for (long value : data) {
            sum += Math.pow(value - mean, 2);
        }

        return Math.sqrt(sum / data.size());
    }

    public double findMin() {
        double min = Double.MAX_VALUE;

        for (long value : data) {
            min = Math.min(min, value);
        }

        return min;
    }

    public double findMax() {
        double max = Double.MIN_VALUE;

        for (long value : data) {
            max = Math.max(max, value);
        }

        return max;
    }
}
