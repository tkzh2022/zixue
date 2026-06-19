package com.library.application.auth;

import com.library.application.auth.command.LoginCommand;
import com.library.application.auth.command.RefreshCommand;
import com.library.application.auth.command.RegisterCommand;
import com.library.application.auth.result.AuthResult;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import com.library.domain.reader.Reader;
import com.library.domain.reader.ReaderRepository;
import com.library.domain.user.RefreshToken;
import com.library.domain.user.RefreshTokenRepository;
import com.library.domain.user.UserAccount;
import com.library.domain.user.UserAccountRepository;
import com.library.domain.user.UserRole;
import com.library.domain.user.UserStatus;
import com.library.infrastructure.alert.AlertSender;
import com.library.infrastructure.security.JwtProperties;
import com.library.infrastructure.security.JwtService;
import com.library.infrastructure.security.LoginAttemptService;
import com.library.infrastructure.security.TokenBlacklist;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceExtendedTest {

    @Mock UserAccountRepository userAccountRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock LoginAttemptService loginAttemptService;
    @Mock TokenBlacklist tokenBlacklist;
    @Mock AlertSender alertSender;
    @Mock ReaderRepository readerRepository;

    private AuthApplicationService service;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setAccessTtl(Duration.ofMinutes(15));
        props.setRefreshTtl(Duration.ofDays(7));

        service = new AuthApplicationService(
                userAccountRepository, refreshTokenRepository, passwordEncoder,
                jwtService, props, loginAttemptService, tokenBlacklist, alertSender,
                readerRepository
        );
    }

    @Test
    void loginShouldRecordFailedAttemptOnWrongPassword() {
        LoginCommand cmd = new LoginCommand("bob", "wrong", "127.0.0.1", "agent");
        when(loginAttemptService.isBlocked("bob")).thenReturn(false);
        UserAccount account = UserAccount.register("bob", "hash", UserRole.READER, Instant.now());
        account.assignId(1L);
        when(userAccountRepository.findByUsername("bob")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(cmd))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).resultCode())
                .isEqualTo(ResultCode.AUTH_USERNAME_OR_PASSWORD_INCORRECT);

        verify(loginAttemptService).loginFailed("bob");
    }

    @Test
    void loginShouldRecordFailedAttemptOnNonexistentUser() {
        LoginCommand cmd = new LoginCommand("ghost", "pass", "127.0.0.1", "agent");
        when(loginAttemptService.isBlocked("ghost")).thenReturn(false);
        when(userAccountRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(cmd))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).resultCode())
                .isEqualTo(ResultCode.AUTH_USERNAME_OR_PASSWORD_INCORRECT);

        verify(loginAttemptService).loginFailed("ghost");
    }

    @Test
    void loginShouldRejectDisabledAccount() {
        LoginCommand cmd = new LoginCommand("disabled", "pass", "127.0.0.1", "agent");
        when(loginAttemptService.isBlocked("disabled")).thenReturn(false);
        UserAccount account = UserAccount.restore(
                1L, "disabled", "hash", UserRole.READER, UserStatus.DISABLED,
                Instant.now(), Instant.now());
        when(userAccountRepository.findByUsername("disabled")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login(cmd))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).resultCode())
                .isEqualTo(ResultCode.AUTH_ACCOUNT_LOCKED);
    }

    @Test
    void refreshShouldRejectExpiredToken() {
        RefreshCommand cmd = new RefreshCommand("expired-token", "127.0.0.1", "agent");
        RefreshToken token = RefreshToken.issue(
                1L, "hash", Instant.now().minusSeconds(200), Instant.now().minusSeconds(100),
                "agent", "127.0.0.1");
        when(refreshTokenRepository.findByHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.refresh(cmd))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).resultCode())
                .isEqualTo(ResultCode.AUTH_REFRESH_TOKEN_INVALID);
    }

    @Test
    void refreshShouldIssueNewTokensOnValidRefreshToken() {
        RefreshCommand cmd = new RefreshCommand("valid-token", "127.0.0.1", "agent");
        RefreshToken token = RefreshToken.issue(
                1L, "hash", Instant.now(), Instant.now().plusSeconds(86400),
                "agent", "127.0.0.1");
        when(refreshTokenRepository.findByHash(anyString())).thenReturn(Optional.of(token));

        UserAccount account = UserAccount.register("bob", "hash", UserRole.READER, Instant.now());
        account.assignId(1L);
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(jwtService.issueAccess(eq(account), any(Instant.class))).thenReturn("new-access");
        when(jwtService.issueRefreshTokenString()).thenReturn("new-refresh");

        AuthResult result = service.refresh(cmd);

        assertThat(result.accessToken()).isEqualTo("new-access");
        assertThat(result.refreshToken()).isEqualTo("new-refresh");
        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void logoutShouldBlacklistAccessTokenAndRevokeRefreshToken() {
        RefreshToken token = RefreshToken.issue(
                1L, "hash", Instant.now(), Instant.now().plusSeconds(86400),
                "agent", "127.0.0.1");
        when(refreshTokenRepository.findByHash(anyString())).thenReturn(Optional.of(token));

        service.logout("jti-123", Duration.ofMinutes(10), "refresh-raw");

        verify(tokenBlacklist).add("jti-123", Duration.ofMinutes(10));
        verify(refreshTokenRepository).save(token);
        assertThat(token.isRevoked()).isTrue();
    }

    @Test
    void logoutShouldHandleNullGracefully() {
        service.logout(null, null, null);

        verifyNoInteractions(tokenBlacklist);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void registerShouldRejectDuplicateUsername() {
        RegisterCommand cmd = new RegisterCommand("dup", "password", "Dup", "123", "d@test.com", "127.0.0.1", "agent");
        when(userAccountRepository.existsByUsername("dup")).thenReturn(true);

        assertThatThrownBy(() -> service.register(cmd))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).resultCode())
                .isEqualTo(ResultCode.AUTH_USERNAME_DUPLICATED);

        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void registerShouldAlsoCreateReaderProfile() {
        RegisterCommand cmd = new RegisterCommand("newuser", "pass", "New", "123", "n@test.com", "127.0.0.1", "agent");
        when(userAccountRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hash");

        UserAccount account = UserAccount.register("newuser", "hash", UserRole.READER, Instant.now());
        account.assignId(42L);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(account);
        when(jwtService.issueAccess(eq(account), any(Instant.class))).thenReturn("token");
        when(jwtService.issueRefreshTokenString()).thenReturn("refresh");

        service.register(cmd);

        verify(readerRepository).save(any(Reader.class));
    }
}
