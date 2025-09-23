package org.example.Lab2;

import javafx.scene.chart.XYChart;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class CalculationTask {

    public static double[] generateArray(int n) {
        Random rand = new Random();
        double[] arr = new double[n];
        for (int i = 0; i < n; i++) {
            arr[i] = rand.nextDouble() * 100;
        }
        return arr;
    }

    public static double[] singleThreadCalc(double[] a, int K) {
        double[] b = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < K; j++) {
                b[i] += Math.pow(a[i], 1.789);
            }
        }
        return b;
    }

    public static double[] multiThreadCalc(double[] a, int K, int threads, ExecutorService pool) {
        double[] b = new double[a.length];
        int chunk = (int) Math.ceil(a.length / (double) threads);

        try {
            Future<?>[] futures = new Future[threads];
            for (int t = 0; t < threads; t++) {
                final int start = t * chunk;
                final int end = Math.min(a.length, (t + 1) * chunk);

                futures[t] = pool.submit(() -> {
                    for (int i = start; i < end; i++) {
                        for (int j = 0; j < K; j++) {
                            b[i] += Math.pow(a[i], 1.789);
                        }
                    }
                });
            }
            for (Future<?> f : futures) f.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }

    // возвращает массив из двух серий: [0] - однопоточно, [1] - многопоточно
    public static XYChart.Series<Number, Number>[] measureThreads(double[] a, int K, int deltaThreads, int maxThreads) {
        @SuppressWarnings("unchecked")
        XYChart.Series<Number, Number>[] result = new XYChart.Series[2];

        XYChart.Series<Number, Number> singleSeries = new XYChart.Series<>();
        singleSeries.setName("Однопоточно");

        XYChart.Series<Number, Number> multiSeries = new XYChart.Series<>();
        multiSeries.setName("Многопоточно");

        // Для честности измерений мы используем один и тот же массив a (только чтение).
        // Если вычисления изменяют исходные данные — нужно копию, но в нашем случае a не меняется.
        for (int t = 1; t <= maxThreads; t += Math.max(1, deltaThreads)) {
            // Однопоточно (время выполнения полного прохода)
            long startSingle = System.nanoTime();
            singleThreadCalc(a, K);
            long endSingle = System.nanoTime();
            long msSingle = (endSingle - startSingle) / 1_000_000;
            singleSeries.getData().add(new XYChart.Data<>(t, msSingle));

            // Многопоточно (t потоков)
            ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(t);
            long startMulti = System.nanoTime();
            multiThreadCalc(a, K, t, pool);
            long endMulti = System.nanoTime();
            pool.shutdown();
            long msMulti = (endMulti - startMulti) / 1_000_000;
            multiSeries.getData().add(new XYChart.Data<>(t, msMulti));
        }

        result[0] = singleSeries;
        result[1] = multiSeries;
        return result;
    }

    public static XYChart.Series<Number, Number>[] measureK(double[] a, int baseK, int deltaK, int threads) {
        @SuppressWarnings("unchecked")
        XYChart.Series<Number, Number>[] result = new XYChart.Series[2];

        XYChart.Series<Number, Number> singleSeries = new XYChart.Series<>();
        singleSeries.setName("Однопоточно");

        XYChart.Series<Number, Number> multiSeries = new XYChart.Series<>();
        multiSeries.setName("Многопоточно");

        // Начнём с K = deltaK (или 1 если deltaK==0), пробегая до baseK*2 (как у тебя было)
        int step = Math.max(1, deltaK);
        int maxK = Math.max(baseK, 1) * 2;
        for (int k = step; k <= maxK; k += step) {
            // Однопоточно
            long startSingle = System.nanoTime();
            singleThreadCalc(a, k);
            long endSingle = System.nanoTime();
            long msSingle = (endSingle - startSingle) / 1_000_000;
            singleSeries.getData().add(new XYChart.Data<>(k, msSingle));

            // Многопоточно (фиксированное число потоков = threads)
            ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(Math.max(1, threads));
            long startMulti = System.nanoTime();
            multiThreadCalc(a, k, Math.max(1, threads), pool);
            long endMulti = System.nanoTime();
            pool.shutdown();
            long msMulti = (endMulti - startMulti) / 1_000_000;
            multiSeries.getData().add(new XYChart.Data<>(k, msMulti));
        }

        result[0] = singleSeries;
        result[1] = multiSeries;
        return result;
    }

}
