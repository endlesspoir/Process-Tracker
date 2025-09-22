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

    public static XYChart.Series<Number, Number> measureThreads(double[] a, int K, int deltaThreads, int maxThreads) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Зависимость от потоков");

        for (int t = 1; t <= maxThreads; t += deltaThreads) {
            ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(t);
            long start = System.nanoTime();
            multiThreadCalc(a, K, t, pool);
            long end = System.nanoTime();
            pool.shutdown();

            long ms = (end - start) / 1_000_000;
            series.getData().add(new XYChart.Data<>(t, ms));
        }
        return series;
    }

    public static XYChart.Series<Number, Number> measureK(double[] a, int baseK, int deltaK, int threads) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Зависимость от K");

        for (int k = deltaK; k <= baseK * 2; k += deltaK) {
            ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(threads);
            long start = System.nanoTime();
            multiThreadCalc(a, k, threads, pool);
            long end = System.nanoTime();
            pool.shutdown();

            long ms = (end - start) / 1_000_000;
            series.getData().add(new XYChart.Data<>(k, ms));
        }
        return series;
    }
}
