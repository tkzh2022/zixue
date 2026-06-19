package com.library.application.auth;

import com.library.application.auth.command.LoginCommand;
import com.library.application.auth.command.RefreshCommand;
import com.library.application.auth.command.RegisterCommand;
import com.library.application.auth.result.AuthResult;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import com.library.domain.user.RefreshToken;
import com.library.domain.user.RefreshTokenRepository;
import com.library.domain.user.UserAccount;
import com.library.domain.user.UserAccountRepository;
import com.library.domain.user.UserRole;
import com.library.infrastructure.alert.AlertSender;
import com.library.infrastructure.security.JwtProperties;
import com.library.infrastructure.security.JwtService;
import com.library.infrastructure.security.LoginAttemptService;
import com.library.infrastructure.security.TokenBlacklist;
import com.library.domain.reader.ReaderRepository;
import com.library.domain.reader.Reader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {

    @Mock UserAccountRepository userAccountRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock LoginAttemptService loginAttemptService;
    @Mock TokenBlacklist tokenBlacklist;
    @Mock AlertSender alertSender;
    @Mock ReaderRepository readerRepository;

    private AuthApplicationService authApplicationService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setAccessTtl(Duration.ofMinutes(15));
        props.setRefreshTtl(Duration.ofDays(7));

        authApplicationService = new AuthApplicationService(
                userAccountRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtService,
                props,
                loginAttemptService,
                tokenBlacklist,
                alertSender,
                readerRepository
        );
    }

    @Test
    void registerShouldCreateUserAndReturnTokens() {
        RegisterCommand cmd = new RegisterCommand("bob", "password", "Bob", "123", "bob@test.com", "127.0.0.1", "agent");
        when(userAccountRepository.existsByUsername("bob")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hash");
        
        UserAccount account = UserAccount.register("bob", "hash", UserRole.READER, Instant.now());
        account.assignId(1L);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(account);
        when(jwtService.issueAccess(eq(account), any(Instant.class))).thenReturn("access-token");
        when(jwtService.issueRefreshTokenString()).thenReturn("refresh-token-raw");

        AuthResult result = authApplicationService.register(cmd);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token-raw");
        verify(userAccountRepository).save(any(UserAccount.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(readerRepository).save(any(Reader.class));
    }

    @Test
    void loginShouldFailIfBlocked() {
        LoginCommand cmd = new LoginCommand("bob", "pass", "127.0.0.1", "agent");
        when(loginAttemptService.isBlocked("bob")).thenReturn(true);

        assertThatThrownBy(() -> authApplicationService.login(cmd))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).resultCode())
                .isEqualTo(ResultCode.AUTH_ACCOUNT_LOCKED);
    }

    @Test
    void loginShouldSucceedAndResetAttempts() {
        LoginCommand cmd = new LoginCommand("bob", "pass", "127.0.0.1", "agent");
        when(loginAttemptService.isBlocked("bob")).thenReturn(false);
        
        UserAccount account = UserAccount.register("bob", "hash", UserRole.READER, Instant.now());
        account.assignId(1L);
        when(userAccountRepository.findByUsername("bob")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);
        when(jwtService.issueAccess(eq(account), any(Instant.class))).thenReturn("access-token");
        when(jwtService.issueRefreshTokenString()).thenReturn("refresh-token-raw");

        AuthResult result = authApplicationService.login(cmd);

        assertThat(result.accessToken()).isEqualTo("access-token");
        verify(loginAttemptService).loginSucceeded("bob");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refreshShouldRevokeAllOnReplay() {
        RefreshCommand cmd = new RefreshCommand("refresh-token-raw", "127.0.0.1", "agent");
        
        RefreshToken token = RefreshToken.issue(1L, "hash", Instant.now(), Instant.now().plusSeconds(100), "agent", "127.0.0.1");
        token.revoke(Instant.now(), "Rotated");
        
        when(refreshTokenRepository.findByHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authApplicationService.refresh(cmd))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).resultCode())
                .isEqualTo(ResultCode.AUTH_REFRESH_TOKEN_INVALID);

        verify(refreshTokenRepository).revokeAllByUser(eq(1L), any(Instant.class), anyString());
        verify(alertSender).sendError(anyString(), anyString(), any());
    }
}
