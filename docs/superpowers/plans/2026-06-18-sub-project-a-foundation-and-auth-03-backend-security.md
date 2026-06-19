## 任务 A12：JwtAuthFilter 与 SecurityConfig

**Files:**
- Create: `library-api/src/main/java/com/library/interfaces/filter/JwtAuthFilter.java`
- Create: `library-api/src/main/java/com/library/infrastructure/security/SecurityConfig.java`

JwtAuthFilter（OncePerRequestFilter）：
- skip 路径：`/api/v1/auth/login`、`/api/v1/auth/register`、`/api/v1/auth/refresh`、`/api/v1/catalog/**`、`/error`、Actuator
- 解析 Bearer token → jwtService.parse → 校验黑名单 → 装载 UserDetails → 设置 SecurityContext
- 异常映射：失败时不直接 500，写入 request attribute 由 GlobalExceptionHandler 通过 AuthenticationEntryPoint 统一回 8401/8402

SecurityConfig：禁用 CSRF、stateless session、CORS（基于配置 `library.cors.allowed-origins`）、注册 filter、permitAll 公开路径，其它 authenticated。

提交：`git commit -m "feat(auth): jwt filter and stateless security config"`

## 任务 A13：方法级权限 @PreAuthorize 起步

**Files:**
- Modify: `library-api/src/main/java/com/library/infrastructure/security/SecurityConfig.java` (加 @EnableMethodSecurity)
- Create: `library-api/src/main/java/com/library/interfaces/security/RoleGuard.java`（自定义注解组合，可选简化）

提交：`git commit -m "feat(auth): enable method-level security"`

## 任务 A14：AuthControllerIT 集成测试

**Files:**
- Create: `library-api/src/test/java/com/library/interfaces/rest/AuthControllerIT.java`

场景：
- 注册成功 / username 重复 → 8002
- 登录成功 → 返回 access+refresh
- 5 次错误密码 → 8405
- 用 access 调 /me 成功；过期 access → 8402
- refresh 成功并轮换；二次用旧 refresh → 8403 + 全部 revoke
- logout 后 access 失效 → 8401

用 @SpringBootTest + @AutoConfigureMockMvc + Testcontainers。

提交：`git commit -m "test(auth): integration tests covering full auth flow"`

## 任务 A15：GlobalExceptionHandler + TraceIdFilter

**Files:**
- Create: `library-api/src/main/java/com/library/interfaces/advice/GlobalExceptionHandler.java`
- Create: `library-api/src/main/java/com/library/interfaces/filter/TraceIdFilter.java`

GlobalExceptionHandler 处理：
- BusinessException → 200 + Result.fail(code, msg, traceId)
- MethodArgumentNotValidException / ConstraintViolationException → 8001
- AuthenticationException / AccessDeniedException → 8401/8402/8403/8404
- DataIntegrityViolationException → 8002
- Exception → 9000 + 写 ERROR + AlertSender.sendError()

TraceIdFilter（最高优先级 OncePerRequestFilter）：
- 读 X-Trace-Id；缺失则 UUID 生成
- MDC.put("traceId", id)；finally MDC.clear()
- response.setHeader("X-Trace-Id", id)

提交：`git commit -m "feat(observability): global exception handler + trace id"`

## 任务 A16：AlertSender 抽象 + LoggingAlertSender

**Files:**
- Create: `library-api/src/main/java/com/library/infrastructure/alert/AlertSender.java`
- Create: `library-api/src/main/java/com/library/infrastructure/alert/LoggingAlertSender.java`

接口：`void sendError(String title, String summary, Throwable error);`
实现：写 ERROR 日志，包含 traceId、堆栈摘要前 10 行。

提交：`git commit -m "feat(observability): alert sender abstraction"`

## 任务 A17：限流 RateLimiter + RateLimitInterceptor

**Files:**
- Create: `library-api/src/main/java/com/library/interfaces/annotation/RateLimit.java`
- Create: `library-api/src/main/java/com/library/infrastructure/ratelimit/RateLimiter.java`
- Create: `library-api/src/main/java/com/library/infrastructure/ratelimit/LocalBucketRateLimiter.java`
- Create: `library-api/src/main/java/com/library/interfaces/filter/RateLimitInterceptor.java`
- Modify: `WebConfig.java` 注册拦截器
- 在 AuthController 的 login/register/refresh 加 @RateLimit
- Test: `library-api/src/test/java/com/library/infrastructure/ratelimit/LocalBucketRateLimiterTest.java`

@RateLimit(key="ip|user|ip+user", limit, period)。
Interceptor 解析方法注解 → 拼 key（ip 来源 X-Forwarded-For 或 RemoteAddr）→ 调 RateLimiter.tryConsume → 超限抛 BusinessException(8003)。
响应 header X-RateLimit-Remaining、Retry-After。

测试：本地 bucket 用模拟时钟，断言 N 次成功后第 N+1 次失败。

提交：`git commit -m "feat(security): bucket4j rate limiting on auth endpoints"`

## 任务 A18：ArchUnit 严格规则升级

**Files:**
- Modify: `library-api/src/test/java/com/library/ArchitectureTest.java`

补充规则：
- domain 包不能依赖任何 spring 类、jakarta.persistence、javax.persistence
- interfaces 不能直接依赖 infrastructure.persistence
- 不允许 `catch (Exception e) {}` 空块（结合 Spotbugs 或 ArchUnit 自定义条件）

提交：`git commit -m "test(arch): tighten layered rules and ban empty catch"`

## 任务 A19：DataInitializer（开发用 seed）

**Files:**
- Create: `library-api/src/main/java/com/library/config/DataInitializer.java`

启动时（@Profile("dev")）：若 user_account 表为空，插入 librarian/librarian123 与 reader1/reader123（密码 BCrypt）。

提交：`git commit -m "feat(dev): seed initial users for dev profile"`
