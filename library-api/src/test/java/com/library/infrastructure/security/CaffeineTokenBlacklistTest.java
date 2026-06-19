package com.library.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CaffeineTokenBlacklistTest {

    private CaffeineTokenBlacklist blacklist;

    @BeforeEach
    void setUp() {
        blacklist = new CaffeineTokenBlacklist();
    }

    @Test
    void shouldAddAndContain() {
        blacklist.add("jti-123", Duration.ofMinutes(5));
        assertThat(blacklist.contains("jti-123")).isTrue();
        assertThat(blacklist.contains("jti-456")).isFalse();
    }

    @Test
    void shouldExpireAfterTtl() throws InterruptedException {
        blacklist.add("jti-123", Duration.ofMillis(10));
        assertThat(blacklist.contains("jti-123")).isTrue();

        Thread.sleep(20);

        assertThat(blacklist.contains("jti-123")).isFalse();
    }
}
