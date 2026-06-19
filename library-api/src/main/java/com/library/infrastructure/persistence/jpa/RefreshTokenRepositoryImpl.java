package com.library.infrastructure.persistence.jpa;

import com.library.domain.user.RefreshToken;
import com.library.domain.user.RefreshTokenRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    public RefreshTokenRepositoryImpl(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        RefreshTokenJpaEntity saved = jpaRepository.save(RefreshTokenMapper.toEntity(token));
        token.assignId(saved.getId());
        return RefreshTokenMapper.toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(RefreshTokenMapper::toDomain);
    }

    @Override
    @Transactional
    public int revokeAllByUser(Long userAccountId, Instant now, String reason) {
        return jpaRepository.revokeAllByUser(userAccountId, now, reason);
    }

    @Override
    @Transactional
    public int deleteExpiredBefore(Instant cutoff) {
        return jpaRepository.deleteExpiredBefore(cutoff);
    }
}
