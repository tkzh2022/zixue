package com.library.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    @Test
    void shouldBlockAfterMaxAttempts() {
        String ip = "192.168.1.1";

        for (int i = 0; i < 4; i++) {
            loginAttemptService.loginFailed(ip);
            assertThat(loginAttemptService.isBlocked(ip)).isFalse();
        }

        loginAttemptService.loginFailed(ip);
        assertThat(loginAttemptService.isBlocked(ip)).isTrue();
    }

    @Test
    void shouldResetOnSuccess() {
        String ip = "192.168.1.1";

        loginAttemptService.loginFailed(ip);
        loginAttemptService.loginFailed(ip);
        assertThat(loginAttemptService.isBlocked(ip)).isFalse();

        loginAttemptService.loginSucceeded(ip);

        for (int i = 0; i < 4; i++) {
            loginAttemptService.loginFailed(ip);
            assertThat(loginAttemptService.isBlocked(ip)).isFalse();
        }

        loginAttemptService.loginFailed(ip);
        assertThat(loginAttemptService.isBlocked(ip)).isTrue();
    }
}
