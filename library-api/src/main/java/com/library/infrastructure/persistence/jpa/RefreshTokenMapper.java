package com.library.infrastructure.persistence.jpa;

import com.library.domain.user.RefreshToken;

final class RefreshTokenMapper {

    private RefreshTokenMapper() {
    }

    static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return RefreshToken.restore(
                entity.getId(),
                entity.getUserAccountId(),
                entity.getTokenHash(),
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getRevokedReason(),
                entity.getUserAgent(),
                entity.getIp());
    }

    static RefreshTokenJpaEntity toEntity(RefreshToken domain) {
        if (domain == null) {
            return null;
        }
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setId(domain.id());
        entity.setUserAccountId(domain.userAccountId());
        entity.setTokenHash(domain.tokenHash());
        entity.setIssuedAt(domain.issuedAt());
        entity.setExpiresAt(domain.expiresAt());
        entity.setRevokedAt(domain.revokedAt());
        entity.setRevokedReason(domain.revokedReason());
        entity.setUserAgent(domain.userAgent());
        entity.setIp(domain.ip());
        return entity;
    }
}
