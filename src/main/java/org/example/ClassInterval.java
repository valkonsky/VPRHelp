package org.example;

public class ClassInterval {

    private final String className;
    private final int fromStudentId;
    private final int toStudentId;

    public ClassInterval(String className, int fromStudentId, int toStudentId) {
        this.className = className;
        this.fromStudentId = fromStudentId;
        this.toStudentId = toStudentId;
    }

    public String getClassName() {
        return className;
    }

    public int getFromStudentId() {
        return fromStudentId;
    }

    public int getToStudentId() {
        return toStudentId;
    }

    public boolean contains(String studentId) {
        if (studentId == null || !studentId.matches("\\d+")) {
            return false;
        }

        int value = Integer.parseInt(studentId);
        return value >= fromStudentId && value <= toStudentId;
    }
}