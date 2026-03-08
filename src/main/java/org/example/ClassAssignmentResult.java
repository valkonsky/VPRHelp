package org.example;

import java.util.List;

public class ClassAssignmentResult {
    private final List<ExamRecord> records;
    private final int assignedCount;

    public ClassAssignmentResult(List<ExamRecord> records, int assignedCount) {
        this.records = records;
        this.assignedCount = assignedCount;
    }

    public List<ExamRecord> getRecords() {
        return records;
    }

    public int getAssignedCount() {
        return assignedCount;
    }
}
