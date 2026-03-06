package org.example;

import java.io.IOException;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) throws IOException {
        ExamRecordSource source = new ExcelExamRecordSource("template.xlsx", 1);
        List<ExamRecord> records = source.loadRecords();

        for (ExamRecord record : records) {
            System.out.println(record);
        }
    }
}
