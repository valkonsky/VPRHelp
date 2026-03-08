package org.example;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class RightPanelBuilder {

    public VBox createRightPanel(
            VBox classIntervalRowsBox,
            Runnable onAddIntervalRow,
            Button applyButton,
            TextField grade2FromField,
            TextField grade2ToField,
            TextField grade3FromField,
            TextField grade3ToField,
            TextField grade4FromField,
            TextField grade4ToField,
            TextField grade5FromField,
            TextField grade5ToField,
            Label grade2CountLabel,
            Label grade2PercentLabel,
            Label grade3CountLabel,
            Label grade3PercentLabel,
            Label grade4CountLabel,
            Label grade4PercentLabel,
            Label grade5CountLabel,
            Label grade5PercentLabel,
            Label matchedCountLabel,
            Label increasedCountLabel,
            Label decreasedCountLabel,
            Label matchedPercentLabel
    ) {
        VBox panel = new VBox(
                12,
                createClassIntervalsPanel(classIntervalRowsBox, onAddIntervalRow),
                createGradeRangesPanel(
                        applyButton,
                        grade2FromField, grade2ToField,
                        grade3FromField, grade3ToField,
                        grade4FromField, grade4ToField,
                        grade5FromField, grade5ToField
                ),
                createGradeStatsPanel(
                        grade2CountLabel, grade2PercentLabel,
                        grade3CountLabel, grade3PercentLabel,
                        grade4CountLabel, grade4PercentLabel,
                        grade5CountLabel, grade5PercentLabel
                ),
                createComparisonStatsPanel(
                        matchedCountLabel,
                        increasedCountLabel,
                        decreasedCountLabel,
                        matchedPercentLabel
                )
        );

        panel.setPadding(new Insets(10));
        panel.setPrefWidth(390);
        panel.getStyleClass().add("right-panel");

        return panel;
    }

    private VBox createClassIntervalsPanel(VBox classIntervalRowsBox, Runnable onAddIntervalRow) {
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
        addRowButton.setOnAction(event -> onAddIntervalRow.run());

        VBox box = new VBox(10, title, hint, classIntervalRowsBox, addRowButton);
        box.setPadding(new Insets(12));
        box.getStyleClass().add("card");

        return box;
    }

    private VBox createGradeRangesPanel(
            Button applyButton,
            TextField grade2FromField,
            TextField grade2ToField,
            TextField grade3FromField,
            TextField grade3ToField,
            TextField grade4FromField,
            TextField grade4ToField,
            TextField grade5FromField,
            TextField grade5ToField
    ) {
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

    private VBox createGradeStatsPanel(
            Label grade2CountLabel,
            Label grade2PercentLabel,
            Label grade3CountLabel,
            Label grade3PercentLabel,
            Label grade4CountLabel,
            Label grade4PercentLabel,
            Label grade5CountLabel,
            Label grade5PercentLabel
    ) {
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

    private VBox createComparisonStatsPanel(
            Label matchedCountLabel,
            Label increasedCountLabel,
            Label decreasedCountLabel,
            Label matchedPercentLabel
    ) {
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
}