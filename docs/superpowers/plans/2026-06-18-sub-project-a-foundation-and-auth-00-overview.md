# Sub-project A 实现计划：底座 + 认证

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 搭建图书馆管理系统的可运行骨架，覆盖 JWT 双 token 认证、限流、统一异常、ArchUnit 分层守护、TraceId、前端登录骨架与路由守卫。

**Architecture:** 单 Maven 工程，DDD 4 层包结构（interfaces / application / domain / infrastructure）。Spring Boot 3.2 + Spring Security 6 + JWT + Caffeine 黑名单 + Bucket4j 限流 + Flyway。前端 Vue 3 + Vite + Element Plus + Pinia + Vue Router + vue-i18n。

**Tech Stack:** Java 17、Spring Boot 3.2.5、Spring Security 6、jjwt 0.12、Bucket4j、Caffeine、Flyway、MapStruct、Lombok、Testcontainers、ArchUnit、Vue 3.4、TypeScript、Vite 5、Pinia、Element Plus 2、vue-i18n 9。

参考 spec：`docs/superpowers/specs/2026-06-18-library-system-design.md` 第 1、2、3、5 章及前端章节。

---

## 文件结构（本 sub-project 涉及）

### 后端 library-api/
```
library-api/
├── pom.xml
├── src/main/java/com/library/
│   ├── LibraryApplication.java
│   ├── interfaces/
│   │   ├── rest/AuthController.java
│   │   ├── advice/GlobalExceptionHandler.java
│   │   ├── filter/JwtAuthFilter.java
│   │   ├── filter/TraceIdFilter.java
│   │   ├── filter/RateLimitInterceptor.java
│   │   ├── annotation/RateLimit.java
│   │   ├── dto/LoginRequest.java RegisterRequest.java RefreshRequest.java
│   │   └── dto/LoginResponse.java UserInfoResponse.java
│   ├── application/
│   │   ├── auth/AuthApplicationService.java
│   │   ├── auth/command/LoginCommand.java RegisterCommand.java RefreshCommand.java
│   │   └── auth/result/AuthResult.java
│   ├── domain/
│   │   ├── user/UserAccount.java
│   │   ├── user/UserAccountRepository.java
│   │   ├── user/UserRole.java UserStatus.java
│   │   ├── user/RefreshToken.java
│   │   ├── user/RefreshTokenRepository.java
│   │   ├── shared/exception/BusinessException.java
│   │   ├── shared/exception/ResultCode.java
│   │   └── shared/Result.java
│   ├── infrastructure/
│   │   ├── persistence/jpa/UserAccountJpaEntity.java UserAccountJpaRepository.java UserAccountRepositoryImpl.java
│   │   ├── persistence/jpa/RefreshTokenJpaEntity.java RefreshTokenJpaRepository.java RefreshTokenRepositoryImpl.java
│   │   ├── security/JwtService.java
│   │   ├── security/PasswordEncoderConfig.java
│   │   ├── security/SecurityConfig.java
│   │   ├── security/TokenBlacklist.java
│   │   ├── security/CaffeineTokenBlacklist.java
│   │   ├── security/LoginAttemptService.java
│   │   ├── ratelimit/RateLimiter.java
│   │   ├── ratelimit/LocalBucketRateLimiter.java
│   │   └── alert/AlertSender.java LoggingAlertSender.java
│   └── config/WebConfig.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── db/migration/V1__init_user_account.sql V2__init_refresh_token.sql
└── src/test/java/com/library/
    ├── ArchitectureTest.java
    ├── interfaces/rest/AuthControllerIT.java
    ├── application/auth/AuthApplicationServiceTest.java
    ├── infrastructure/security/JwtServiceTest.java
    ├── infrastructure/security/CaffeineTokenBlacklistTest.java
    └── infrastructure/security/LoginAttemptServiceTest.java
```

### 前端 library-web/
```
library-web/
├── package.json vite.config.ts tsconfig.json
├── index.html
├── src/
│   ├── main.ts App.vue
│   ├── api/request.ts auth.ts
│   ├── stores/user.ts app.ts
│   ├── router/index.ts guards.ts
│   ├── layouts/AdminLayout.vue ReaderLayout.vue BlankLayout.vue
│   ├── views/auth/Login.vue Register.vue
│   ├── views/dashboard/Dashboard.vue (占位)
│   ├── views/error/NotFound.vue Forbidden.vue
│   ├── components/global/LangSwitch.vue
│   ├── i18n/index.ts locales/zh/common.json zh/menu.json zh/error.json en/common.json en/menu.json en/error.json
│   ├── utils/auth.ts trace.ts
│   └── styles/element-overrides.scss variables.scss
└── tests/
    ├── unit/stores/user.spec.ts
    └── unit/api/request.spec.ts
```

---

## 任务编号约定

- A1-A2 工程骨架
- A3-A6 用户/Token 领域 + 数据迁移
- A7-A11 JWT 服务与登录
- A12-A14 拦截器与权限
- A15-A17 异常 + TraceId + 限流
- A18 ArchUnit
- A19-A24 前端骨架
- A25-A27 前端登录与路由守卫
- A28 集成验证 + 文档
