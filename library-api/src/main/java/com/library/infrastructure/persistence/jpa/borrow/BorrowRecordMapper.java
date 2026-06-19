package com.library.infrastructure.persistence.jpa.borrow;

import com.library.domain.borrow.BorrowRecord;

final class BorrowRecordMapper {

    private BorrowRecordMapper() {
    }

    static BorrowRecord toDomain(BorrowRecordJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return BorrowRecord.restore(
                entity.getId(),
                entity.getReaderId(),
                entity.getBookCopyId(),
                entity.getBorrowTime(),
                entity.getDueDate(),
                entity.getReturnTime(),
                entity.getRenewCount(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    static BorrowRecordJpaEntity toEntity(BorrowRecord domain) {
        if (domain == null) {
            return null;
        }
        BorrowRecordJpaEntity entity = new BorrowRecordJpaEntity();
        entity.setId(domain.id());
        entity.setReaderId(domain.readerId());
        entity.setBookCopyId(domain.bookCopyId());
        entity.setBorrowTime(domain.borrowTime());
        entity.setDueDate(domain.dueDate());
        entity.setReturnTime(domain.returnTime());
        entity.setRenewCount(domain.renewCount());
        entity.setStatus(domain.status());
        entity.setCreatedAt(domain.createdAt());
        entity.setUpdatedAt(domain.updatedAt());
        return entity;
    }
}
