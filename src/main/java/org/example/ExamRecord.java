package org.example;

import java.util.List;

public class ExamRecord {
    private String studentId;
    private List<Integer> marks;
    private Integer totalScore;

    public ExamRecord(String studentId, List<Integer> marks, Integer totalScore) {
        this.studentId = studentId;
        this.marks = marks;
        this.totalScore = totalScore;
    }

    public String getStudentId() {
        return studentId;
    }

    public List<Integer> getMarks() {
        return marks;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    @Override
    public String toString() {
        return studentId + " " + marks;
    }
}
