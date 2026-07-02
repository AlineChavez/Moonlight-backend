package com.moonlight.security;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal in-memory sliding-window limiter to slow down brute-force login attempts.
 * Per-process only (won't coordinate across multiple instances), which is fine given
 * the rest of this app's state is also in-memory and single-instance.
 */
public class RateLimiter {

    private final int maxAttempts;
    private final long windowMillis;
    private final Map<String, Deque<Long>> attemptsByKey = new ConcurrentHashMap<>();

    public RateLimiter(int maxAttempts, long windowMillis) {
        this.maxAttempts = maxAttempts;
        this.windowMillis = windowMillis;
    }

    /**
     * Records an attempt for the given key and returns true if it is still within the allowed rate.
     */
    public synchronized boolean allow(String key) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = attemptsByKey.computeIfAbsent(key, k -> new ArrayDeque<>());

        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMillis) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxAttempts) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }
}
