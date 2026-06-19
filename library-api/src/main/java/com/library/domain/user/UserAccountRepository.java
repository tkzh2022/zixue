package com.library.domain.user;

import java.util.Optional;

public interface UserAccountRepository {

    Optional<UserAccount> findById(Long id);

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    UserAccount save(UserAccount account);
}
