package org.example;

import java.util.List;

public class ExamRecord {

    private final String studentId;
    private final boolean absent;

    private final List<String> variants;      // варианты по частям
    private final List<Integer> taskScores;   // null = "Х" / не приступал

    private final String classNumber;
    private final String gender;
    private final Integer previousPeriodMark;
    private final Integer totalScore;

    public ExamRecord(
            String studentId,
            boolean absent,
            List<String> variants,
            List<Integer> taskScores,
            String classNumber,
            String gender,
            Integer previousPeriodMark,
            Integer totalScore
    ) {
        this.studentId = studentId;
        this.absent = absent;
        this.variants = variants;
        this.taskScores = taskScores;
        this.classNumber = classNumber;
        this.gender = gender;
        this.previousPeriodMark = previousPeriodMark;
        this.totalScore = totalScore;
    }

    public String getStudentId() {
        return studentId;
    }

    public boolean isAbsent() {
        return absent;
    }

    public List<String> getVariants() {
        return variants;
    }

    public List<Integer> getTaskScores() {
        return taskScores;
    }

    public String getClassNumber() {
        return classNumber;
    }

    public String getGender() {
        return gender;
    }

    public Integer getPreviousPeriodMark() {
        return previousPeriodMark;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    @Override
    public String toString() {
        if (absent) {
            return "Student " + studentId +
                    " — не присутствовал" +
                    ", class=" + classNumber +
                    ", gender=" + gender +
                    ", prev=" + previousPeriodMark +
                    ", total=" + totalScore;
        }

        return "Student " + studentId +
                " variants=" + variants +
                " tasks=" + taskScores +
                " class=" + classNumber +
                " gender=" + gender +
                " prev=" + previousPeriodMark +
                " total=" + totalScore;
    }
}