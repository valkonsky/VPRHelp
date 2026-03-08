package org.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Locale;

public class ClassStatsTableBuilder {

    public void setupClassStatsTable(TableView<ClassStats> classStatsTableView) {
        classStatsTableView.getColumns().clear();

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
}