package org.example;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainApp extends Application {

    private final TableView<ExamRecord> tableView = new TableView<ExamRecord>();
    private final TextField fileField = new TextField();
    private final TextField sheetField = new TextField("0");

    private final Label statusLabel = new Label("Выберите Excel-файл");
    private final Label totalCountLabel = new Label("Всего записей: 0");
    private final Label absentCountLabel = new Label("Отсутствовали: 0");
    private final Label absentPercentLabel = new Label("Процент отсутствующих: 0.00%");

    @Override
    public void start(Stage stage) {
        stage.setTitle("VPRHelp");

        fileField.setPrefWidth(420);
        fileField.setEditable(false);

        Button browseButton = new Button("Выбрать файл");
        Button loadButton = new Button("Загрузить");

        browseButton.setOnAction(event -> chooseFile(stage));
        loadButton.setOnAction(event -> loadData());

        HBox controls = new HBox(10,
                new Label("Файл:"),
                fileField,
                browseButton,
                new Label("Лист:"),
                sheetField,
                loadButton
        );
        controls.setPadding(new Insets(10));

        HBox statsPanel = new HBox(20,
                createStatBox(totalCountLabel),
                createStatBox(absentCountLabel),
                createStatBox(absentPercentLabel)
        );
        statsPanel.setPadding(new Insets(0, 10, 10, 10));

        VBox topPanel = new VBox(10, controls, statsPanel);

        setupTable();

        BorderPane root = new BorderPane();
        root.setTop(topPanel);
        root.setCenter(tableView);
        root.setBottom(statusLabel);
        BorderPane.setMargin(statusLabel, new Insets(10));

        Scene scene = new Scene(root, 1250, 720);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createStatBox(Label valueLabel) {
        Label titleLabel = new Label("Показатель");
        titleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");

        valueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox box = new VBox(6, titleLabel, valueLabel);
        box.setPadding(new Insets(12));
        box.setMinWidth(220);
        box.setPrefWidth(220);
        box.setStyle(
                "-fx-background-color: #f5f5f5;" +
                        "-fx-border-color: #d9d9d9;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        if (valueLabel == totalCountLabel) {
            titleLabel.setText("Всего записей");
        } else if (valueLabel == absentCountLabel) {
            titleLabel.setText("Отсутствовали");
        } else if (valueLabel == absentPercentLabel) {
            titleLabel.setText("Процент отсутствующих");
        }

        return box;
    }

    private void setupTable() {
        TableColumn<ExamRecord, String> studentIdColumn = new TableColumn<ExamRecord, String>("Код участника");
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<ExamRecord, String>("studentId"));
        studentIdColumn.setPrefWidth(120);

        TableColumn<ExamRecord, String> absentColumn = new TableColumn<ExamRecord, String>("Статус");
        absentColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isAbsent() ? "Отсутствовал" : "Присутствовал"));
        absentColumn.setPrefWidth(140);

        TableColumn<ExamRecord, String> variantsColumn = new TableColumn<ExamRecord, String>("Варианты");
        variantsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.join(", ", cellData.getValue().getVariants())));
        variantsColumn.setPrefWidth(160);

        TableColumn<ExamRecord, String> taskScoresColumn = new TableColumn<ExamRecord, String>("Баллы");
        taskScoresColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatTaskScores(cellData.getValue().getTaskScores())));
        taskScoresColumn.setPrefWidth(360);

        TableColumn<ExamRecord, String> classNumberColumn = new TableColumn<ExamRecord, String>("Класс");
        classNumberColumn.setCellValueFactory(new PropertyValueFactory<ExamRecord, String>("classNumber"));
        classNumberColumn.setPrefWidth(90);

        TableColumn<ExamRecord, String> genderColumn = new TableColumn<ExamRecord, String>("Пол");
        genderColumn.setCellValueFactory(new PropertyValueFactory<ExamRecord, String>("gender"));
        genderColumn.setPrefWidth(80);

        TableColumn<ExamRecord, String> previousMarkColumn = new TableColumn<ExamRecord, String>("Предыдущая отметка");
        previousMarkColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(toDisplayString(cellData.getValue().getPreviousPeriodMark())));
        previousMarkColumn.setPrefWidth(180);

        TableColumn<ExamRecord, String> totalScoreColumn = new TableColumn<ExamRecord, String>("Итого баллов");
        totalScoreColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(toDisplayString(cellData.getValue().getTotalScore())));
        totalScoreColumn.setPrefWidth(130);

        tableView.getColumns().add(studentIdColumn);
        tableView.getColumns().add(absentColumn);
        tableView.getColumns().add(variantsColumn);
        tableView.getColumns().add(taskScoresColumn);
        tableView.getColumns().add(classNumberColumn);
        tableView.getColumns().add(genderColumn);
        tableView.getColumns().add(previousMarkColumn);
        tableView.getColumns().add(totalScoreColumn);

        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        Region spacer = new Region();
        spacer.setMinHeight(0);
    }

    private void chooseFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите Excel-файл");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel files", "*.xlsx")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            fileField.setText(file.getAbsolutePath());
            statusLabel.setText("Файл выбран: " + file.getName());
        }
    }

    private void loadData() {
        String filePath = fileField.getText();
        if (filePath == null || filePath.trim().isEmpty()) {
            showError("Сначала выберите Excel-файл.");
            return;
        }

        int sheetIndex;
        try {
            sheetIndex = Integer.parseInt(sheetField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Индекс листа должен быть целым числом.");
            return;
        }

        try {
            ExamRecordSource source = new ExcelExamRecordSource(filePath, sheetIndex);
            List<ExamRecord> records = source.loadRecords();

            tableView.setItems(FXCollections.observableArrayList(records));
            updateStats(records);

            statusLabel.setText("Загружено записей: " + records.size());
        } catch (Exception e) {
            showError("Ошибка загрузки: " + e.getMessage());
        }
    }

    private void updateStats(List<ExamRecord> records) {
        int totalCount = records.size();
        long absentCount = records.stream()
                .filter(ExamRecord::isAbsent)
                .count();

        double absentPercent = totalCount == 0
                ? 0.0
                : (absentCount * 100.0) / totalCount;

        totalCountLabel.setText(String.valueOf(totalCount));
        absentCountLabel.setText(String.valueOf(absentCount));
        absentPercentLabel.setText(String.format(Locale.US, "%.2f%%", absentPercent));
    }

    private String formatTaskScores(List<Integer> scores) {
        if (scores == null || scores.isEmpty()) {
            return "";
        }

        return scores.stream()
                .map(score -> score == null ? "Х" : String.valueOf(score))
                .collect(Collectors.joining(", "));
    }

    private String toDisplayString(Integer value) {
        return value == null ? "" : String.valueOf(value);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Не удалось выполнить действие");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}