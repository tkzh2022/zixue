package com.library.domain.user;

import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;

import java.time.Instant;
import java.util.Objects;

public class UserAccount {

    private Long id;
    private String username;
    private String passwordHash;
    private UserRole role;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private UserAccount() {
    }

    public static UserAccount register(String username, String passwordHash, UserRole role, Instant now) {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(passwordHash, "passwordHash");
        Objects.requireNonNull(role, "role");
        UserAccount account = new UserAccount();
        account.username = username;
        account.passwordHash = passwordHash;
        account.role = role;
        account.status = UserStatus.ACTIVE;
        account.createdAt = now;
        account.updatedAt = now;
        return account;
    }

    public static UserAccount restore(Long id,
                                      String username,
                                      String passwordHash,
                                      UserRole role,
                                      UserStatus status,
                                      Instant createdAt,
                                      Instant updatedAt) {
        UserAccount account = new UserAccount();
        account.id = id;
        account.username = username;
        account.passwordHash = passwordHash;
        account.role = role;
        account.status = status;
        account.createdAt = createdAt;
        account.updatedAt = updatedAt;
        return account;
    }

    public void ensureActive() {
        if (status != UserStatus.ACTIVE) {
            throw new BusinessException(ResultCode.AUTH_ACCOUNT_LOCKED);
        }
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public void changePassword(String newPasswordHash, Instant now) {
        Objects.requireNonNull(newPasswordHash, "newPasswordHash");
        this.passwordHash = newPasswordHash;
        this.updatedAt = now;
    }

    public void disable(Instant now) {
        this.status = UserStatus.DISABLED;
        this.updatedAt = now;
    }

    public void enable(Instant now) {
        this.status = UserStatus.ACTIVE;
        this.updatedAt = now;
    }

    public Long id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public UserRole role() {
        return role;
    }

    public UserStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public void assignId(Long id) {
        this.id = id;
    }
}
