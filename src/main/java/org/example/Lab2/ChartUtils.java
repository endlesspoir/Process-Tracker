package org.example.Lab2;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
 *  класс для быстрого отображения линейных графиков JavaFX
 * из любого потока. Файл хранит единственный статический метод showChart(...),
 * который создаёт новое окно со сценой и отображает переданные серии данных.
 *
 * Примечание: чтобы этот класс корректно работал в приложениях, которые
 * не запускают JavaFX через Application.launch(...), здесь в static-блоке
 * вызывается создание JFXPanel() — это инициализирует JavaFX runtime.
 */
public class ChartUtils {

    /**
     * Отобразить линейный график в отдельном окне.
     *
     * @param title      Заголовок окна и графика
     * @param xLabel     Подпись оси X
     * @param yLabel     Подпись оси Y
     * @param seriesArray Переменное число серий (XYChart.Series) с данными для отображения.
     *                    Можно передать 0 или больше серий.
     *
     * Особенности реализации:
     * - Метод можно вызывать из любого потока: UI-пакет JavaFX требует, чтобы
     *   работа с сценой и узлами выполнялась в JavaFX Application Thread — для
     *   этого используется Platform.runLater(...).
     * - Если JavaFX runtime ещё не инициализирован (например, нет вызова
     *   Application.launch(...)), статический блок класса создаёт JFXPanel,
     *   что принудительно инициализирует JavaFX.
     */
    public static void showChart(String title, String xLabel, String yLabel, XYChart.Series<Number, Number>... seriesArray) {
        // Platform.runLater гарантирует, что код внутри лямбды выполнится
        // в JavaFX Application Thread — это обязательно при создании Stage/Scene.
        Platform.runLater(() -> {
            // Создаём новое окно (Stage). В простых утилитных приложениях
            // каждое вызванное showChart откроет новое окно.
            Stage stage = new Stage();
            stage.setTitle(title);

            // Оси графика: NumberAxis автоматически подберёт диапазон
            // если вы не укажете свои минимумы/максимумы.
            NumberAxis xAxis = new NumberAxis();
            xAxis.setLabel(xLabel);

            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel(yLabel);

            // Сам объект LineChart, использующий созданные оси.
            LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle(title);

            // Добавляем все переданные серии в график. Каждая серия — это набор
            // точек (XYChart.Data) с числовыми координатами.
            for (XYChart.Series<Number, Number> series : seriesArray) {
                // Небольшая защита: не добавляем null-серии
                if (series != null) {
                    lineChart.getData().add(series);
                }
            }

            // Создаём сцену заданного размера и показываем окно.
            stage.setScene(new Scene(lineChart, 600, 400));
            stage.show();
        });
    }


    // Чтобы JavaFX не ругался, если вызывается график до запуска FX runtime.
    // Создание JFXPanel инициализирует JavaFX toolkit в окружении Swing/AWT.
    // Если вы используете чистый JavaFX (через Application.launch), этот блок
    // не вреден, но и не обязателен.
    static {
        new JFXPanel();
    }
}
