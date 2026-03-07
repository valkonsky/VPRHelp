package org.example;

import java.util.List;

public class App {
    public static void main(String[] args) {
        ExamRecordSource source = new ExcelExamRecordSource("template.xlsx", 1);
        List<ExamRecord> records = source.loadRecords();

        for (ExamRecord record : records) {
            System.out.println(record);
        }
    }
}