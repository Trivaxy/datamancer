package xyz.trivaxy.datamancer.profile;

import com.github.freva.asciitable.HorizontalAlign;

import java.util.function.Function;

public enum FunctionReportColumn {
    NAME("Function", HorizontalAlign.RIGHT, entry -> entry.getFunctionId().toString()),
    MEAN("Mean (μs)", HorizontalAlign.LEFT, entry -> String.format("%.5f", entry.calculateMean())),
    STANDARD_DEVIATION("Standard Deviation (μs)", HorizontalAlign.LEFT, entry -> String.format("%.5f", entry.calculateStandardDeviation())),
    MIN("Min (μs)", HorizontalAlign.LEFT, entry -> String.format("%.5f", entry.findMin())),
    MAX("Max (μs)", HorizontalAlign.LEFT, entry -> String.format("%.5f", entry.findMax())),
    ITERATIONS("Iterations", HorizontalAlign.LEFT, entry -> String.valueOf(entry.getTotalExecutionCount()));

    private final String name;
    private final HorizontalAlign alignment;
    private final Function<PerformanceEntry, String> valueRetriever;

    FunctionReportColumn(String name, HorizontalAlign alignment, Function<PerformanceEntry, String> valueRetriever) {
        this.name = name;
        this.alignment = alignment;
        this.valueRetriever = valueRetriever;
    }

    public String getName() {
        return name;
    }

    public HorizontalAlign getAlignment() {
        return alignment;
    }

    public String getValueInEntry(PerformanceEntry entry) {
        return valueRetriever.apply(entry);
    }
}
