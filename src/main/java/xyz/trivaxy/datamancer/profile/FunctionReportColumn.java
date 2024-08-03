package xyz.trivaxy.datamancer.profile;

import com.github.freva.asciitable.HorizontalAlign;

import java.util.Comparator;
import java.util.function.Function;

public enum FunctionReportColumn {
    NAME("Function", HorizontalAlign.RIGHT, entry -> entry.getFunctionId().toString(), Comparator.comparing(PerformanceEntry::getFunctionId)),
    MEAN("Mean (μs)", HorizontalAlign.LEFT, entry -> String.format("%.5f", entry.calculateMean()), Comparator.comparingDouble(PerformanceEntry::calculateMean)),
    SD("Standard Deviation (μs)", HorizontalAlign.LEFT, entry -> String.format("%.5f", entry.calculateStandardDeviation()), Comparator.comparingDouble(PerformanceEntry::calculateStandardDeviation)),
    MIN("Min (μs)", HorizontalAlign.LEFT, entry -> String.format("%.5f", entry.findMin()), Comparator.comparingDouble(PerformanceEntry::findMin)),
    MAX("Max (μs)", HorizontalAlign.LEFT, entry -> String.format("%.5f", entry.findMax()), Comparator.comparingDouble(PerformanceEntry::findMax)),
    ITERATIONS("Iterations", HorizontalAlign.LEFT, entry -> String.valueOf(entry.getTotalExecutionCount()), Comparator.comparing(PerformanceEntry::getTotalExecutionCount)),;

    private final String name;
    private final HorizontalAlign alignment;
    private final Function<PerformanceEntry, String> valueRetriever;
    private final Comparator<PerformanceEntry> comparator;

    FunctionReportColumn(String name, HorizontalAlign alignment, Function<PerformanceEntry, String> valueRetriever, Comparator<PerformanceEntry> comparator) {
        this.name = name;
        this.alignment = alignment;
        this.valueRetriever = valueRetriever;
        this.comparator = comparator;
    }

    public String getColumnHeaderName() {
        return name;
    }

    public HorizontalAlign getAlignment() {
        return alignment;
    }

    public String getValueInEntry(PerformanceEntry entry) {
        return valueRetriever.apply(entry);
    }

    public Comparator<PerformanceEntry> getComparator() {
        return comparator;
    }
}
