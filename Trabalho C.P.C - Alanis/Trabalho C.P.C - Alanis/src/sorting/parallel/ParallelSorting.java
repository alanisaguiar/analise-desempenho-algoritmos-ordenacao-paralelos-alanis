package sorting.parallel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import sorting.serial.SerialSorting;

public class ParallelSorting {
    private static final int THRESHOLD = 8_192;

    // MergeSort Paralelo
    public static void mergeSort(int[] arr, int threads) {
        ForkJoinPool pool = new ForkJoinPool(threads);
        pool.invoke(new MergeSortTask(arr, 0, arr.length - 1));
        pool.shutdown();
    }

    static class MergeSortTask extends RecursiveAction {
        private final int[] arr; private final int l, r;
        MergeSortTask(int[] arr, int l, int r) { this.arr=arr; this.l=l; this.r=r; }
        @Override protected void compute() {
            if (r - l <= THRESHOLD) { SerialSorting.mergeSort(arr, l, r); return; }
            int mid = (l + r) / 2;
            MergeSortTask left  = new MergeSortTask(arr, l, mid);
            MergeSortTask right = new MergeSortTask(arr, mid + 1, r);
            invokeAll(left, right);
            SerialSorting.merge(arr, l, mid, r);
        }
    }

    // Quick Sort Paralelo
    public static void quickSort(int[] arr, int threads) {
        ForkJoinPool pool = new ForkJoinPool(threads);
        pool.invoke(new QuickSortTask(arr, 0, arr.length - 1));
        pool.shutdown();
    }

    static class QuickSortTask extends RecursiveAction {
        private final int[] arr; private final int low, high;
        QuickSortTask(int[] arr, int low, int high) { this.arr=arr; this.low=low; this.high=high; }
        @Override protected void compute() {
            if (high - low <= THRESHOLD) { SerialSorting.quickSort(arr, low, high); return; }
            int pi = SerialSorting.partition(arr, low, high);
            QuickSortTask left  = new QuickSortTask(arr, low, pi - 1);
            QuickSortTask right = new QuickSortTask(arr, pi + 1, high);
            invokeAll(left, right);
        }
    }

    // Bubble Sort Paralelo
    public static void bubbleSort(int[] arr, int threads) throws Exception {
        int n = arr.length;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int phase = 0; phase < n; phase++) {
            final int p = phase;
            List<Future<?>> futures = new ArrayList<>();   
            int chunkSize = Math.max(1, n / threads);
            for (int t = 0; t < threads; t++) {
                final int start = t * chunkSize;
                final int end   = (t == threads - 1) ? n - 1 : start + chunkSize - 1;
                futures.add(pool.submit(() -> {            
                    int from = (p % 2 == 0) ? start + (start % 2) : start + (1 - start % 2);
                    for (int j = from; j < end; j += 2) {
                        if (arr[j] > arr[j+1]) { int tmp=arr[j]; arr[j]=arr[j+1]; arr[j+1]=tmp; }
                    }
                }));
            }
            for (Future<?> f : futures) f.get();           
        }
        pool.shutdown();
    }

    // Counting Sort Paralelo
    public static void countingSort(int[] arr, int threads) throws Exception {
        if (arr.length == 0) return;
        int max = Arrays.stream(arr).parallel().max().getAsInt();
        int min = Arrays.stream(arr).parallel().min().getAsInt();
        int range = max - min + 1;
        int[][] localCounts = new int[threads][range];
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        int chunk = arr.length / threads;
        List<Future<?>> futures = new ArrayList<>();       
        for (int t = 0; t < threads; t++) {
            final int tid = t, s = t * chunk;
            final int e = (t == threads - 1) ? arr.length : s + chunk;
            futures.add(pool.submit(() -> {                
                for (int i = s; i < e; i++) localCounts[tid][arr[i] - min]++;
            }));
        }
        for (Future<?> f : futures) f.get();               
        int[] count = new int[range];
        for (int[] lc : localCounts) for (int i = 0; i < range; i++) count[i] += lc[i];
        for (int i = 1; i < range; i++) count[i] += count[i-1];
        int[] output = new int[arr.length];
        for (int i = arr.length - 1; i >= 0; i--)
            output[--count[arr[i] - min]] = arr[i];
        System.arraycopy(output, 0, arr, 0, arr.length);
        pool.shutdown();
    }
}