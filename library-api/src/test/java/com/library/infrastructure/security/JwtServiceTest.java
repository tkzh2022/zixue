package com.library.infrastructure.security;

import com.library.domain.user.UserAccount;
import com.library.domain.user.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    @Test
    void shouldFailFastIfSecretIsTooShort() {
        JwtProperties props = new JwtProperties();
        props.setSecret("short");

        assertThatThrownBy(() -> new JwtService(props))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void shouldIssueAndParseAccessToken() {
        JwtProperties props = new JwtProperties();
        props.setSecret("this-is-a-very-long-secret-key-for-testing-purposes");
        props.setIssuer("test-issuer");
        props.setAccessTtl(Duration.ofMinutes(15));
        JwtService jwtService = new JwtService(props);

        UserAccount account = UserAccount.register("bob", "hash", UserRole.READER, Instant.now());
        account.assignId(42L);

        Instant now = Instant.now();
        String token = jwtService.issueAccess(account, now);

        assertThat(token).isNotBlank();

        JwtService.ParsedJwt parsed = jwtService.parse(token);
        assertThat(parsed.userId()).isEqualTo(42L);
        assertThat(parsed.role()).isEqualTo("READER");
        assertThat(parsed.jti()).isNotBlank();
        assertThat(parsed.expiresAt()).isAfter(now);
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtProperties props = new JwtProperties();
        props.setSecret("this-is-a-very-long-secret-key-for-testing-purposes");
        props.setIssuer("test-issuer");
        props.setAccessTtl(Duration.ofMillis(1));
        JwtService jwtService = new JwtService(props);

        UserAccount account = UserAccount.register("bob", "hash", UserRole.READER, Instant.now());
        account.assignId(42L);

        String token = jwtService.issueAccess(account, Instant.now());

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> jwtService.parse(token))
                .isInstanceOf(JwtService.JwtValidationException.class);
    }
}
