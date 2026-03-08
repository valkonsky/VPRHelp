package org.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ExamTableBuilder {

    private final ParticipantRegistry participantRegistry;
    private final GradeService gradeService;

    public ExamTableBuilder(ParticipantRegistry participantRegistry, GradeService gradeService) {
        this.participantRegistry = participantRegistry;
        this.gradeService = gradeService;
    }

    public void setupTable(
            TableView<ExamRecord> tableView,
            TextField grade2FromField,
            TextField grade2ToField,
            TextField grade3FromField,
            TextField grade3ToField,
            TextField grade4FromField,
            TextField grade4ToField,
            TextField grade5FromField,
            TextField grade5ToField
    ) {
        tableView.getColumns().clear();

        TableColumn<ExamRecord, String> studentIdColumn =
                new TableColumn<>("Код участника");
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studentIdColumn.setPrefWidth(80);

        TableColumn<ExamRecord, String> fullNameColumn =
                new TableColumn<>("ФИО");
        fullNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        safeString(participantRegistry.getNameByCode(cellData.getValue().getStudentId()))
                )
        );
        fullNameColumn.setPrefWidth(220);

        TableColumn<ExamRecord, String> variantsColumn =
                new TableColumn<>("Варианты");
        variantsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatVariants(cellData.getValue().getVariants()))
        );
        variantsColumn.setPrefWidth(100);

        TableColumn<ExamRecord, String> taskScoresColumn =
                new TableColumn<>("Баллы");
        taskScoresColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatTaskScores(cellData.getValue().getTaskScores()))
        );
        taskScoresColumn.setPrefWidth(180);

        TableColumn<ExamRecord, String> classNumberColumn =
                new TableColumn<>("Класс");
        classNumberColumn.setCellValueFactory(new PropertyValueFactory<>("classNumber"));
        classNumberColumn.setPrefWidth(70);

        TableColumn<ExamRecord, String> genderColumn =
                new TableColumn<>("Пол");
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderColumn.setPrefWidth(70);

        TableColumn<ExamRecord, String> previousMarkColumn =
                new TableColumn<>("Оценка за четверть/полугодие");
        previousMarkColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(toDisplayString(cellData.getValue().getPreviousPeriodMark()))
        );
        previousMarkColumn.setPrefWidth(190);

        TableColumn<ExamRecord, String> totalScoreColumn =
                new TableColumn<>("Итого баллов");
        totalScoreColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(toDisplayString(cellData.getValue().getTotalScore()))
        );
        totalScoreColumn.setPrefWidth(110);

        TableColumn<ExamRecord, String> currentMarkColumn =
                new TableColumn<>("Текущая отметка");
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
        currentMarkColumn.setPrefWidth(130);

        TableColumn<ExamRecord, String> comparisonColumn =
                new TableColumn<>("Сравнение");
        comparisonColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        safeString(
                                gradeService.buildComparisonText(
                                        cellData.getValue(),
                                        grade2FromField, grade2ToField,
                                        grade3FromField, grade3ToField,
                                        grade4FromField, grade4ToField,
                                        grade5FromField, grade5ToField
                                )
                        )
                )
        );
        comparisonColumn.setPrefWidth(110);

        tableView.getColumns().addAll(
                studentIdColumn,
                fullNameColumn,
                variantsColumn,
                taskScoresColumn,
                classNumberColumn,
                genderColumn,
                previousMarkColumn,
                totalScoreColumn,
                currentMarkColumn,
                comparisonColumn
        );

        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ExamRecord item, boolean empty) {
                super.updateItem(item, empty);

                getStyleClass().removeAll(
                        "score-row-2",
                        "score-row-3",
                        "score-row-4",
                        "score-row-5",
                        "absent-row"
                );

                if (empty || item == null) {
                    return;
                }

                if (item.isAbsent()) {
                    getStyleClass().add("absent-row");
                    return;
                }

                Integer total = item.getTotalScore();
                if (total == null) {
                    return;
                }

                Integer currentMark = gradeService.determineCurrentGradeSafe(
                        total,
                        grade2FromField, grade2ToField,
                        grade3FromField, grade3ToField,
                        grade4FromField, grade4ToField,
                        grade5FromField, grade5ToField
                );

                if (currentMark == null) {
                    return;
                }

                switch (currentMark) {
                    case 2 -> getStyleClass().add("score-row-2");
                    case 3 -> getStyleClass().add("score-row-3");
                    case 4 -> getStyleClass().add("score-row-4");
                    case 5 -> getStyleClass().add("score-row-5");
                }
            }
        });
    }

    private String formatVariants(java.util.List<String> variants) {
        if (variants == null || variants.isEmpty()) {
            return "";
        }

        return variants.stream()
                .map(this::safeString)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private String formatTaskScores(java.util.List<Integer> scores) {
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

    private String safeString(String value) {
        return value == null ? "" : value;
    }
}