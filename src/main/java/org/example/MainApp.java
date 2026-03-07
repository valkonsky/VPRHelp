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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
    private final Label totalCountLabel = new Label("0");
    private final Label absentCountLabel = new Label("0");
    private final Label absentPercentLabel = new Label("0.00%");

    private final TextField grade2FromField = new TextField();
    private final TextField grade2ToField = new TextField();
    private final TextField grade3FromField = new TextField();
    private final TextField grade3ToField = new TextField();
    private final TextField grade4FromField = new TextField();
    private final TextField grade4ToField = new TextField();
    private final TextField grade5FromField = new TextField();
    private final TextField grade5ToField = new TextField();

    private final Label grade2CountLabel = new Label("0");
    private final Label grade2PercentLabel = new Label("0.00%");
    private final Label grade3CountLabel = new Label("0");
    private final Label grade3PercentLabel = new Label("0.00%");
    private final Label grade4CountLabel = new Label("0");
    private final Label grade4PercentLabel = new Label("0.00%");
    private final Label grade5CountLabel = new Label("0");
    private final Label grade5PercentLabel = new Label("0.00%");

    private final Label matchedCountLabel = new Label("0");
    private final Label increasedCountLabel = new Label("0");
    private final Label decreasedCountLabel = new Label("0");
    private final Label matchedPercentLabel = new Label("0.00%");

    private List<ExamRecord> currentRecords = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        stage.setTitle("VPRHelp");

        fileField.setPrefWidth(420);
        fileField.setEditable(false);

        setDefaultGradeRanges();

        Button browseButton = new Button("Выбрать файл");
        Button loadButton = new Button("Загрузить");
        Button calculateGradesButton = new Button("Рассчитать оценки");

        browseButton.setOnAction(event -> chooseFile(stage));
        loadButton.setOnAction(event -> loadData());
        calculateGradesButton.setOnAction(event -> recalculateAllStats());

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
                createStatBox("Всего записей", totalCountLabel),
                createStatBox("Отсутствовали", absentCountLabel),
                createStatBox("Процент отсутствующих", absentPercentLabel)
        );
        statsPanel.setPadding(new Insets(0, 10, 10, 10));

        VBox topPanel = new VBox(10, controls, statsPanel);

        VBox rightPanel = new VBox(12,
                createGradeRangesPanel(calculateGradesButton),
                createGradeStatsPanel(),
                createComparisonStatsPanel()
        );
        rightPanel.setPadding(new Insets(10));
        rightPanel.setPrefWidth(360);

        setupTable();

        BorderPane root = new BorderPane();
        root.setTop(topPanel);
        root.setCenter(tableView);
        root.setRight(rightPanel);
        root.setBottom(statusLabel);
        BorderPane.setMargin(statusLabel, new Insets(10));

        Scene scene = new Scene(root, 1700, 760);
        stage.setScene(scene);
        stage.show();
    }

    private void setupTable() {
        TableColumn<ExamRecord, String> studentIdColumn = new TableColumn<ExamRecord, String>("Код участника");
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<ExamRecord, String>("studentId"));
        studentIdColumn.setPrefWidth(120);

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

        TableColumn<ExamRecord, String> currentMarkColumn = new TableColumn<ExamRecord, String>("Текущая отметка");
        currentMarkColumn.setCellValueFactory(cellData -> {
            Integer currentMark = determineCurrentGradeSafe(cellData.getValue().getTotalScore());
            return new SimpleStringProperty(toDisplayString(currentMark));
        });
        currentMarkColumn.setPrefWidth(140);

        TableColumn<ExamRecord, String> comparisonColumn = new TableColumn<ExamRecord, String>("Сравнение");
        comparisonColumn.setCellValueFactory(cellData -> {
            ExamRecord record = cellData.getValue();
            String comparison = buildComparisonText(record);
            return new SimpleStringProperty(comparison);
        });
        comparisonColumn.setPrefWidth(130);

        tableView.getColumns().add(studentIdColumn);
        tableView.getColumns().add(variantsColumn);
        tableView.getColumns().add(taskScoresColumn);
        tableView.getColumns().add(classNumberColumn);
        tableView.getColumns().add(genderColumn);
        tableView.getColumns().add(previousMarkColumn);
        tableView.getColumns().add(totalScoreColumn);
        tableView.getColumns().add(currentMarkColumn);
        tableView.getColumns().add(comparisonColumn);

        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    private VBox createGradeRangesPanel(Button calculateGradesButton) {
        Label title = new Label("Интервалы оценок");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);

        addRangeRow(grid, 0, "Оценка 2:", grade2FromField, grade2ToField);
        addRangeRow(grid, 1, "Оценка 3:", grade3FromField, grade3ToField);
        addRangeRow(grid, 2, "Оценка 4:", grade4FromField, grade4ToField);
        addRangeRow(grid, 3, "Оценка 5:", grade5FromField, grade5ToField);

        Label hint = new Label("Интервалы включительные. Пример: 0–5, 6–10.");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #666666;");

        VBox box = new VBox(10, title, grid, hint, calculateGradesButton);
        box.setPadding(new Insets(12));
        box.setStyle(
                "-fx-background-color: #f5f5f5;" +
                        "-fx-border-color: #d9d9d9;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        return box;
    }

    private VBox createGradeStatsPanel() {
        Label title = new Label("Статистика оценок");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        grid.add(new Label("Оценка"), 0, 0);
        grid.add(new Label("Количество"), 1, 0);
        grid.add(new Label("Процент"), 2, 0);

        addGradeStatRow(grid, 1, "2", grade2CountLabel, grade2PercentLabel);
        addGradeStatRow(grid, 2, "3", grade3CountLabel, grade3PercentLabel);
        addGradeStatRow(grid, 3, "4", grade4CountLabel, grade4PercentLabel);
        addGradeStatRow(grid, 4, "5", grade5CountLabel, grade5PercentLabel);

        Label note = new Label("Проценты считаются среди присутствовавших участников с заполненным итоговым баллом.");
        note.setWrapText(true);
        note.setStyle("-fx-text-fill: #666666;");

        VBox box = new VBox(10, title, grid, note);
        box.setPadding(new Insets(12));
        box.setStyle(
                "-fx-background-color: #f5f5f5;" +
                        "-fx-border-color: #d9d9d9;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        return box;
    }

    private VBox createComparisonStatsPanel() {
        Label title = new Label("Сравнение отметок");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        grid.add(new Label("Показатель"), 0, 0);
        grid.add(new Label("Значение"), 1, 0);

        grid.add(new Label("Совпадает"), 0, 1);
        grid.add(matchedCountLabel, 1, 1);

        grid.add(new Label("Выше"), 0, 2);
        grid.add(increasedCountLabel, 1, 2);

        grid.add(new Label("Ниже"), 0, 3);
        grid.add(decreasedCountLabel, 1, 3);

        grid.add(new Label("Процент совпадений"), 0, 4);
        grid.add(matchedPercentLabel, 1, 4);

        Label note = new Label("Учитываются только присутствовавшие участники с заполненными предыдущей отметкой и итоговым баллом.");
        note.setWrapText(true);
        note.setStyle("-fx-text-fill: #666666;");

        VBox box = new VBox(10, title, grid, note);
        box.setPadding(new Insets(12));
        box.setStyle(
                "-fx-background-color: #f5f5f5;" +
                        "-fx-border-color: #d9d9d9;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        return box;
    }

    private void addRangeRow(GridPane grid, int rowIndex, String labelText, TextField fromField, TextField toField) {
        fromField.setPrefWidth(70);
        toField.setPrefWidth(70);

        grid.add(new Label(labelText), 0, rowIndex);
        grid.add(new Label("от"), 1, rowIndex);
        grid.add(fromField, 2, rowIndex);
        grid.add(new Label("до"), 3, rowIndex);
        grid.add(toField, 4, rowIndex);
    }

    private void addGradeStatRow(GridPane grid, int rowIndex, String grade, Label countLabel, Label percentLabel) {
        grid.add(new Label(grade), 0, rowIndex);
        grid.add(countLabel, 1, rowIndex);
        grid.add(percentLabel, 2, rowIndex);
    }

    private VBox createStatBox(String titleText, Label valueLabel) {
        Label titleLabel = new Label(titleText);
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

        return box;
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
            currentRecords = source.loadRecords();

            tableView.setItems(FXCollections.observableArrayList(currentRecords));
            updateAttendanceStats(currentRecords);
            recalculateAllStats();

            statusLabel.setText("Загружено записей: " + currentRecords.size());
        } catch (Exception e) {
            showError("Ошибка загрузки: " + e.getMessage());
        }
    }

    private void updateAttendanceStats(List<ExamRecord> records) {
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

    private void recalculateAllStats() {
        GradeRange range2;
        GradeRange range3;
        GradeRange range4;
        GradeRange range5;

        try {
            range2 = readRange(grade2FromField, grade2ToField, "2");
            range3 = readRange(grade3FromField, grade3ToField, "3");
            range4 = readRange(grade4FromField, grade4ToField, "4");
            range5 = readRange(grade5FromField, grade5ToField, "5");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            return;
        }

        updateGradeStats(range2, range3, range4, range5);
        updateComparisonStats(range2, range3, range4, range5);
        tableView.refresh();
    }

    private void updateGradeStats(GradeRange range2, GradeRange range3, GradeRange range4, GradeRange range5) {
        List<ExamRecord> eligibleRecords = currentRecords.stream()
                .filter(record -> !record.isAbsent())
                .filter(record -> record.getTotalScore() != null)
                .collect(Collectors.toList());

        int baseCount = eligibleRecords.size();

        long count2 = eligibleRecords.stream()
                .filter(record -> range2.contains(record.getTotalScore()))
                .count();

        long count3 = eligibleRecords.stream()
                .filter(record -> range3.contains(record.getTotalScore()))
                .count();

        long count4 = eligibleRecords.stream()
                .filter(record -> range4.contains(record.getTotalScore()))
                .count();

        long count5 = eligibleRecords.stream()
                .filter(record -> range5.contains(record.getTotalScore()))
                .count();

        grade2CountLabel.setText(String.valueOf(count2));
        grade3CountLabel.setText(String.valueOf(count3));
        grade4CountLabel.setText(String.valueOf(count4));
        grade5CountLabel.setText(String.valueOf(count5));

        grade2PercentLabel.setText(formatPercent(count2, baseCount));
        grade3PercentLabel.setText(formatPercent(count3, baseCount));
        grade4PercentLabel.setText(formatPercent(count4, baseCount));
        grade5PercentLabel.setText(formatPercent(count5, baseCount));
    }

    private void updateComparisonStats(GradeRange range2, GradeRange range3, GradeRange range4, GradeRange range5) {
        List<ExamRecord> comparableRecords = currentRecords.stream()
                .filter(record -> !record.isAbsent())
                .filter(record -> record.getPreviousPeriodMark() != null)
                .filter(record -> record.getTotalScore() != null)
                .collect(Collectors.toList());

        int baseCount = 0;
        long matchedCount = 0;
        long increasedCount = 0;
        long decreasedCount = 0;

        for (ExamRecord record : comparableRecords) {
            Integer currentGrade = determineGrade(
                    record.getTotalScore(),
                    range2,
                    range3,
                    range4,
                    range5
            );

            Integer previousGrade = record.getPreviousPeriodMark();

            if (currentGrade == null) {
                continue;
            }

            baseCount++;

            if (currentGrade.intValue() == previousGrade.intValue()) {
                matchedCount++;
            } else if (currentGrade.intValue() > previousGrade.intValue()) {
                increasedCount++;
            } else {
                decreasedCount++;
            }
        }

        matchedCountLabel.setText(String.valueOf(matchedCount));
        increasedCountLabel.setText(String.valueOf(increasedCount));
        decreasedCountLabel.setText(String.valueOf(decreasedCount));
        matchedPercentLabel.setText(formatPercent(matchedCount, baseCount));
    }

    private GradeRange readRange(TextField fromField, TextField toField, String gradeName) {
        String fromText = fromField.getText().trim();
        String toText = toField.getText().trim();

        if (fromText.isEmpty() || toText.isEmpty()) {
            throw new IllegalArgumentException("Для оценки " + gradeName + " нужно заполнить обе границы интервала.");
        }

        try {
            int from = Integer.parseInt(fromText);
            int to = Integer.parseInt(toText);

            if (from > to) {
                throw new IllegalArgumentException("Для оценки " + gradeName + " левая граница больше правой.");
            }

            return new GradeRange(from, to);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Интервал для оценки " + gradeName + " должен содержать целые числа.");
        }
    }

    private Integer determineGrade(
            Integer totalScore,
            GradeRange range2,
            GradeRange range3,
            GradeRange range4,
            GradeRange range5
    ) {
        if (totalScore == null) {
            return null;
        }

        if (range2.contains(totalScore.intValue())) {
            return Integer.valueOf(2);
        }
        if (range3.contains(totalScore.intValue())) {
            return Integer.valueOf(3);
        }
        if (range4.contains(totalScore.intValue())) {
            return Integer.valueOf(4);
        }
        if (range5.contains(totalScore.intValue())) {
            return Integer.valueOf(5);
        }

        return null;
    }

    private Integer determineCurrentGradeSafe(Integer totalScore) {
        try {
            GradeRange range2 = readRange(grade2FromField, grade2ToField, "2");
            GradeRange range3 = readRange(grade3FromField, grade3ToField, "3");
            GradeRange range4 = readRange(grade4FromField, grade4ToField, "4");
            GradeRange range5 = readRange(grade5FromField, grade5ToField, "5");

            return determineGrade(totalScore, range2, range3, range4, range5);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String buildComparisonText(ExamRecord record) {
        Integer previousMark = record.getPreviousPeriodMark();
        Integer currentMark = determineCurrentGradeSafe(record.getTotalScore());

        if (previousMark == null || currentMark == null) {
            return "";
        }

        if (currentMark.intValue() == previousMark.intValue()) {
            return "Совпадает";
        }
        if (currentMark.intValue() > previousMark.intValue()) {
            return "Выше";
        }
        return "Ниже";
    }

    private String formatPercent(long count, int baseCount) {
        double percent = baseCount == 0 ? 0.0 : (count * 100.0) / baseCount;
        return String.format(Locale.US, "%.2f%%", percent);
    }

    private void setDefaultGradeRanges() {
        grade2FromField.setText("0");
        grade2ToField.setText("5");

        grade3FromField.setText("6");
        grade3ToField.setText("10");

        grade4FromField.setText("11");
        grade4ToField.setText("15");

        grade5FromField.setText("16");
        grade5ToField.setText("20");
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

    private static class GradeRange {
        private final int from;
        private final int to;

        private GradeRange(int from, int to) {
            this.from = from;
            this.to = to;
        }

        private boolean contains(int value) {
            return value >= from && value <= to;
        }
    }
}