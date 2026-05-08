package sorting.benchmark;
import sorting.serial.SerialSorting;
import sorting.parallel.ParallelSorting;
import java.io.*;
import java.util.*;

public class BenchmarkRunner {

    static final int SAMPLES    = 5;
    //static final int[] SIZES    = {1_000, 10_000, 50_000, 100_000, 500_000};
    static final int[] SIZES = {1_000, 10_000, 50_000};
    static final int[] THREADS  = {2, 4, 8};
    static final String[] ALGOS = {"MergeSort", "QuickSort", "BubbleSort", "CountingSort"};

    private final Random rng = new Random(42);
    private final List<String[]> csvRows = new ArrayList<>();

    public void run() throws Exception {
        new File("csv").mkdirs(); // cria a pasta csv
        csvRows.add(new String[]{"Algorithm","Mode","Threads","Size","Sample","TimeMs"});

        for (int size : SIZES) {
            System.out.printf("=== Tamanho: %,d ===%n", size);
            for (String algo : ALGOS) {
                // Serial
                for (int s = 1; s <= SAMPLES; s++) {
                    int[] arr = randomArray(size);
                    long ms = measureSerial(algo, arr);
                    csvRows.add(new String[]{algo, "Serial", "1",
                                             String.valueOf(size), String.valueOf(s), String.valueOf(ms)});
                    System.out.printf("  %s Serial      size=%,7d sample=%d -> %dms%n",algo,size,s,ms);
                }
                // Paralelo
                for (int t : THREADS) {
                    for (int s = 1; s <= SAMPLES; s++) {
                        int[] arr = randomArray(size);
                        long ms = measureParallel(algo, arr, t);
                        csvRows.add(new String[]{algo, "Parallel", String.valueOf(t),
                                                 String.valueOf(size), String.valueOf(s), String.valueOf(ms)});
                        System.out.printf("  %s Parallel t=%d size=%,7d sample=%d -> %dms%n",algo,t,size,s,ms);
                    }
                }
            }
        }
        writeCSV("csv/results.csv");
        System.out.println("\n[OK!] Resultados salvos em csv/results.csv");
    }

    private long measureSerial(String algo, int[] arr) throws Exception {
        long start = System.currentTimeMillis();
        switch (algo) {
            case "MergeSort"    -> SerialSorting.mergeSort(arr, 0, arr.length - 1);
            case "QuickSort"    -> SerialSorting.quickSort(arr, 0, arr.length - 1);
            case "BubbleSort"   -> SerialSorting.bubbleSort(arr);
            case "CountingSort" -> SerialSorting.countingSort(arr);
        }
        return System.currentTimeMillis() - start;
    }

    private long measureParallel(String algo, int[] arr, int threads) throws Exception {
        long start = System.currentTimeMillis();
        switch (algo) {
            case "MergeSort"    -> ParallelSorting.mergeSort(arr, threads);
            case "QuickSort"    -> ParallelSorting.quickSort(arr, threads);
            case "BubbleSort"   -> ParallelSorting.bubbleSort(arr, threads);
            case "CountingSort" -> ParallelSorting.countingSort(arr, threads);
        }
        return System.currentTimeMillis() - start;
    }

    private int[] randomArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = rng.nextInt(1_000_000);
        return arr;
    }

    private void writeCSV(String path) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            for (String[] row : csvRows)
                pw.println(String.join(",", row));
        }
    }
}