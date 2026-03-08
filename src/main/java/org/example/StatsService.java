package org.example;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsService {

    private final GradeService gradeService;

    public StatsService(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    public AttendanceStats calculateAttendanceStats(List<ExamRecord> records) {
        int totalCount = records.size();
        int absentCount = (int) records.stream()
                .filter(ExamRecord::isAbsent)
                .count();

        double absentPercent = totalCount == 0
                ? 0.0
                : (absentCount * 100.0) / totalCount;

        return new AttendanceStats(totalCount, absentCount, absentPercent);
    }

    public GradeStats calculateGradeStats(
            List<ExamRecord> records,
            GradeRange range2,
            GradeRange range3,
            GradeRange range4,
            GradeRange range5
    ) {
        List<ExamRecord> eligibleRecords = records.stream()
                .filter(record -> !record.isAbsent())
                .filter(record -> record.getTotalScore() != null)
                .collect(Collectors.toList());

        int baseCount = eligibleRecords.size();

        int count2 = (int) eligibleRecords.stream()
                .filter(record -> range2.contains(record.getTotalScore()))
                .count();

        int count3 = (int) eligibleRecords.stream()
                .filter(record -> range3.contains(record.getTotalScore()))
                .count();

        int count4 = (int) eligibleRecords.stream()
                .filter(record -> range4.contains(record.getTotalScore()))
                .count();

        int count5 = (int) eligibleRecords.stream()
                .filter(record -> range5.contains(record.getTotalScore()))
                .count();

        return new GradeStats(
                count2, count3, count4, count5,
                formatPercent(count2, baseCount),
                formatPercent(count3, baseCount),
                formatPercent(count4, baseCount),
                formatPercent(count5, baseCount)
        );
    }

    public ComparisonStats calculateComparisonStats(
            List<ExamRecord> records,
            GradeRange range2,
            GradeRange range3,
            GradeRange range4,
            GradeRange range5
    ) {
        List<ExamRecord> comparableRecords = records.stream()
                .filter(record -> !record.isAbsent())
                .filter(record -> record.getPreviousPeriodMark() != null)
                .filter(record -> record.getTotalScore() != null)
                .collect(Collectors.toList());

        int baseCount = 0;
        int matchedCount = 0;
        int increasedCount = 0;
        int decreasedCount = 0;

        for (ExamRecord record : comparableRecords) {
            Integer currentGrade = gradeService.determineGrade(
                    record.getTotalScore(),
                    range2,
                    range3,
                    range4,
                    range5
            );

            Integer previousGrade = record.getPreviousPeriodMark();

            if (currentGrade == null) {
                continue;
            }

            baseCount++;

            if (currentGrade.intValue() == previousGrade.intValue()) {
                matchedCount++;
            } else if (currentGrade.intValue() > previousGrade.intValue()) {
                increasedCount++;
            } else {
                decreasedCount++;
            }
        }

        return new ComparisonStats(
                matchedCount,
                increasedCount,
                decreasedCount,
                formatPercent(matchedCount, baseCount)
        );
    }

    public List<ClassStats> buildClassStats(
            List<ExamRecord> currentRecords,
            GradeRange range2,
            GradeRange range3,
            GradeRange range4,
            GradeRange range5
    ) {
        Map<String, List<ExamRecord>> grouped = currentRecords.stream()
                .collect(Collectors.groupingBy(record -> normalizeClassNumber(record.getClassNumber())));

        return grouped.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey, this::compareClassNumbers))
                .map(entry -> buildSingleClassStats(entry.getKey(), entry.getValue(), range2, range3, range4, range5))
                .collect(Collectors.toList());
    }

    private ClassStats buildSingleClassStats(
            String classNumber,
            List<ExamRecord> records,
            GradeRange range2,
            GradeRange range3,
            GradeRange range4,
            GradeRange range5
    ) {
        int totalCount = records.size();
        int absentCount = (int) records.stream()
                .filter(ExamRecord::isAbsent)
                .count();

        double absentPercent = totalCount == 0
                ? 0.0
                : (absentCount * 100.0) / totalCount;

        List<ExamRecord> gradedRecords = records.stream()
                .filter(record -> !record.isAbsent())
                .filter(record -> record.getTotalScore() != null)
                .collect(Collectors.toList());

        int grade2Count = (int) gradedRecords.stream()
                .filter(record -> range2.contains(record.getTotalScore()))
                .count();

        int grade3Count = (int) gradedRecords.stream()
                .filter(record -> range3.contains(record.getTotalScore()))
                .count();

        int grade4Count = (int) gradedRecords.stream()
                .filter(record -> range4.contains(record.getTotalScore()))
                .count();

        int grade5Count = (int) gradedRecords.stream()
                .filter(record -> range5.contains(record.getTotalScore()))
                .count();

        int matchedCount = 0;
        int increasedCount = 0;
        int decreasedCount = 0;
        int comparisonBaseCount = 0;

        for (ExamRecord record : records) {
            if (record.isAbsent()) {
                continue;
            }
            if (record.getPreviousPeriodMark() == null || record.getTotalScore() == null) {
                continue;
            }

            Integer currentGrade = gradeService.determineGrade(
                    record.getTotalScore(),
                    range2,
                    range3,
                    range4,
                    range5
            );

            if (currentGrade == null) {
                continue;
            }

            comparisonBaseCount++;

            if (currentGrade.intValue() == record.getPreviousPeriodMark().intValue()) {
                matchedCount++;
            } else if (currentGrade.intValue() > record.getPreviousPeriodMark().intValue()) {
                increasedCount++;
            } else {
                decreasedCount++;
            }
        }

        double matchedPercent = comparisonBaseCount == 0
                ? 0.0
                : (matchedCount * 100.0) / comparisonBaseCount;

        return new ClassStats(
                classNumber,
                totalCount,
                absentCount,
                absentPercent,
                grade2Count,
                grade3Count,
                grade4Count,
                grade5Count,
                matchedCount,
                increasedCount,
                decreasedCount,
                matchedPercent
        );
    }

    private String normalizeClassNumber(String classNumber) {
        if (classNumber == null || classNumber.trim().isEmpty()) {
            return "Неизвестно";
        }
        return classNumber.trim();
    }

    private int compareClassNumbers(String left, String right) {
        String leftDigits = left.replaceAll("\\D+", "");
        String rightDigits = right.replaceAll("\\D+", "");

        boolean leftNumeric = !leftDigits.isEmpty();
        boolean rightNumeric = !rightDigits.isEmpty();

        if (leftNumeric && rightNumeric) {
            int numberCompare = Integer.compare(Integer.parseInt(leftDigits), Integer.parseInt(rightDigits));
            if (numberCompare != 0) {
                return numberCompare;
            }
        } else if (leftNumeric) {
            return -1;
        } else if (rightNumeric) {
            return 1;
        }

        return left.compareToIgnoreCase(right);
    }

    private String formatPercent(long count, int baseCount) {
        double percent = baseCount == 0 ? 0.0 : (count * 100.0) / baseCount;
        return String.format(Locale.US, "%.2f%%", percent);
    }

    public static class AttendanceStats {
        private final int totalCount;
        private final int absentCount;
        private final double absentPercent;

        public AttendanceStats(int totalCount, int absentCount, double absentPercent) {
            this.totalCount = totalCount;
            this.absentCount = absentCount;
            this.absentPercent = absentPercent;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public int getAbsentCount() {
            return absentCount;
        }

        public double getAbsentPercent() {
            return absentPercent;
        }
    }

    public static class GradeStats {
        private final int count2;
        private final int count3;
        private final int count4;
        private final int count5;
        private final String percent2;
        private final String percent3;
        private final String percent4;
        private final String percent5;

        public GradeStats(
                int count2,
                int count3,
                int count4,
                int count5,
                String percent2,
                String percent3,
                String percent4,
                String percent5
        ) {
            this.count2 = count2;
            this.count3 = count3;
            this.count4 = count4;
            this.count5 = count5;
            this.percent2 = percent2;
            this.percent3 = percent3;
            this.percent4 = percent4;
            this.percent5 = percent5;
        }

        public int getCount2() {
            return count2;
        }

        public int getCount3() {
            return count3;
        }

        public int getCount4() {
            return count4;
        }

        public int getCount5() {
            return count5;
        }

        public String getPercent2() {
            return percent2;
        }

        public String getPercent3() {
            return percent3;
        }

        public String getPercent4() {
            return percent4;
        }

        public String getPercent5() {
            return percent5;
        }
    }

    public static class ComparisonStats {
        private final int matchedCount;
        private final int increasedCount;
        private final int decreasedCount;
        private final String matchedPercent;

        public ComparisonStats(int matchedCount, int increasedCount, int decreasedCount, String matchedPercent) {
            this.matchedCount = matchedCount;
            this.increasedCount = increasedCount;
            this.decreasedCount = decreasedCount;
            this.matchedPercent = matchedPercent;
        }

        public int getMatchedCount() {
            return matchedCount;
        }

        public int getIncreasedCount() {
            return increasedCount;
        }

        public int getDecreasedCount() {
            return decreasedCount;
        }

        public String getMatchedPercent() {
            return matchedPercent;
        }
    }
}