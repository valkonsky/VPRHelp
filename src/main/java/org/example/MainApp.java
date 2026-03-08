package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainApp extends Application {

    private final TableView<ExamRecord> tableView = new TableView<ExamRecord>();
    private final TableView<ClassStats> classStatsTableView = new TableView<ClassStats>();

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

    private final VBox classIntervalRowsBox = new VBox(8);
    private final List<ClassIntervalRow> classIntervalRows = new ArrayList<ClassIntervalRow>();

    private List<ExamRecord> rawRecords = new ArrayList<ExamRecord>();
    private List<ExamRecord> currentRecords = new ArrayList<ExamRecord>();

    private final ParticipantRegistry participantRegistry =
            new ParticipantRegistry(new File("data/participants.properties"));

    private final GradeService gradeService = new GradeService();
    private final ClassIntervalService classIntervalService = new ClassIntervalService();
    private final StatsService statsService = new StatsService(gradeService);

    private final ExamTableBuilder examTableBuilder =
            new ExamTableBuilder(participantRegistry, gradeService);

    private final ClassStatsTableBuilder classStatsTableBuilder =
            new ClassStatsTableBuilder();

    private final RightPanelBuilder rightPanelBuilder =
            new RightPanelBuilder();

    @Override
    public void start(Stage stage) {
        stage.setTitle("VPRHelp");

        try {
            participantRegistry.load();
            statusLabel.setText("База участников загружена: " + participantRegistry.size());
        } catch (IOException e) {
            statusLabel.setText("Не удалось загрузить базу участников: " + e.getMessage());
        }

        fileField.setPrefWidth(420);
        fileField.setEditable(false);

        setDefaultGradeRanges();
        setDefaultClassIntervals();

        Button browseButton = new Button("Выбрать файл");
        Button loadButton = new Button("Загрузить файл");
        Button applyButton = new Button("Применить и показать");
        Button loadProtocolButton = new Button("Загрузить протокол");

        browseButton.getStyleClass().add("secondary-button");
        loadButton.getStyleClass().add("primary-button");
        applyButton.getStyleClass().add("primary-button");
        loadProtocolButton.getStyleClass().add("secondary-button");

        browseButton.setOnAction(event -> chooseFile(stage));
        loadButton.setOnAction(event -> loadData());
        applyButton.setOnAction(event -> applySettingsAndShowResults());
        loadProtocolButton.setOnAction(event -> loadProtocol(stage));

        HBox controls = new HBox(
                10,
                new Label("Файл:"),
                fileField,
                browseButton,
                new Label("Лист:"),
                sheetField,
                loadButton,
                loadProtocolButton
        );
        controls.setPadding(new Insets(10));
        controls.getStyleClass().add("top-toolbar");

        HBox statsPanel = new HBox(
                20,
                createStatBox("Всего записей", totalCountLabel),
                createStatBox("Отсутствовали", absentCountLabel),
                createStatBox("Процент отсутствующих", absentPercentLabel)
        );
        statsPanel.setPadding(new Insets(0, 10, 10, 10));
        statsPanel.getStyleClass().add("stats-row");

        VBox topPanel = new VBox(10, controls, statsPanel);
        topPanel.getStyleClass().add("top-panel");

        VBox rightPanelContent = rightPanelBuilder.createRightPanel(
                classIntervalRowsBox,
                () -> addClassIntervalRow("", "", ""),
                applyButton,
                grade2FromField, grade2ToField,
                grade3FromField, grade3ToField,
                grade4FromField, grade4ToField,
                grade5FromField, grade5ToField,
                grade2CountLabel, grade2PercentLabel,
                grade3CountLabel, grade3PercentLabel,
                grade4CountLabel, grade4PercentLabel,
                grade5CountLabel, grade5PercentLabel,
                matchedCountLabel,
                increasedCountLabel,
                decreasedCountLabel,
                matchedPercentLabel
        );

        ScrollPane rightPanelScroll = new ScrollPane(rightPanelContent);
        rightPanelScroll.setFitToWidth(true);
        rightPanelScroll.setPrefWidth(410);
        rightPanelScroll.getStyleClass().add("side-scroll");

        examTableBuilder.setupTable(
                tableView,
                grade2FromField, grade2ToField,
                grade3FromField, grade3ToField,
                grade4FromField, grade4ToField,
                grade5FromField, grade5ToField
        );
        classStatsTableBuilder.setupClassStatsTable(classStatsTableView);

        tableView.getStyleClass().add("exam-table");
        classStatsTableView.getStyleClass().add("exam-table");
        statusLabel.getStyleClass().add("status-bar");

        Tab participantsTab = new Tab("Участники", tableView);
        participantsTab.setClosable(false);

        Tab classStatsTab = new Tab("По классам", classStatsTableView);
        classStatsTab.setClosable(false);

        TabPane tabPane = new TabPane(participantsTab, classStatsTab);

        BorderPane root = new BorderPane();
        root.setTop(topPanel);
        root.setCenter(tabPane);
        root.setRight(rightPanelScroll);
        root.setBottom(statusLabel);
        root.getStyleClass().add("app-root");
        BorderPane.setMargin(statusLabel, new Insets(10));

        Scene scene = new Scene(root, 1850, 780);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    private VBox createStatBox(String titleText, Label valueLabel) {
        Label titleLabel = new Label(titleText);
        titleLabel.getStyleClass().add("stat-title");

        valueLabel.getStyleClass().add("stat-value");

        VBox box = new VBox(6, titleLabel, valueLabel);
        box.setPadding(new Insets(12));
        box.setMinWidth(220);
        box.setPrefWidth(220);
        box.getStyleClass().add("card");

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

    private void loadProtocol(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите протокол");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Word files", "*.docx")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        try {
            ProtocolDocxReader reader = new ProtocolDocxReader();
            Map<String, String> loaded = reader.read(file.getAbsolutePath());

            if (loaded.isEmpty()) {
                showError("В протоколе не найдено ни одной пары «код - ФИО».");
                return;
            }

            int before = participantRegistry.size();
            participantRegistry.putAll(loaded);
            participantRegistry.save();
            int after = participantRegistry.size();

            tableView.refresh();

            if (rawRecords == null || rawRecords.isEmpty()) {
                statusLabel.setText(
                        "Протокол загружен: " + file.getName() +
                                ". Найдено записей: " + loaded.size() +
                                ", всего в базе: " + after +
                                ", новых: " + (after - before) +
                                ". Теперь загрузите Excel-файл."
                );
                return;
            }

            statusLabel.setText(
                    "Протокол загружен: " + file.getName() +
                            ". Найдено записей: " + loaded.size() +
                            ", всего в базе: " + after +
                            ", новых: " + (after - before)
            );
        } catch (Exception e) {
            showError("Ошибка загрузки протокола: " + e.getMessage());
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
            rawRecords = source.loadRecords();
            currentRecords = new ArrayList<ExamRecord>();

            tableView.setItems(FXCollections.observableArrayList());
            classStatsTableView.setItems(FXCollections.observableArrayList());
            resetStats();

            statusLabel.setText(
                    "Файл загружен: " + rawRecords.size() +
                            " записей. Теперь задайте диапазоны классов и баллов, затем нажмите «Применить и показать»."
            );
        } catch (Exception e) {
            showError("Ошибка загрузки: " + e.getMessage());
        }
    }

    private void applySettingsAndShowResults() {
        if (rawRecords == null || rawRecords.isEmpty()) {
            showError("Сначала загрузите Excel-файл.");
            return;
        }

        GradeRange range2;
        GradeRange range3;
        GradeRange range4;
        GradeRange range5;
        List<ClassInterval> classIntervals;

        try {
            classIntervals = readClassIntervals();

            range2 = gradeService.readRange(grade2FromField, grade2ToField, "2");
            range3 = gradeService.readRange(grade3FromField, grade3ToField, "3");
            range4 = gradeService.readRange(grade4FromField, grade4ToField, "4");
            range5 = gradeService.readRange(grade5FromField, grade5ToField, "5");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            return;
        }

        ClassAssignmentResult assignmentResult =
                classIntervalService.applyClassIntervals(rawRecords, classIntervals);
        currentRecords = assignmentResult.getRecords();

        tableView.setItems(FXCollections.observableArrayList(currentRecords));
        updateAttendanceStats(currentRecords);
        updateGradeStats(range2, range3, range4, range5);
        updateComparisonStats(range2, range3, range4, range5);
        updateClassStatsTable(range2, range3, range4, range5);

        tableView.refresh();
        classStatsTableView.refresh();

        long unknownCount = currentRecords.stream()
                .filter(record -> record.getClassNumber() == null || record.getClassNumber().trim().isEmpty())
                .count();

        statusLabel.setText(
                "Показаны результаты. " +
                        "Классы определены по диапазонам: " + assignmentResult.getAssignedCount() +
                        "; без класса осталось: " + unknownCount
        );
    }

    private void resetStats() {
        totalCountLabel.setText("0");
        absentCountLabel.setText("0");
        absentPercentLabel.setText("0.00%");

        grade2CountLabel.setText("0");
        grade2PercentLabel.setText("0.00%");
        grade3CountLabel.setText("0");
        grade3PercentLabel.setText("0.00%");
        grade4CountLabel.setText("0");
        grade4PercentLabel.setText("0.00%");
        grade5CountLabel.setText("0");
        grade5PercentLabel.setText("0.00%");

        matchedCountLabel.setText("0");
        increasedCountLabel.setText("0");
        decreasedCountLabel.setText("0");
        matchedPercentLabel.setText("0.00%");
    }

    private void updateAttendanceStats(List<ExamRecord> records) {
        StatsService.AttendanceStats stats = statsService.calculateAttendanceStats(records);

        totalCountLabel.setText(String.valueOf(stats.getTotalCount()));
        absentCountLabel.setText(String.valueOf(stats.getAbsentCount()));
        absentPercentLabel.setText(String.format(Locale.US, "%.2f%%", stats.getAbsentPercent()));
    }

    private void updateGradeStats(GradeRange range2, GradeRange range3, GradeRange range4, GradeRange range5) {
        StatsService.GradeStats stats =
                statsService.calculateGradeStats(currentRecords, range2, range3, range4, range5);

        grade2CountLabel.setText(String.valueOf(stats.getCount2()));
        grade3CountLabel.setText(String.valueOf(stats.getCount3()));
        grade4CountLabel.setText(String.valueOf(stats.getCount4()));
        grade5CountLabel.setText(String.valueOf(stats.getCount5()));

        grade2PercentLabel.setText(stats.getPercent2());
        grade3PercentLabel.setText(stats.getPercent3());
        grade4PercentLabel.setText(stats.getPercent4());
        grade5PercentLabel.setText(stats.getPercent5());
    }

    private void updateComparisonStats(GradeRange range2, GradeRange range3, GradeRange range4, GradeRange range5) {
        StatsService.ComparisonStats stats =
                statsService.calculateComparisonStats(currentRecords, range2, range3, range4, range5);

        matchedCountLabel.setText(String.valueOf(stats.getMatchedCount()));
        increasedCountLabel.setText(String.valueOf(stats.getIncreasedCount()));
        decreasedCountLabel.setText(String.valueOf(stats.getDecreasedCount()));
        matchedPercentLabel.setText(stats.getMatchedPercent());
    }

    private void updateClassStatsTable(GradeRange range2, GradeRange range3, GradeRange range4, GradeRange range5) {
        List<ClassStats> stats = statsService.buildClassStats(currentRecords, range2, range3, range4, range5);
        classStatsTableView.setItems(FXCollections.observableArrayList(stats));
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

    private void setDefaultClassIntervals() {
        addClassIntervalRow("", "", "");
        addClassIntervalRow("", "", "");
        addClassIntervalRow("", "", "");
    }

    private void addClassIntervalRow(String className, String from, String to) {
        ClassIntervalRow row = new ClassIntervalRow(className, from, to);
        classIntervalRows.add(row);
        classIntervalRowsBox.getChildren().add(row.container);
    }

    private List<ClassInterval> readClassIntervals() {
        List<ClassIntervalService.ClassIntervalInput> inputs =
                new ArrayList<ClassIntervalService.ClassIntervalInput>();

        for (ClassIntervalRow row : classIntervalRows) {
            inputs.add(new ClassIntervalService.ClassIntervalInput(
                    row.classField,
                    row.fromField,
                    row.toField
            ));
        }

        return classIntervalService.readClassIntervals(inputs);
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

    private class ClassIntervalRow {
        private final HBox container;
        private final TextField classField = new TextField();
        private final TextField fromField = new TextField();
        private final TextField toField = new TextField();

        private ClassIntervalRow(String className, String from, String to) {
            classField.setPromptText("Класс");
            classField.setPrefWidth(80);
            classField.setText(className);

            fromField.setPromptText("от");
            fromField.setPrefWidth(90);
            fromField.setText(from);

            toField.setPromptText("до");
            toField.setPrefWidth(90);
            toField.setText(to);

            Button removeButton = new Button("✕");
            removeButton.getStyleClass().add("secondary-button");

            HBox.setHgrow(classField, Priority.ALWAYS);
            HBox.setHgrow(fromField, Priority.ALWAYS);
            HBox.setHgrow(toField, Priority.ALWAYS);

            HBox rowContainer = new HBox(
                    8,
                    new Label("Класс"), classField,
                    new Label("от"), fromField,
                    new Label("до"), toField,
                    removeButton
            );

            removeButton.setOnAction(event -> {
                classIntervalRows.remove(this);
                classIntervalRowsBox.getChildren().remove(rowContainer);
                if (classIntervalRows.isEmpty()) {
                    addClassIntervalRow("", "", "");
                }
            });

            container = rowContainer;
        }
    }
}