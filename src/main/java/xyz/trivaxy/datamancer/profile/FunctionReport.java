package xyz.trivaxy.datamancer.profile;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class FunctionReport {

    private final List<PerformanceEntry> entries;
    public static final Path OUTPUT_PATH = Paths.get("datamancer", "function_report.txt");

    public FunctionReport(Collection<PerformanceEntry> entries) {
        this.entries = entries.stream().sorted(Comparator.comparingDouble(PerformanceEntry::getAverageExecutionTime)).collect(Collectors.toList());
    }

    public void writeToFile() throws IOException {
        Files.createDirectories(OUTPUT_PATH.getParent());

        Formatter formatter = new Formatter(new FileWriter(OUTPUT_PATH.toFile()));
        formatter.format("%-30s %-6s %-13s\n", "Function", "iter", "time(μs)");

        for (PerformanceEntry entry : entries) {
            formatter.format("%-30s %-6d %-13f\n", entry.getFunction().getId(), entry.getTotalExecutionCount(), entry.getAverageExecutionTime());
        }

        formatter.flush();
        formatter.close();
    }
}
