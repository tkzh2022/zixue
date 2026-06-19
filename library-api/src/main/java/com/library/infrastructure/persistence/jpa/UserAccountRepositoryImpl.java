package com.library.infrastructure.persistence.jpa;

import com.library.domain.user.UserAccount;
import com.library.domain.user.UserAccountRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserAccountRepositoryImpl implements UserAccountRepository {

    private final UserAccountJpaRepository jpaRepository;

    public UserAccountRepositoryImpl(UserAccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<UserAccount> findById(Long id) {
        return jpaRepository.findById(id).map(UserAccountMapper::toDomain);
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(UserAccountMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public UserAccount save(UserAccount account) {
        UserAccountJpaEntity entity = UserAccountMapper.toEntity(account);
        UserAccountJpaEntity saved = jpaRepository.save(entity);
        account.assignId(saved.getId());
        return UserAccountMapper.toDomain(saved);
    }
}
