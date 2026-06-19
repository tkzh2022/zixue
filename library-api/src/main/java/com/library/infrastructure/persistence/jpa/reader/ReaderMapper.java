package com.library.infrastructure.persistence.jpa.reader;

import com.library.domain.reader.Reader;

final class ReaderMapper {

    private ReaderMapper() {
    }

    static Reader toDomain(ReaderJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Reader.restore(
                entity.getId(),
                entity.getUserAccountId(),
                entity.getReaderNo(),
                entity.getName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getStatus(),
                entity.getRegisterDate(),
                entity.getDeletedAt()
        );
    }

    static ReaderJpaEntity toEntity(Reader domain) {
        if (domain == null) {
            return null;
        }
        ReaderJpaEntity entity = new ReaderJpaEntity();
        entity.setId(domain.id());
        entity.setUserAccountId(domain.userAccountId());
        entity.setReaderNo(domain.readerNo());
        entity.setName(domain.name());
        entity.setPhone(domain.phone());
        entity.setEmail(domain.email());
        entity.setStatus(domain.status());
        entity.setRegisterDate(domain.registerDate());
        entity.setDeletedAt(domain.deletedAt());
        return entity;
    }
}
