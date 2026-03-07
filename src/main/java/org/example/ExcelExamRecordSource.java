package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class ExcelExamRecordSource implements ExamRecordSource {

    private static final Pattern TASK_HEADER_PATTERN = Pattern.compile("^\\d+\\s*\\(.*\\)$");
    private static final String ABSENT_MARK = "не присутствовал";

    private final String filename;
    private final int sheetIndex;

    public ExcelExamRecordSource(String filename, int sheetIndex) {
        this.filename = filename;
        this.sheetIndex = sheetIndex;
    }

    @Override
    public List<ExamRecord> loadRecords() {
        List<ExamRecord> records = new ArrayList<ExamRecord>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filename);
             Workbook workbook = new XSSFWorkbook(fis)) {

            if (sheetIndex < 0 || sheetIndex >= workbook.getNumberOfSheets()) {
                throw new IllegalArgumentException("Некорректный индекс листа: " + sheetIndex);
            }

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());

            if (headerRow == null) {
                return records;
            }

            SheetSchema schema = parseSchema(headerRow, formatter, evaluator);

            for (int rowIndex = headerRow.getRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                String studentId = getCellValue(row, schema.studentIdIndex, formatter, evaluator);
                if (studentId.isEmpty() || !studentId.matches("\\d+")) {
                    continue;
                }

                boolean absent = false;
                List<String> variants = new ArrayList<String>();

                for (Integer variantIndex : schema.variantIndexes) {
                    String variantValue = getCellValue(row, variantIndex.intValue(), formatter, evaluator);

                    if (variantValue.equalsIgnoreCase(ABSENT_MARK)) {
                        absent = true;
                    }

                    variants.add(variantValue);
                }

                List<Integer> taskScores = new ArrayList<Integer>();
                if (!absent) {
                    for (Integer taskIndex : schema.taskIndexes) {
                        String value = getCellValue(row, taskIndex.intValue(), formatter, evaluator);
                        taskScores.add(parseTaskValue(value));
                    }
                }

                String classNumber = getCellValue(row, schema.classNumberIndex, formatter, evaluator);
                String gender = getCellValue(row, schema.genderIndex, formatter, evaluator);

                Integer previousPeriodMark = parseIntegerOrNull(
                        getCellValue(row, schema.previousPeriodMarkIndex, formatter, evaluator)
                );

                Integer totalScore = parseIntegerOrNull(
                        getCellValue(row, schema.totalScoreIndex, formatter, evaluator)
                );

                records.add(new ExamRecord(
                        studentId,
                        absent,
                        variants,
                        taskScores,
                        classNumber,
                        gender,
                        previousPeriodMark,
                        totalScore
                ));
            }

            return records;

        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка чтения Excel: " + filename, e);
        }
    }

    private SheetSchema parseSchema(Row headerRow, DataFormatter formatter, FormulaEvaluator evaluator) {
        Integer studentIdIndex = null;
        Integer classNumberIndex = null;
        Integer genderIndex = null;
        Integer previousPeriodMarkIndex = null;
        Integer totalScoreIndex = null;

        List<Integer> variantIndexes = new ArrayList<Integer>();
        List<Integer> taskIndexes = new ArrayList<Integer>();

        short lastCellNum = headerRow.getLastCellNum();

        for (int i = 0; i < lastCellNum; i++) {
            String header = getCellValue(headerRow, i, formatter, evaluator);
            String normalized = normalize(header);

            if (normalized.contains("код участника")) {
                studentIdIndex = Integer.valueOf(i);
            } else if (normalized.contains("вариант")) {
                variantIndexes.add(Integer.valueOf(i));
            } else if (normalized.contains("порядковый номер класса")) {
                classNumberIndex = Integer.valueOf(i);
            } else if (normalized.equals("пол")) {
                genderIndex = Integer.valueOf(i);
            } else if (normalized.contains("отметка за предыдущ")) {
                previousPeriodMarkIndex = Integer.valueOf(i);
            } else if (normalized.contains("итого баллов")) {
                totalScoreIndex = Integer.valueOf(i);
            } else if (isTaskHeader(header)) {
                taskIndexes.add(Integer.valueOf(i));
            }
        }

        if (studentIdIndex == null) {
            throw new IllegalStateException("Не найдена колонка 'Код участника'");
        }

        if (variantIndexes.isEmpty()) {
            throw new IllegalStateException("Не найдены колонки 'Вариант'");
        }

        if (classNumberIndex == null || genderIndex == null
                || previousPeriodMarkIndex == null || totalScoreIndex == null) {
            throw new IllegalStateException("Не удалось определить служебные колонки");
        }

        Collections.sort(variantIndexes);
        taskIndexes.sort(Comparator.naturalOrder());

        return new SheetSchema(
                studentIdIndex.intValue(),
                variantIndexes,
                taskIndexes,
                classNumberIndex.intValue(),
                genderIndex.intValue(),
                previousPeriodMarkIndex.intValue(),
                totalScoreIndex.intValue()
        );
    }

    private boolean isTaskHeader(String header) {
        if (header == null) {
            return false;
        }

        String trimmed = header.trim();
        return TASK_HEADER_PATTERN.matcher(trimmed).matches();
    }

    private Integer parseTaskValue(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        if (trimmed.equalsIgnoreCase("х") || trimmed.equalsIgnoreCase("x")) {
            return null;
        }

        try {
            return Integer.valueOf(parseNumericString(trimmed));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseIntegerOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Integer.valueOf(parseNumericString(value.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String parseNumericString(String value) {
        String normalized = value.replace(" ", "").replace(",", ".");
        double parsed = Double.parseDouble(normalized);
        return String.valueOf((int) parsed);
    }

    private String getCellValue(Row row, int cellIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? "" : formatter.formatCellValue(cell, evaluator).trim();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static class SheetSchema {
        private final int studentIdIndex;
        private final List<Integer> variantIndexes;
        private final List<Integer> taskIndexes;
        private final int classNumberIndex;
        private final int genderIndex;
        private final int previousPeriodMarkIndex;
        private final int totalScoreIndex;

        private SheetSchema(
                int studentIdIndex,
                List<Integer> variantIndexes,
                List<Integer> taskIndexes,
                int classNumberIndex,
                int genderIndex,
                int previousPeriodMarkIndex,
                int totalScoreIndex
        ) {
            this.studentIdIndex = studentIdIndex;
            this.variantIndexes = variantIndexes;
            this.taskIndexes = taskIndexes;
            this.classNumberIndex = classNumberIndex;
            this.genderIndex = genderIndex;
            this.previousPeriodMarkIndex = previousPeriodMarkIndex;
            this.totalScoreIndex = totalScoreIndex;
        }
    }
}