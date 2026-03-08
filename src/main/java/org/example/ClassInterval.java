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
        Integer numericId = extractStudentIdNumber(studentId);
        if (numericId == null) {
            return false;
        }
        return numericId >= fromStudentId && numericId <= toStudentId;
    }

    private Integer extractStudentIdNumber(String studentId) {
        if (studentId == null) {
            return null;
        }

        String normalized = studentId.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        normalized = normalized.replace(',', '.');

        if (normalized.matches("\\d+\\.0+")) {
            normalized = normalized.substring(0, normalized.indexOf('.'));
        }

        String digitsOnly = normalized.replaceAll("\\D+", "");
        if (digitsOnly.isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(digitsOnly);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}