package org.example.Lab2;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class ChartUtils {

    public static void showChart(String title, String xLabel, String yLabel, XYChart.Series<Number, Number>... seriesArray) {
        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.setTitle(title);

            NumberAxis xAxis = new NumberAxis();
            xAxis.setLabel(xLabel);

            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel(yLabel);

            LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle(title);

            for (XYChart.Series<Number, Number> series : seriesArray) {
                lineChart.getData().add(series);
            }

            stage.setScene(new Scene(lineChart, 600, 400));
            stage.show();
        });
    }


    // Чтобы JavaFX не ругался, если вызывается график до запуска FX runtime
    static {
        new JFXPanel();
    }
}
