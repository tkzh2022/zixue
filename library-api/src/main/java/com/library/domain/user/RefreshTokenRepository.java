package com.library.domain.user;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByHash(String tokenHash);

    int revokeAllByUser(Long userAccountId, Instant now, String reason);

    int deleteExpiredBefore(Instant cutoff);
}
