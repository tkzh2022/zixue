package com.library.infrastructure.persistence.jpa.borrow;

import com.library.domain.borrow.BorrowRule;

final class BorrowRuleMapper {

    private BorrowRuleMapper() {
    }

    static BorrowRule toDomain(BorrowRuleJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return BorrowRule.restore(
                entity.getId(),
                entity.getReaderType(),
                entity.getMaxBorrowDays(),
                entity.getMaxBorrowCount(),
                entity.getMaxRenewCount(),
                entity.getFinePerDay(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    static BorrowRuleJpaEntity toEntity(BorrowRule domain) {
        if (domain == null) {
            return null;
        }
        BorrowRuleJpaEntity entity = new BorrowRuleJpaEntity();
        entity.setId(domain.id());
        entity.setReaderType(domain.readerType());
        entity.setMaxBorrowDays(domain.maxBorrowDays());
        entity.setMaxBorrowCount(domain.maxBorrowCount());
        entity.setMaxRenewCount(domain.maxRenewCount());
        entity.setFinePerDay(domain.finePerDay());
        entity.setCreatedAt(domain.createdAt());
        entity.setUpdatedAt(domain.updatedAt());
        return entity;
    }
}
