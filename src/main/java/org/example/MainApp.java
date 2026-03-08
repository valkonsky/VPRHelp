package org.example;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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

        VBox rightPanelContent = new VBox(
                12,
                createClassIntervalsPanel(),
                createGradeRangesPanel(applyButton),
                createGradeStatsPanel(),
                createComparisonStatsPanel()
        );
        rightPanelContent.setPadding(new Insets(10));
        rightPanelContent.setPrefWidth(390);
        rightPanelContent.getStyleClass().add("right-panel");

        ScrollPane rightPanelScroll = new ScrollPane(rightPanelContent);
        rightPanelScroll.setFitToWidth(true);
        rightPanelScroll.setPrefWidth(410);
        rightPanelScroll.getStyleClass().add("side-scroll");

        setupTable();
        setupClassStatsTable();

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

    private void setupTable() {
        TableColumn<ExamRecord, String> studentIdColumn = new TableColumn<ExamRecord, String>("Код участника");
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<ExamRecord, String>("studentId"));
        studentIdColumn.setPrefWidth(120);

        TableColumn<ExamRecord, String> fullNameColumn = new TableColumn<ExamRecord, String>("ФИО");
        fullNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        participantRegistry.getNameByCode(cellData.getValue().getStudentId())
                ));
        fullNameColumn.setPrefWidth(240);

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
            Integer currentMark = gradeService.determineCurrentGradeSafe(
                    cellData.getValue().getTotalScore(),
                    grade2FromField, grade2ToField,
                    grade3FromField, grade3ToField,
                    grade4FromField, grade4ToField,
                    grade5FromField, grade5ToField
            );
            return new SimpleStringProperty(toDisplayString(currentMark));
        });
        currentMarkColumn.setPrefWidth(140);

        TableColumn<ExamRecord, String> comparisonColumn = new TableColumn<ExamRecord, String>("Сравнение");
        comparisonColumn.setCellValueFactory(cellData -> {
            ExamRecord record = cellData.getValue();
            return new SimpleStringProperty(
                    gradeService.buildComparisonText(
                            record,
                            grade2FromField, grade2ToField,
                            grade3FromField, grade3ToField,
                            grade4FromField, grade4ToField,
                            grade5FromField, grade5ToField
                    )
            );
        });
        comparisonColumn.setPrefWidth(130);

        tableView.getColumns().add(studentIdColumn);
        tableView.getColumns().add(fullNameColumn);
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

    private void setupClassStatsTable() {
        TableColumn<ClassStats, String> classColumn = new TableColumn<ClassStats, String>("Класс");
        classColumn.setCellValueFactory(new PropertyValueFactory<ClassStats, String>("classNumber"));
        classColumn.setPrefWidth(90);

        TableColumn<ClassStats, Integer> totalColumn = new TableColumn<ClassStats, Integer>("Всего");
        totalColumn.setCellValueFactory(new PropertyValueFactory<ClassStats, Integer>("totalCount"));
        totalColumn.setPrefWidth(80);

        TableColumn<ClassStats, Integer> absentColumn = new TableColumn<ClassStats, Integer>("Отсутств.");
        absentColumn.setCellValueFactory(new PropertyValueFactory<ClassStats, Integer>("absentCount"));
        absentColumn.setPrefWidth(95);

        TableColumn<ClassStats, String> absentPercentColumn = new TableColumn<ClassStats, String>("% отсутств.");
        absentPercentColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format(Locale.US, "%.2f%%", cellData.getValue().getAbsentPercent())));
        absentPercentColumn.setPrefWidth(110);

        TableColumn<ClassStats, Integer> grade2Column = new TableColumn<ClassStats, Integer>("2");
        grade2Column.setCellValueFactory(new PropertyValueFactory<ClassStats, Integer>("grade2Count"));
        grade2Column.setPrefWidth(60);

        TableColumn<ClassStats, Integer> grade3Column = new TableColumn<ClassStats, Integer>("3");
        grade3Column.setCellValueFactory(new PropertyValueFactory<ClassStats, Integer>("grade3Count"));
        grade3Column.setPrefWidth(60);

        TableColumn<ClassStats, Integer> grade4Column = new TableColumn<ClassStats, Integer>("4");
        grade4Column.setCellValueFactory(new PropertyValueFactory<ClassStats, Integer>("grade4Count"));
        grade4Column.setPrefWidth(60);

        TableColumn<ClassStats, Integer> grade5Column = new TableColumn<ClassStats, Integer>("5");
        grade5Column.setCellValueFactory(new PropertyValueFactory<ClassStats, Integer>("grade5Count"));
        grade5Column.setPrefWidth(60);

        TableColumn<ClassStats, Integer> matchedColumn = new TableColumn<ClassStats, Integer>("Совпало");
        matchedColumn.setCellValueFactory(new PropertyValueFactory<ClassStats, Integer>("matchedCount"));
        matchedColumn.setPrefWidth(90);

        TableColumn<ClassStats, Integer> increasedColumn = new TableColumn<ClassStats, Integer>("Выше");
        increasedColumn.setCellValueFactory(new PropertyValueFactory<ClassStats, Integer>("increasedCount"));
        increasedColumn.setPrefWidth(80);

        TableColumn<ClassStats, Integer> decreasedColumn = new TableColumn<ClassStats, Integer>("Ниже");
        decreasedColumn.setCellValueFactory(new PropertyValueFactory<ClassStats, Integer>("decreasedCount"));
        decreasedColumn.setPrefWidth(80);

        TableColumn<ClassStats, String> matchedPercentColumn = new TableColumn<ClassStats, String>("% совпад.");
        matchedPercentColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format(Locale.US, "%.2f%%", cellData.getValue().getMatchedPercent())));
        matchedPercentColumn.setPrefWidth(100);

        classStatsTableView.getColumns().add(classColumn);
        classStatsTableView.getColumns().add(totalColumn);
        classStatsTableView.getColumns().add(absentColumn);
        classStatsTableView.getColumns().add(absentPercentColumn);
        classStatsTableView.getColumns().add(grade2Column);
        classStatsTableView.getColumns().add(grade3Column);
        classStatsTableView.getColumns().add(grade4Column);
        classStatsTableView.getColumns().add(grade5Column);
        classStatsTableView.getColumns().add(matchedColumn);
        classStatsTableView.getColumns().add(increasedColumn);
        classStatsTableView.getColumns().add(decreasedColumn);
        classStatsTableView.getColumns().add(matchedPercentColumn);

        classStatsTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    private VBox createClassIntervalsPanel() {
        Label title = new Label("Диапазоны номеров по классам");
        title.getStyleClass().add("card-title");

        Label hint = new Label(
                "Сначала загрузите Excel-файл. Затем заполните класс и включительный диапазон кодов участников. " +
                        "Класс будет определяться при нажатии «Применить и показать»."
        );
        hint.setWrapText(true);
        hint.getStyleClass().add("muted-text");

        Button addRowButton = new Button("Добавить диапазон");
        addRowButton.getStyleClass().add("secondary-button");
        addRowButton.setOnAction(event -> addClassIntervalRow("", "", ""));

        VBox box = new VBox(10, title, hint, classIntervalRowsBox, addRowButton);
        box.setPadding(new Insets(12));
        box.getStyleClass().add("card");

        return box;
    }

    private VBox createGradeRangesPanel(Button applyButton) {
        Label title = new Label("Интервалы оценок");
        title.getStyleClass().add("card-title");

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);

        addRangeRow(grid, 0, "Оценка 2:", grade2FromField, grade2ToField);
        addRangeRow(grid, 1, "Оценка 3:", grade3FromField, grade3ToField);
        addRangeRow(grid, 2, "Оценка 4:", grade4FromField, grade4ToField);
        addRangeRow(grid, 3, "Оценка 5:", grade5FromField, grade5ToField);

        Label hint = new Label("Интервалы включительные. После заполнения нажмите «Применить и показать».");
        hint.setWrapText(true);
        hint.getStyleClass().add("muted-text");

        VBox box = new VBox(10, title, grid, hint, applyButton);
        box.setPadding(new Insets(12));
        box.getStyleClass().add("card");

        return box;
    }

    private VBox createGradeStatsPanel() {
        Label title = new Label("Статистика оценок");
        title.getStyleClass().add("card-title");

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
        note.getStyleClass().add("muted-text");

        VBox box = new VBox(10, title, grid, note);
        box.setPadding(new Insets(12));
        box.getStyleClass().add("card");

        return box;
    }

    private VBox createComparisonStatsPanel() {
        Label title = new Label("Сравнение отметок");
        title.getStyleClass().add("card-title");

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
        note.getStyleClass().add("muted-text");

        VBox box = new VBox(10, title, grid, note);
        box.setPadding(new Insets(12));
        box.getStyleClass().add("card");

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

    private String formatTaskScores(List<Integer> scores) {
        if (scores == null || scores.isEmpty()) {
            return "";
        }

        return scores.stream()
                .map(score -> score == null ? "Х" : String.valueOf(score))
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
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