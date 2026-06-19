package com.library.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {

    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshTokenJpaEntity t set t.revokedAt = :now, t.revokedReason = :reason " +
           "where t.userAccountId = :userId and t.revokedAt is null")
    int revokeAllByUser(@Param("userId") Long userAccountId,
                        @Param("now") Instant now,
                        @Param("reason") String reason);

    @Modifying
    @Query("delete from RefreshTokenJpaEntity t where t.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}
