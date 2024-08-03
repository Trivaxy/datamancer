package xyz.trivaxy.datamancer.profile;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import net.minecraft.resources.ResourceLocation;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class FunctionReport {

    private final List<PerformanceEntry> entries;
    private final int overflowCount;
    private final List<ResourceLocation> overflowStacktrace;
    public static final Path OUTPUT_PATH = Paths.get("datamancer", "function_report.txt");

    public FunctionReport(Collection<PerformanceEntry> entries, int overflowCount, List<ResourceLocation> overflowStacktrace) {
        this.entries = entries.stream().toList();
        this.overflowCount = overflowCount;
        this.overflowStacktrace = overflowStacktrace;
    }

    public String constructTable() {
        return AsciiTable.getTable(
            AsciiTable.FANCY_ASCII,
            entries,
            Arrays.stream(FunctionReportColumn.values())
                .map(column -> new Column().header(column.getColumnHeaderName()).dataAlign(column.getAlignment()).with(column::getValueInEntry))
                .toList()
        );
    }

    public String getReport() {
        StringBuilder builder = new StringBuilder();

        if (overflowCount != 0) {
            builder.append("Note: ").append(overflowCount).append(" overflow(s) were detected\n\n");
            builder.append("Last overflow stacktrace:\n");

            Map<ResourceLocation, Integer> stackTraces = overflowStacktrace.stream()
                .collect(Collectors.groupingBy(s -> s, LinkedHashMap::new, Collectors.summingInt(e -> 1)));

            int indent = 2;
            for (Map.Entry<ResourceLocation, Integer> entry : stackTraces.entrySet()) {
                builder.append(" ".repeat(indent)).append("- ").append(entry.getKey());

                if (entry.getValue() > 1)
                    builder.append(" (").append(entry.getValue()).append(")");

                builder.append("\n");
                indent += 2;
            }
        }

        builder.append("\n");

        if (overflowCount != 0)
            builder.append("Caution: the presence of overflow(s) can drastically affect the accuracy of the report\n\n");

        builder.append(constructTable());

        return builder.toString();
    }

    public void writeToFile() throws IOException {
        Files.createDirectories(OUTPUT_PATH.getParent());

        try (FileWriter writer = new FileWriter(OUTPUT_PATH.toFile())) {
            writer.write(getReport());
        }
    }
}
