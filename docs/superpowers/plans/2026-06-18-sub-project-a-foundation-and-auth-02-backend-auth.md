## 任务 A3：设计领域层 - UserAccount 与 RefreshToken 聚合

**Files:**
- Create: `library-api/src/main/java/com/library/domain/user/UserRole.java`
- Create: `library-api/src/main/java/com/library/domain/user/UserStatus.java`
- Create: `library-api/src/main/java/com/library/domain/user/UserAccount.java`
- Create: `library-api/src/main/java/com/library/domain/user/UserAccountRepository.java`
- Create: `library-api/src/main/java/com/library/domain/user/RefreshToken.java`
- Create: `library-api/src/main/java/com/library/domain/user/RefreshTokenRepository.java`
- Create: `library-api/src/main/java/com/library/domain/shared/Result.java`
- Create: `library-api/src/main/java/com/library/domain/shared/exception/ResultCode.java`
- Create: `library-api/src/main/java/com/library/domain/shared/exception/BusinessException.java`

代码片段（每个文件给出关键内容）：

`UserRole.java`：枚举 READER, LIBRARIAN

`UserStatus.java`：枚举 ACTIVE, DISABLED, LOCKED

`UserAccount.java`：聚合根，含 id、username、passwordHash、role、status、createdAt、updatedAt；提供 markLocked()、markActive()、changePassword(newHash)、isActive() 等领域方法；纯 POJO 不依赖 Spring。

`RefreshToken.java`：id、userAccountId、tokenHash、issuedAt、expiresAt、revokedAt、revokedReason、userAgent、ip；提供 isExpired(now)、isRevoked()、revoke(reason)。

仓储接口：领域包内只放接口，签名：
```java
public interface UserAccountRepository {
    Optional<UserAccount> findByUsername(String username);
    Optional<UserAccount> findById(Long id);
    UserAccount save(UserAccount account);
    boolean existsByUsername(String username);
}

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByHash(String hash);
    void revokeAllByUser(Long userAccountId, String reason);
    int deleteExpiredBefore(Instant cutoff);
}
```

`Result<T>` 与 `ResultCode`、`BusinessException`：见 spec 7.5 错误码表，全部枚举一次性写入。

提交命令：`git commit -m "feat(domain): user account and refresh token aggregates"`

## 任务 A4：JPA 持久化实现

**Files:**
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/UserAccountJpaEntity.java`
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/UserAccountJpaRepository.java` (Spring Data 接口)
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/UserAccountRepositoryImpl.java`
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/RefreshTokenJpaEntity.java`
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/RefreshTokenJpaRepository.java`
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/RefreshTokenRepositoryImpl.java`
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/UserAccountMapper.java` (MapStruct)
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/RefreshTokenMapper.java`

JPA 实体使用 @Table、@Column 显式映射；Impl 类带 @Repository，注入 JpaRepository 与 MapStruct mapper，做 entity ↔ domain 转换。

提交命令：`git commit -m "feat(infra): JPA repositories for user account and refresh token"`

## 任务 A5：Flyway 迁移脚本 V1 + V2

**Files:**
- Create: `library-api/src/main/resources/db/migration/V1__init_user_account.sql`
- Create: `library-api/src/main/resources/db/migration/V2__init_refresh_token.sql`

按 spec 3.2 的字段类型与索引建表。

提交命令：`git commit -m "feat(db): flyway migrations V1 V2 for auth tables"`

## 任务 A6：JpaIT 集成测试基类（Testcontainers MySQL）

**Files:**
- Create: `library-api/src/test/java/com/library/support/JpaIntegrationTestBase.java`
- Create: `library-api/src/test/java/com/library/infrastructure/persistence/UserAccountRepositoryImplIT.java`

JpaIntegrationTestBase 用 @Testcontainers + @ServiceConnection，启动 MySQL 8 容器；测试 save/findByUsername/existsByUsername 闭环。

提交命令：`git commit -m "test(infra): integration tests for user account repository"`

## 任务 A7：JwtService 与单元测试

**Files:**
- Create: `library-api/src/main/java/com/library/infrastructure/security/JwtProperties.java` (@ConfigurationProperties)
- Create: `library-api/src/main/java/com/library/infrastructure/security/JwtService.java`
- Create: `library-api/src/test/java/com/library/infrastructure/security/JwtServiceTest.java`

JwtService 接口：`String issueAccess(UserAccount); ParsedJwt parse(String); String issueRefreshTokenString();`
内部用 jjwt 0.12 的 Jwts.builder/parser API。startup 校验 secret 长度 >= 32 字节，缺失或为默认值 → 抛 IllegalStateException。

测试覆盖：签发可解析、过期拒绝、签名错误拒绝、缺失 secret fail-fast。

提交命令：`git commit -m "feat(auth): jwt service with fail-fast secret validation"`

## 任务 A8：BCrypt + LoginAttemptService

**Files:**
- Create: `library-api/src/main/java/com/library/infrastructure/security/PasswordEncoderConfig.java`
- Create: `library-api/src/main/java/com/library/infrastructure/security/LoginAttemptService.java`
- Create: `library-api/src/test/java/com/library/infrastructure/security/LoginAttemptServiceTest.java`

LoginAttemptService 内部用 Caffeine（key=username, expireAfterWrite=15min, 计数 +1，>=5 锁定到 expire）。

提交命令：`git commit -m "feat(auth): bcrypt encoder + login attempt rate limiter"`

## 任务 A9：TokenBlacklist（Caffeine 实现 + 抽象接口）

**Files:**
- Create: `library-api/src/main/java/com/library/infrastructure/security/TokenBlacklist.java`
- Create: `library-api/src/main/java/com/library/infrastructure/security/CaffeineTokenBlacklist.java`
- Create: `library-api/src/test/java/com/library/infrastructure/security/CaffeineTokenBlacklistTest.java`

接口：`void add(String jti, Duration ttl); boolean contains(String jti);`
实现：`@ConditionalOnProperty(name="library.token-blacklist.type", havingValue="caffeine", matchIfMissing=true)`。

提交命令：`git commit -m "feat(auth): token blacklist with caffeine impl"`

## 任务 A10：AuthApplicationService（应用层用例）

**Files:**
- Create: `library-api/src/main/java/com/library/application/auth/command/LoginCommand.java`
- Create: `library-api/src/main/java/com/library/application/auth/command/RegisterCommand.java`
- Create: `library-api/src/main/java/com/library/application/auth/command/RefreshCommand.java`
- Create: `library-api/src/main/java/com/library/application/auth/result/AuthResult.java`
- Create: `library-api/src/main/java/com/library/application/auth/AuthApplicationService.java`
- Create: `library-api/src/test/java/com/library/application/auth/AuthApplicationServiceTest.java`

AuthApplicationService 方法：
- `AuthResult login(LoginCommand)`：尝试计数 → 查 user → 校验密码 → 通过则发 access + refresh，写 refresh_token 表（hash 存）；失败 attempts++
- `AuthResult register(RegisterCommand)`：username 不重复 → BCrypt → 创建 UserAccount(role=READER, status=ACTIVE)
- `AuthResult refresh(RefreshCommand)`：hash 查找 → 校验 → revoke 旧 → 发新 → 检测重放
- `void logout(String accessJti, Duration remaining, String refreshTokenString)`：黑名单 + revoke
- `void logoutAll(Long userAccountId)`

测试用 Mockito mock UserAccountRepository / RefreshTokenRepository / JwtService / PasswordEncoder / LoginAttemptService。

提交命令：`git commit -m "feat(auth): application service with login/register/refresh/logout"`

## 任务 A11：AuthController + DTO

**Files:**
- Create: `library-api/src/main/java/com/library/interfaces/dto/auth/LoginRequest.java RegisterRequest.java RefreshRequest.java`
- Create: `library-api/src/main/java/com/library/interfaces/dto/auth/LoginResponse.java UserInfoResponse.java`
- Create: `library-api/src/main/java/com/library/interfaces/rest/AuthController.java`

接口：`POST /api/v1/auth/login`、`/register`、`/refresh`、`/logout`、`/logout-all`、`GET /me`。
DTO 用 jakarta validation 注解（@NotBlank, @Size, @Email）。

提交命令：`git commit -m "feat(auth): rest controller for authentication"`
