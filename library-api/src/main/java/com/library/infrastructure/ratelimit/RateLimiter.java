package com.library.infrastructure.ratelimit;

public interface RateLimiter {
    boolean tryConsume(String key, long limit, long periodInSeconds);
}
