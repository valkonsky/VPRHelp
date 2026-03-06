package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelExamRecordSource implements ExamRecordSource{
    private final String filename;
    private final int sheetIndex;

    public ExcelExamRecordSource(String filename,int sheetIndex) throws IOException {
        this.filename = filename;
        this.sheetIndex = sheetIndex;
    }
    @Override
    public List<ExamRecord> loadRecords() {
        List<ExamRecord> records = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filename);
             Workbook workbook = new XSSFWorkbook(fis)) {

            if (sheetIndex < 0 || sheetIndex >= workbook.getNumberOfSheets()) {
                throw new IllegalArgumentException("Некорректный индекс листа: " + sheetIndex);
            }

            Sheet sheet = workbook.getSheetAt(sheetIndex);

            for (Row row : sheet) {
                Cell firstCell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                if (firstCell == null) {
                    continue;
                }

                String studentId = formatter.formatCellValue(firstCell).trim();

                if (studentId.isEmpty() || !studentId.matches("\\d+")) {
                    continue;
                }

                List<Integer> marks = new ArrayList<>();
                Integer totalScore = null;

                for (int i = 1; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                    if (cell == null) {
                        continue;
                    }

                    String value = formatter.formatCellValue(cell).trim();

                    if (value.isEmpty()) {
                        continue;
                    }

                    if (i == row.getLastCellNum() - 1) {
                        try {
                            totalScore = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            totalScore = null;
                        }
                    } else {
                        try {
                            marks.add(Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            // пропускаем нечисловые ячейки
                        }
                    }
                }

                records.add(new ExamRecord(studentId, marks, totalScore));
            }

        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения Excel: " + filename, e);
        }

        return records;
    }

}

