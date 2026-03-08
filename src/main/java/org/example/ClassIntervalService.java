package org.example;

import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ClassIntervalService {

    public List<ClassInterval> readClassIntervals(List<ClassIntervalInput> rows) {
        List<ClassInterval> intervals = new ArrayList<ClassInterval>();

        for (int i = 0; i < rows.size(); i++) {
            ClassIntervalInput row = rows.get(i);

            String className = safeTrim(row.getClassName());
            String fromText = safeTrim(row.getFromText());
            String toText = safeTrim(row.getToText());

            boolean allBlank = className.isEmpty() && fromText.isEmpty() && toText.isEmpty();
            if (allBlank) {
                continue;
            }

            if (className.isEmpty() || fromText.isEmpty() || toText.isEmpty()) {
                throw new IllegalArgumentException(
                        "Диапазон классов в строке " + (i + 1) + " заполнен не полностью. Нужны класс, от и до."
                );
            }

            int fromValue;
            int toValue;
            try {
                fromValue = Integer.parseInt(fromText);
                toValue = Integer.parseInt(toText);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "В диапазоне классов для строки " + (i + 1) + " границы должны быть целыми числами."
                );
            }

            if (fromValue > toValue) {
                throw new IllegalArgumentException(
                        "В диапазоне классов для строки " + (i + 1) + " левая граница больше правой."
                );
            }

            intervals.add(new ClassInterval(className, fromValue, toValue));
        }

        validateNoOverlaps(intervals);
        return intervals;
    }

    public ClassAssignmentResult applyClassIntervals(List<ExamRecord> records, List<ClassInterval> intervals) {
        List<ExamRecord> result = new ArrayList<ExamRecord>();
        int assignedCount = 0;

        for (ExamRecord record : records) {
            String classNumber = record.getClassNumber();

            if (classNumber == null || classNumber.trim().isEmpty()) {
                String inferredClass = findClassByStudentId(record.getStudentId(), intervals);
                if (inferredClass != null) {
                    classNumber = inferredClass;
                    assignedCount++;
                }
            }

            result.add(new ExamRecord(
                    record.getStudentId(),
                    record.isAbsent(),
                    record.getVariants(),
                    record.getTaskScores(),
                    classNumber,
                    record.getGender(),
                    record.getPreviousPeriodMark(),
                    record.getTotalScore()
            ));
        }

        return new ClassAssignmentResult(result, assignedCount);
    }

    private void validateNoOverlaps(List<ClassInterval> intervals) {
        List<ClassInterval> sortedIntervals = intervals.stream()
                .sorted(Comparator.comparingInt(ClassInterval::getFromStudentId))
                .collect(Collectors.toList());

        for (int i = 1; i < sortedIntervals.size(); i++) {
            ClassInterval previous = sortedIntervals.get(i - 1);
            ClassInterval current = sortedIntervals.get(i);

            if (current.getFromStudentId() <= previous.getToStudentId()) {
                throw new IllegalArgumentException(
                        "Диапазоны классов пересекаются: " + previous.getClassName() +
                                " (" + previous.getFromStudentId() + "-" + previous.getToStudentId() + ") и " +
                                current.getClassName() + " (" + current.getFromStudentId() + "-" + current.getToStudentId() + ")"
                );
            }
        }
    }

    private String findClassByStudentId(String studentId, List<ClassInterval> intervals) {
        for (ClassInterval interval : intervals) {
            if (interval.contains(studentId)) {
                return interval.getClassName();
            }
        }
        return null;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public static class ClassIntervalInput {
        private final String className;
        private final String fromText;
        private final String toText;

        public ClassIntervalInput(String className, String fromText, String toText) {
            this.className = className;
            this.fromText = fromText;
            this.toText = toText;
        }

        public ClassIntervalInput(TextField classField, TextField fromField, TextField toField) {
            this(
                    classField == null ? "" : classField.getText(),
                    fromField == null ? "" : fromField.getText(),
                    toField == null ? "" : toField.getText()
            );
        }

        public String getClassName() {
            return className;
        }

        public String getFromText() {
            return fromText;
        }

        public String getToText() {
            return toText;
        }
    }
}