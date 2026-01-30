package com.sumit.genaiqna.controller;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class RateLimiter {
    public static final int MAX_REQUESTS = 20;
    public static final long WINDOW_MILLIS = 60_000;

    private final Map<String, Counter> counters = new ConcurrentHashMap<>(); // Thread Safe

    public boolean allowRequest(String apiKey) {
        long now = Instant.now().toEpochMilli();
        Counter counter = counters.compute(apiKey, (key, existing) -> {
            if (existing == null || now - existing.windowStart > WINDOW_MILLIS) {
                return new Counter(1, now);
            }
            existing.count++;
            return existing;
        });

        return counter.count <= MAX_REQUESTS;
    }

    private static class Counter {
        int count;
        long windowStart;

        Counter(int count, long windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }
}
