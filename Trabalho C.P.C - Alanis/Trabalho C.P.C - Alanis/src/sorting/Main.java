package sorting;
import sorting.benchmark.BenchmarkRunner;
import sorting.view.ChartViewer;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Benchmark de Algoritmos de Ordenacao Paralela ===\n");

        BenchmarkRunner runner = new BenchmarkRunner();
        runner.run();

        System.out.println("\nAbrindo visualizador de graficos...");
        ChartViewer.launch();
    }
}