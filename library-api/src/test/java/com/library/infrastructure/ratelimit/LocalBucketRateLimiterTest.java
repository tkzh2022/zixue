package com.library.infrastructure.ratelimit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalBucketRateLimiterTest {

    @Test
    void shouldLimitRequests() {
        LocalBucketRateLimiter rateLimiter = new LocalBucketRateLimiter();
        String key = "test-ip";

        for (int i = 0; i < 5; i++) {
            assertThat(rateLimiter.tryConsume(key, 5, 60)).isTrue();
        }

        assertThat(rateLimiter.tryConsume(key, 5, 60)).isFalse();
    }
}
