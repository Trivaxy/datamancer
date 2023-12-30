package xyz.trivaxy.datamancer.profile;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FunctionReport {

    private final List<PerformanceEntry> entries;
    public static final Path OUTPUT_PATH = Paths.get("datamancer", "function_report.txt");

    public FunctionReport(Collection<PerformanceEntry> entries) {
        this.entries = entries.stream().toList();
    }

    public String constructTable() {
        return AsciiTable.getTable(
                AsciiTable.FANCY_ASCII,
                entries,
                Arrays.asList(
                        new Column().header("Function").with(entry -> entry.getFunctionId().toString()),
                        new Column().header("Mean").with(entry -> String.format("%.5f", entry.calculateMean())),
                        new Column().header("SD").with(entry -> String.format("%.5f", entry.calculateStandardDeviation())),
                        new Column().header("Min").with(entry -> String.format("%.5f", entry.findMin())),
                        new Column().header("Max").with(entry -> String.format("%.5f", entry.findMax())),
                        new Column().header("Iterations").with(entry -> String.valueOf(entry.getTotalExecutionCount()))
                )
        );
    }

    public void writeToFile() throws IOException {
        Files.createDirectories(OUTPUT_PATH.getParent());

        try (FileWriter writer = new FileWriter(OUTPUT_PATH.toFile())) {
            writer.write(constructTable());
        }
    }
}
