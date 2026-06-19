package com.library.infrastructure.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LocalBucketRateLimiter implements RateLimiter {

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean tryConsume(String key, long limit, long periodInSeconds) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> createNewBucket(limit, periodInSeconds));
        return bucket.tryConsume(1);
    }

    private Bucket createNewBucket(long limit, long periodInSeconds) {
        Refill refill = Refill.greedy(limit, Duration.ofSeconds(periodInSeconds));
        Bandwidth limitBandwidth = Bandwidth.classic(limit, refill);
        return Bucket.builder().addLimit(limitBandwidth).build();
    }
}
