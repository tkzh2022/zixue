package com.library.infrastructure.persistence.jpa.fine;

import com.library.domain.fine.FineRecord;

final class FineRecordMapper {

    private FineRecordMapper() {
    }

    static FineRecord toDomain(FineRecordJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return FineRecord.restore(
                entity.getId(),
                entity.getBorrowRecordId(),
                entity.getReaderId(),
                entity.getAmount(),
                entity.getReason(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getPaidAt()
        );
    }

    static FineRecordJpaEntity toEntity(FineRecord domain) {
        if (domain == null) {
            return null;
        }
        FineRecordJpaEntity entity = new FineRecordJpaEntity();
        entity.setId(domain.id());
        entity.setBorrowRecordId(domain.borrowRecordId());
        entity.setReaderId(domain.readerId());
        entity.setAmount(domain.amount());
        entity.setReason(domain.reason());
        entity.setStatus(domain.status());
        entity.setCreatedAt(domain.createdAt());
        entity.setPaidAt(domain.paidAt());
        return entity;
    }
}
