package com.library.infrastructure.persistence.jpa;

import com.library.domain.user.UserAccount;

final class UserAccountMapper {

    private UserAccountMapper() {
    }

    static UserAccount toDomain(UserAccountJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserAccount.restore(
                entity.getId(),
                entity.getUsername(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    static UserAccountJpaEntity toEntity(UserAccount domain) {
        if (domain == null) {
            return null;
        }
        UserAccountJpaEntity entity = new UserAccountJpaEntity();
        entity.setId(domain.id());
        entity.setUsername(domain.username());
        entity.setPasswordHash(domain.passwordHash());
        entity.setRole(domain.role());
        entity.setStatus(domain.status());
        entity.setCreatedAt(domain.createdAt());
        entity.setUpdatedAt(domain.updatedAt());
        return entity;
    }
}
