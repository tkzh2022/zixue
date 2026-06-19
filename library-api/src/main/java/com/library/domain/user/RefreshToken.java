package com.library.domain.user;

import java.time.Instant;
import java.util.Objects;

public class RefreshToken {

    private Long id;
    private Long userAccountId;
    private String tokenHash;
    private Instant issuedAt;
    private Instant expiresAt;
    private Instant revokedAt;
    private String revokedReason;
    private String userAgent;
    private String ip;

    private RefreshToken() {
    }

    public static RefreshToken issue(Long userAccountId,
                                     String tokenHash,
                                     Instant issuedAt,
                                     Instant expiresAt,
                                     String userAgent,
                                     String ip) {
        Objects.requireNonNull(userAccountId, "userAccountId");
        Objects.requireNonNull(tokenHash, "tokenHash");
        Objects.requireNonNull(issuedAt, "issuedAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        RefreshToken token = new RefreshToken();
        token.userAccountId = userAccountId;
        token.tokenHash = tokenHash;
        token.issuedAt = issuedAt;
        token.expiresAt = expiresAt;
        token.userAgent = userAgent;
        token.ip = ip;
        return token;
    }

    public static RefreshToken restore(Long id,
                                       Long userAccountId,
                                       String tokenHash,
                                       Instant issuedAt,
                                       Instant expiresAt,
                                       Instant revokedAt,
                                       String revokedReason,
                                       String userAgent,
                                       String ip) {
        RefreshToken token = new RefreshToken();
        token.id = id;
        token.userAccountId = userAccountId;
        token.tokenHash = tokenHash;
        token.issuedAt = issuedAt;
        token.expiresAt = expiresAt;
        token.revokedAt = revokedAt;
        token.revokedReason = revokedReason;
        token.userAgent = userAgent;
        token.ip = ip;
        return token;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isUsable(Instant now) {
        return !isRevoked() && !isExpired(now);
    }

    public void revoke(Instant now, String reason) {
        this.revokedAt = now;
        this.revokedReason = reason;
    }

    public Long id() {
        return id;
    }

    public Long userAccountId() {
        return userAccountId;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public Instant issuedAt() {
        return issuedAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant revokedAt() {
        return revokedAt;
    }

    public String revokedReason() {
        return revokedReason;
    }

    public String userAgent() {
        return userAgent;
    }

    public String ip() {
        return ip;
    }

    public void assignId(Long id) {
        this.id = id;
    }
}
