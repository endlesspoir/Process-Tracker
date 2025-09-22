package org.example.Lab1;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class MainApp extends Application {

    private TableView<ProcessInfo> table;
    private Label totalLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        totalLabel = new Label("Total processes: 0");

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProcessInfo, Long> pidCol = new TableColumn<>("PID");
        pidCol.setCellValueFactory(new PropertyValueFactory<>("pid"));

        TableColumn<ProcessInfo, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(new PropertyValueFactory<>("user"));

        TableColumn<ProcessInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<ProcessInfo, Long> memCol = new TableColumn<>("Memory (KB)");
        memCol.setCellValueFactory(new PropertyValueFactory<>("memory"));

        TableColumn<ProcessInfo, Number> priorityCol = new TableColumn<>("Priority (0-5)");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    int p = priority.intValue();
                    setText(String.valueOf(p));
                    double ratio = p / 5.0;
                    int red = (int) (ratio * 255);
                    int green = (int) ((1 - ratio) * 255);
                    setStyle("-fx-background-color: rgb(" + red + "," + green + ",0); -fx-alignment: CENTER;");
                }
            }
        });

        TableColumn<ProcessInfo, Integer> threadsCol = new TableColumn<>("Threads");
        threadsCol.setCellValueFactory(new PropertyValueFactory<>("threads"));

        table.getColumns().addAll(pidCol, userCol, nameCol, memCol, priorityCol, threadsCol);

        table.setRowFactory(tv -> {
            TableRow<ProcessInfo> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    ProcessInfo rowData = row.getItem();
                    showThreadsWindow(rowData.getPid());
                }
            });
            return row;
        });

        VBox root = new VBox(10, totalLabel, table);
        root.setPadding(new Insets(15));
        Scene scene = new Scene(root, 950, 600);
        scene.getStylesheets().add(getClass().getResource("/styles_lab1.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Process Viewer");
        primaryStage.show();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTable();
            }
        }, 0, 3000);
    }

    private void updateTable() {
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();

        List<OSProcess> processes = os.getProcesses(
                p -> true,
                Comparator.comparingInt(OSProcess::getProcessID),
                Integer.MAX_VALUE
        );

        List<ProcessInfo> data = processes.stream()
                .map(p -> new ProcessInfo(
                        p.getProcessID(),
                        p.getUser(),
                        p.getName(),
                        p.getResidentSetSize() / 1024,
                        normalizePriority(p.getPriority()),
                        p.getThreadCount()
                ))
                .collect(Collectors.toList());

        Platform.runLater(() -> {
            table.getItems().setAll(data);
            totalLabel.setText("Total processes: " + data.size());
        });
    }

    private int normalizePriority(int sysPriority) {
        int min = 0;
        int max = 39;
        sysPriority = Math.max(min, Math.min(max, sysPriority));
        return (int) Math.round((sysPriority - min) / (double)(max - min) * 5);
    }

    private void showThreadsWindow(long pid) {
        Stage stage = new Stage();
        TableView<ThreadInfo> threadTable = new TableView<>();
        threadTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ThreadInfo, Long> tidCol = new TableColumn<>("Thread ID");
        tidCol.setCellValueFactory(new PropertyValueFactory<>("tid"));

        TableColumn<ThreadInfo, Number> priorityCol = new TableColumn<>("Priority (0-5)");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    int p = priority.intValue();
                    setText(String.valueOf(p));
                    double ratio = p / 5.0;
                    int red = (int) (ratio * 255);
                    int green = (int) ((1 - ratio) * 255);
                    setStyle("-fx-background-color: rgb(" + red + "," + green + ",0); -fx-alignment: CENTER;");
                }
            }
        });

        threadTable.getColumns().addAll(tidCol, priorityCol);

        VBox root = new VBox(10, threadTable);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root, 450, 300);
        scene.getStylesheets().add(getClass().getResource("/styles_lab1.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Threads of PID " + pid);
        stage.show();

        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        OSProcess process = os.getProcess((int) pid);

        if (process != null && process.getThreadDetails() != null && !process.getThreadDetails().isEmpty()) {
            List<ThreadInfo> threads = process.getThreadDetails().stream()
                    .map(t -> new ThreadInfo(t.getThreadId(), normalizePriority(t.getPriority())))
                    .collect(Collectors.toList());
            threadTable.getItems().setAll(threads);
        } else {
            threadTable.setPlaceholder(new Label("Cannot access threads. Run as root to see all threads."));
        }
    }
}
