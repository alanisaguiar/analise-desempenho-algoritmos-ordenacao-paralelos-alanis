package sorting.benchmark;
import java.io.*;
import java.util.*;
import java.util.stream.*;

public class AnalyzeCSV {

    record Row(String algo, String mode, int threads, int size, double ms){}

    public static void main(String[] args) throws Exception {
        List<Row> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("csv/results.csv"))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                rows.add(new Row(p[0], p[1], Integer.parseInt(p[2]),
                                 Integer.parseInt(p[3]), Double.parseDouble(p[5])));
            }
        }

        Map<String, DoubleSummaryStatistics> stats = rows.stream()
            .collect(Collectors.groupingBy(
                r -> r.algo() + "|" + r.mode() + "|" + r.threads() + "|" + r.size(),
                Collectors.summarizingDouble(Row::ms)));

        System.out.printf("%-14s %-10s %7s %8s %10s%n",
            "Algorithm","Mode","Threads","Size","Avg(ms)");
        stats.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> {
                var s = e.getValue();
                System.out.printf("%-14s %-10s %7s %8s %10.1f%n",
                    e.getKey().split("\\|")[0],
                    e.getKey().split("\\|")[1],
                    e.getKey().split("\\|")[2],
                    e.getKey().split("\\|")[3],
                    s.getAverage());
            });
    }
}