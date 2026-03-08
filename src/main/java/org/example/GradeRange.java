package org.example;

public class GradeRange {
    private final int from;
    private final int to;

    public GradeRange(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public boolean contains(int value) {
        return value >= from && value <= to;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }
}