package org.example.Lab2;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainApp extends Application {

    private TextField nField, threadsField, kField, deltaThreadsField, deltaKField;
    private TextArea resultArea;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Лабораторная 2 - Многопоточность");

        Label nLabel = new Label("N (размер массива):");
        nField = new TextField("10000");

        Label threadsLabel = new Label("Макс. потоки:");
        threadsField = new TextField("8");

        Label kLabel = new Label("Параметр сложности K:");
        kField = new TextField("500");

        Label deltaThreadsLabel = new Label("Шаг потоков:");
        deltaThreadsField = new TextField("2");

        Label deltaKLabel = new Label("Шаг K:");
        deltaKField = new TextField("200");

        Button startButton = new Button("Старт");
        startButton.setOnAction(e -> runCalculations());

        resultArea = new TextArea();
        resultArea.setEditable(false);

        GridPane inputGrid = new GridPane();
        inputGrid.setPadding(new Insets(10));
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);

        inputGrid.addRow(0, nLabel, nField);
        inputGrid.addRow(1, threadsLabel, threadsField);
        inputGrid.addRow(2, kLabel, kField);
        inputGrid.addRow(3, deltaThreadsLabel, deltaThreadsField);
        inputGrid.addRow(4, deltaKLabel, deltaKField);
        inputGrid.addRow(5, startButton);

        VBox root = new VBox(10, inputGrid, resultArea);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    private void runCalculations() {
        int N = Integer.parseInt(nField.getText());
        int maxThreads = Integer.parseInt(threadsField.getText());
        int K = Integer.parseInt(kField.getText());
        int deltaThreads = Integer.parseInt(deltaThreadsField.getText());
        int deltaK = Integer.parseInt(deltaKField.getText());

        resultArea.clear();

        // CPU параметры через OSHI
        SystemInfo si = new SystemInfo();
        CentralProcessor cpu = si.getHardware().getProcessor();
        long freq = cpu.getMaxFreq();
        int cores = cpu.getPhysicalProcessorCount();
        resultArea.appendText("CPU: " + cpu.getProcessorIdentifier().getName() + "\n");
        resultArea.appendText("Частота: " + (freq / 1_000_000) + " MHz\n");
        resultArea.appendText("Ядер: " + cores + "\n\n");

        // Массив случайных чисел
        double[] a = CalculationTask.generateArray(N);

        // Однопоточные вычисления
        long start = System.nanoTime();
        CalculationTask.singleThreadCalc(a, K);
        long end = System.nanoTime();
        long singleThreadTime = (end - start) / 1_000_000;
        resultArea.appendText("Время (однопоточно): " + singleThreadTime + " мс\n");

        // Многопоточные вычисления (фиксированное число потоков)
        ExecutorService pool = Executors.newFixedThreadPool(maxThreads);
        start = System.nanoTime();
        CalculationTask.multiThreadCalc(a, K, maxThreads, pool);
        end = System.nanoTime();
        long multiThreadTime = (end - start) / 1_000_000;
        resultArea.appendText("Время (многопоточно, " + maxThreads + " потоков): " + multiThreadTime + " мс\n\n");
        pool.shutdown();

        // График 1: зависимость времени от числа потоков
        XYChart.Series<Number, Number> threadSeries = CalculationTask.measureThreads(a, K, deltaThreads, maxThreads);

        // График 2: зависимость времени от K
        XYChart.Series<Number, Number> kSeries = CalculationTask.measureK(a, K, deltaK, maxThreads);

        // Открыть графики
        ChartUtils.showChart("График 1: Время vs Потоки", "Потоки", "Время (мс)", threadSeries);
        ChartUtils.showChart("График 2: Время vs K", "K", "Время (мс)", kSeries);
    }

    public static void main(String[] args) {
        launch();
    }
}
