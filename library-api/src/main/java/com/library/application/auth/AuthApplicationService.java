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
import com.library.infrastructure.alert.AlertSender;
import com.library.infrastructure.security.JwtProperties;
import com.library.infrastructure.security.JwtService;
import com.library.infrastructure.security.LoginAttemptService;
import com.library.infrastructure.security.TokenBlacklist;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthApplicationService {

    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklist tokenBlacklist;
    private final AlertSender alertSender;
    private final ReaderRepository readerRepository;

    public AuthApplicationService(UserAccountRepository userAccountRepository,
                                  RefreshTokenRepository refreshTokenRepository,
                                  PasswordEncoder passwordEncoder,
                                  JwtService jwtService,
                                  JwtProperties jwtProperties,
                                  LoginAttemptService loginAttemptService,
                                  TokenBlacklist tokenBlacklist,
                                  AlertSender alertSender,
                                  ReaderRepository readerRepository) {
        this.userAccountRepository = userAccountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.loginAttemptService = loginAttemptService;
        this.tokenBlacklist = tokenBlacklist;
        this.alertSender = alertSender;
        this.readerRepository = readerRepository;
    }

    @Transactional
    public AuthResult register(RegisterCommand command) {
        if (userAccountRepository.existsByUsername(command.username())) {
            throw new BusinessException(ResultCode.AUTH_USERNAME_DUPLICATED);
        }

        String hash = passwordEncoder.encode(command.password());
        UserAccount account = UserAccount.register(command.username(), hash, UserRole.READER, Instant.now());
        UserAccount saved = userAccountRepository.save(account);

        String readerNo = "R" + String.format("%08d", saved.id());
        Reader reader = Reader.create(
                saved.id(), readerNo, command.name(), command.phone(), command.email(), LocalDate.now());
        readerRepository.save(reader);

        return generateAuthResult(saved, command.ip(), command.userAgent());
    }

    @Transactional
    public AuthResult login(LoginCommand command) {
        if (loginAttemptService.isBlocked(command.username())) {
            throw new BusinessException(ResultCode.AUTH_ACCOUNT_LOCKED);
        }

        Optional<UserAccount> accountOpt = userAccountRepository.findByUsername(command.username());
        if (accountOpt.isEmpty()) {
            loginAttemptService.loginFailed(command.username());
            throw new BusinessException(ResultCode.AUTH_USERNAME_OR_PASSWORD_INCORRECT);
        }

        UserAccount account = accountOpt.get();
        if (!passwordEncoder.matches(command.password(), account.passwordHash())) {
            loginAttemptService.loginFailed(command.username());
            throw new BusinessException(ResultCode.AUTH_USERNAME_OR_PASSWORD_INCORRECT);
        }

        account.ensureActive();
        loginAttemptService.loginSucceeded(command.username());

        return generateAuthResult(account, command.ip(), command.userAgent());
    }

    @Transactional
    public AuthResult refresh(RefreshCommand command) {
        String tokenHash = hashToken(command.refreshToken());
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByHash(tokenHash);

        if (tokenOpt.isEmpty()) {
            throw new BusinessException(ResultCode.AUTH_REFRESH_TOKEN_INVALID);
        }

        RefreshToken token = tokenOpt.get();
        Instant now = Instant.now();

        if (token.isRevoked()) {
            // Replay detected! Revoke all tokens for this user
            refreshTokenRepository.revokeAllByUser(token.userAccountId(), now, "Replay detected");
            alertSender.sendError("Security Alert: Token Replay", 
                    "Revoked refresh token was used for user " + token.userAccountId(), null);
            throw new BusinessException(ResultCode.AUTH_REFRESH_TOKEN_INVALID);
        }

        if (token.isExpired(now)) {
            throw new BusinessException(ResultCode.AUTH_REFRESH_TOKEN_INVALID);
        }

        UserAccount account = userAccountRepository.findById(token.userAccountId())
                .orElseThrow(() -> new BusinessException(ResultCode.AUTH_REFRESH_TOKEN_INVALID));

        account.ensureActive();

        // Revoke the old token
        token.revoke(now, "Rotated");
        refreshTokenRepository.save(token);

        return generateAuthResult(account, command.ip(), command.userAgent());
    }

    public void logout(String accessJti, Duration remaining, String refreshTokenString) {
        if (accessJti != null && remaining != null && !remaining.isNegative()) {
            tokenBlacklist.add(accessJti, remaining);
        }

        if (refreshTokenString != null) {
            String tokenHash = hashToken(refreshTokenString);
            refreshTokenRepository.findByHash(tokenHash).ifPresent(token -> {
                token.revoke(Instant.now(), "Logout");
                refreshTokenRepository.save(token);
            });
        }
    }

    @Transactional
    public void logoutTokens(String accessToken, String refreshTokenString) {
        String jti = null;
        Duration remaining = null;
        if (accessToken != null && !accessToken.isBlank()) {
            JwtService.ParsedJwt parsedJwt = jwtService.parse(accessToken);
            jti = parsedJwt.jti();
            remaining = Duration.between(Instant.now(), parsedJwt.expiresAt());
        }
        logout(jti, remaining, refreshTokenString);
    }

    @Transactional
    public void logoutAll(Long userAccountId) {
        refreshTokenRepository.revokeAllByUser(userAccountId, Instant.now(), "Logout All");
    }

    private AuthResult generateAuthResult(UserAccount account, String ip, String userAgent) {
        Instant now = Instant.now();
        String accessToken = jwtService.issueAccess(account, now);
        String refreshTokenStr = jwtService.issueRefreshTokenString();
        String refreshTokenHash = hashToken(refreshTokenStr);

        Instant refreshExpiresAt = now.plus(jwtProperties.getRefreshTtl());
        
        RefreshToken refreshToken = RefreshToken.issue(
                account.id(),
                refreshTokenHash,
                now,
                refreshExpiresAt,
                userAgent,
                ip
        );
        
        refreshTokenRepository.save(refreshToken);

        return new AuthResult(
                accessToken,
                refreshTokenStr,
                jwtProperties.getAccessTtl().getSeconds(),
                jwtProperties.getRefreshTtl().getSeconds(),
                account
        );
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
