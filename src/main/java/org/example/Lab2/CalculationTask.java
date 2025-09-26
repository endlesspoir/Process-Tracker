package org.example.Lab2;

import javafx.scene.chart.XYChart;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Утилитарный класс для генерации массива случайных чисел и проведения вычислительных экспериментов
 * (сравнение времени выполнения однопоточных и многопоточных вычислений).
 * Используется для построения графиков зависимости времени выполнения от количества потоков или параметра K.
 */
public class CalculationTask {

    /**
     * Генерация массива случайных чисел от 0 до 100.
     *
     * @param n размер массива
     * @return массив double длиной n
     */
    public static double[] generateArray(int n) {
        Random rand = new Random();
        double[] arr = new double[n];
        for (int i = 0; i < n; i++) {
            arr[i] = rand.nextDouble() * 100;
        }
        return arr;
    }

    /**
     * Однопоточные вычисления.
     * Для каждого элемента массива выполняется K повторений возведения в степень 1.789
     * и суммирования результата.
     *
     * @param a массив входных чисел
     * @param K количество повторений операции
     * @return массив результатов той же длины
     */
    public static double[] singleThreadCalc(double[] a, int K) {
        double[] b = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < K; j++) {
                b[i] += Math.pow(a[i], 1.789);
            }
        }
        return b;
    }

    /**
     * Многопоточные вычисления.
     * Массив делится на равные части (chunk) и обрабатывается указанным количеством потоков.
     *
     * @param a       массив входных чисел
     * @param K       количество повторений операции
     * @param threads количество потоков
     * @param pool    пул потоков (ExecutorService)
     * @return массив результатов той же длины
     */
    public static double[] multiThreadCalc(double[] a, int K, int threads, ExecutorService pool) {
        double[] b = new double[a.length];
        int chunk = (int) Math.ceil(a.length / (double) threads);

        try {
            Future<?>[] futures = new Future[threads];
            for (int t = 0; t < threads; t++) {
                final int start = t * chunk;
                final int end = Math.min(a.length, (t + 1) * chunk);

                // Каждому потоку достаётся свой диапазон элементов массива
                futures[t] = pool.submit(() -> {
                    for (int i = start; i < end; i++) {
                        for (int j = 0; j < K; j++) {
                            b[i] += Math.pow(a[i], 1.789);
                        }
                    }
                });
            }
            // Дожидаемся завершения всех потоков
            for (Future<?> f : futures) f.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * Измерение времени выполнения при разном числе потоков.
     * Возвращает массив из двух серий данных для графика: [0] — однопоточные, [1] — многопоточные.
     * По оси X откладывается количество потоков, по оси Y — время выполнения в миллисекундах.
     */
    public static XYChart.Series<Number, Number>[] measureThreads(double[] a, int K, int deltaThreads, int maxThreads) {
        @SuppressWarnings("unchecked")
        XYChart.Series<Number, Number>[] result = new XYChart.Series[2];

        XYChart.Series<Number, Number> singleSeries = new XYChart.Series<>();
        singleSeries.setName("Однопоточно");

        XYChart.Series<Number, Number> multiSeries = new XYChart.Series<>();
        multiSeries.setName("Многопоточно");

        // Перебираем количество потоков от 1 до maxThreads, с шагом deltaThreads (минимум 1)
        for (int t = 1; t <= maxThreads; t += Math.max(1, deltaThreads)) {
            // Измеряем время однопоточного выполнения
            long startSingle = System.nanoTime();
            singleThreadCalc(a, K);
            long endSingle = System.nanoTime();
            long msSingle = (endSingle - startSingle) / 1_000_000;
            singleSeries.getData().add(new XYChart.Data<>(t, msSingle));

            // Измеряем время многопоточного выполнения с t потоками
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

    /**
     * Измерение времени выполнения при разных значениях параметра K (число повторов операции).
     * Возвращает массив из двух серий данных для графика: [0] — однопоточные, [1] — многопоточные.
     * По оси X откладывается K, по оси Y — время выполнения в миллисекундах.
     */
    public static XYChart.Series<Number, Number>[] measureK(double[] a, int baseK, int deltaK, int threads) {
        @SuppressWarnings("unchecked")
        XYChart.Series<Number, Number>[] result = new XYChart.Series[2];

        XYChart.Series<Number, Number> singleSeries = new XYChart.Series<>();
        singleSeries.setName("Однопоточно");

        XYChart.Series<Number, Number> multiSeries = new XYChart.Series<>();
        multiSeries.setName("Многопоточно");

        // Перебираем значения K от deltaK (или 1) до baseK*2, с шагом deltaK (минимум 1)
        int step = Math.max(1, deltaK);
        int maxK = Math.max(baseK, 1) * 2;
        for (int k = step; k <= maxK; k += step) {
            // Измеряем время однопоточного выполнения
            long startSingle = System.nanoTime();
            singleThreadCalc(a, k);
            long endSingle = System.nanoTime();
            long msSingle = (endSingle - startSingle) / 1_000_000;
            singleSeries.getData().add(new XYChart.Data<>(k, msSingle));

            // Измеряем время многопоточного выполнения (фиксированное число потоков)
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