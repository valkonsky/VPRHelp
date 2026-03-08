package org.example;

import javafx.scene.control.TextField;

public class GradeService {

    public GradeRange readRange(TextField fromField, TextField toField, String gradeName) {
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

    public Integer determineGrade(
            Integer totalScore,
            GradeRange range2,
            GradeRange range3,
            GradeRange range4,
            GradeRange range5
    ) {
        if (totalScore == null) {
            return null;
        }

        if (range2.contains(totalScore)) {
            return 2;
        }
        if (range3.contains(totalScore)) {
            return 3;
        }
        if (range4.contains(totalScore)) {
            return 4;
        }
        if (range5.contains(totalScore)) {
            return 5;
        }

        return null;
    }

    public Integer determineCurrentGradeSafe(
            Integer totalScore,
            TextField grade2FromField,
            TextField grade2ToField,
            TextField grade3FromField,
            TextField grade3ToField,
            TextField grade4FromField,
            TextField grade4ToField,
            TextField grade5FromField,
            TextField grade5ToField
    ) {
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

    public String buildComparisonText(
            ExamRecord record,
            TextField grade2FromField,
            TextField grade2ToField,
            TextField grade3FromField,
            TextField grade3ToField,
            TextField grade4FromField,
            TextField grade4ToField,
            TextField grade5FromField,
            TextField grade5ToField
    ) {
        Integer previousMark = record.getPreviousPeriodMark();
        Integer currentMark = determineCurrentGradeSafe(
                record.getTotalScore(),
                grade2FromField,
                grade2ToField,
                grade3FromField,
                grade3ToField,
                grade4FromField,
                grade4ToField,
                grade5FromField,
                grade5ToField
        );

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
}