package com.sumit.genaiqna.util;

public class Stopwatch {
    private final long start;

    private Stopwatch() {
        this.start = System.currentTimeMillis();
    }

    public static Stopwatch start() {
        return new Stopwatch();
    }

    public long elapsedMillis() {
        return System.currentTimeMillis() - start;
    }
}
