package com.library.infrastructure.persistence;

import com.library.domain.user.UserAccount;
import com.library.domain.user.UserAccountRepository;
import com.library.domain.user.UserRole;
import com.library.support.JpaIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserAccountRepositoryImplIT extends JpaIntegrationTestBase {

    @Autowired
    UserAccountRepository repository;

    @Test
    void saveThenFindByUsernameRoundTrip() {
        Instant now = Instant.now();
        UserAccount account = UserAccount.register("alice", "$2a$10$hashhashhashhashhashhash", UserRole.READER, now);

        UserAccount saved = repository.save(account);

        assertThat(saved.id()).isNotNull();
        assertThat(repository.existsByUsername("alice")).isTrue();

        Optional<UserAccount> found = repository.findByUsername("alice");
        assertThat(found).isPresent();
        assertThat(found.get().username()).isEqualTo("alice");
        assertThat(found.get().role()).isEqualTo(UserRole.READER);
    }

    @Test
    void findByUsernameReturnsEmptyWhenAbsent() {
        assertThat(repository.findByUsername("ghost")).isEmpty();
        assertThat(repository.existsByUsername("ghost")).isFalse();
    }
}
