package org.example;

public class ClassStats {

    private final String classNumber;
    private final int totalCount;
    private final int absentCount;
    private final double absentPercent;

    private final int grade2Count;
    private final int grade3Count;
    private final int grade4Count;
    private final int grade5Count;

    private final int matchedCount;
    private final int increasedCount;
    private final int decreasedCount;
    private final double matchedPercent;

    public ClassStats(
            String classNumber,
            int totalCount,
            int absentCount,
            double absentPercent,
            int grade2Count,
            int grade3Count,
            int grade4Count,
            int grade5Count,
            int matchedCount,
            int increasedCount,
            int decreasedCount,
            double matchedPercent
    ) {
        this.classNumber = classNumber;
        this.totalCount = totalCount;
        this.absentCount = absentCount;
        this.absentPercent = absentPercent;
        this.grade2Count = grade2Count;
        this.grade3Count = grade3Count;
        this.grade4Count = grade4Count;
        this.grade5Count = grade5Count;
        this.matchedCount = matchedCount;
        this.increasedCount = increasedCount;
        this.decreasedCount = decreasedCount;
        this.matchedPercent = matchedPercent;
    }

    public String getClassNumber() {
        return classNumber;
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

    public int getGrade2Count() {
        return grade2Count;
    }

    public int getGrade3Count() {
        return grade3Count;
    }

    public int getGrade4Count() {
        return grade4Count;
    }

    public int getGrade5Count() {
        return grade5Count;
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

    public double getMatchedPercent() {
        return matchedPercent;
    }
}